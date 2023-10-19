/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.file;

import dev.javafp.eq.Eq;
import dev.javafp.eq.Equals;
import dev.javafp.ex.FileProblem;
import dev.javafp.ex.Throw;
import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.Chat;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.Say;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dev.javafp.util.ClassUtils.shortClassName;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

/**
 * <p> Some "file utilities"
 * <p> So the file system in Java is a very tricky place.
 * <p> Even in an o/s like Linux it is a tricky place
 * <p> full pathnames, relative pathnames, .., .,  case significance,  hard links, symbolic links, permissions, file-systems,
 * imaginary vs real paths, no proper transactions, how to report errors - oh my
 * <p> When we add to this the fact that Java doesn't know much about the file system it is being deployed on and the result is a large pile of poo
 * <p> For example:
 *
 * <pre>{@code
 *  Path x = getPath(tempDir, "x");
 *  Path z = getPath(x, "y", "z");
 * }</pre>
 * <p> If we do the above (and x,y,z do not exist) then ask x for its children it finds y
 * <p> That is not what I would expect
 *
 */
public class FileUtil
{

    // On OS/X the max file name size is 255
    public final static int MAX_FILE_NAME = 100;

    private static String allowedChars = "a-zA-Z0-9,._:=+@-";
    private static Pattern allowedCharsPattern = Pattern.compile("^[" + allowedChars + "]+$");

    public static ExistsEnum Exists = ExistsEnum.Exists;
    public static ExistsEnum Missing = ExistsEnum.Missing;
    private static List<PosixFilePermission> WRITES = Arrays.asList(OWNER_WRITE, GROUP_WRITE, OTHERS_WRITE);
    private static List<PosixFilePermission> READS = Arrays.asList(OWNER_READ, GROUP_READ, OTHERS_READ);

    /**
     * Used as an argument to various file utility functions
     */
    public enum ExistsEnum
    {
        /**
         * Expect a file to exist
         */
        Exists,

        /**
         * Expect a file to not exist
         */
        Missing
    }

    public static FileTypeEnum Directory = FileTypeEnum.Directory;
    public static FileTypeEnum File = FileTypeEnum.File;

    /**
     * Used as an argument to various file utility functions
     */
    public enum FileTypeEnum
    {
        /**
         * Expect a file to be a directory
         */
        Directory,
        /**
         * Expect a file to not be a directory
         */
        File
    }

    public static FileAccessEnum Readable = FileAccessEnum.Readable;
    public static FileAccessEnum Writable = FileAccessEnum.Writable;

    /**
     * Used as an argument to various file utility functions
     */
    public enum FileAccessEnum
    {
        /**
         * Expect a file to be readable
         */
        Readable,
        /**
         * Expect a file to be writable
         */
        Writable
    }

    public static Chat<Path> checkPathName(String pathName, ExistsEnum exists, FileAccessEnum fileAccess, FileTypeEnum fileType)
    {
        return FileUtil
                .checkPathNameIsReasonable(fileType, pathName)
                .flatMap(name -> Chat.Right(FileUtil.getPath(name)))
                .flatMap(path -> FileUtil.checkPath(path, exists, fileAccess, fileType));
    }

    public static Chat<Path> checkPath(Path path, ExistsEnum exists, FileAccessEnum access, FileTypeEnum fileType)
    {
        return checkExists(exists, path)
                .flatMap(pth -> checkAccess(access, pth))
                .flatMap(pth -> checkFileType(fileType, pth));
    }

    private static Chat<Path> checkFileType(FileTypeEnum fileType, Path path)
    {
        return fileType == Directory
               ? check(p -> !isADirectory(p), " is not a directory", path)
               : check(p -> isADirectory(p), " is not a file", path);
    }

    private static Chat<Path> checkAccess(FileAccessEnum access, Path path)
    {
        return access == Readable
               ? check(p -> !isReadable(p), " is not readable", path)
               : check(p -> !isWritable(p), " is not writable", path);
    }

    private static Chat<Path> checkExists(ExistsEnum exists, Path path)
    {
        return exists == Exists
               ? check(p -> !exists(p), " does not exist", path)
               : check(p -> exists(p), " already exists", path);

    }

    private static <A> Chat<A> check(Fn<A, Boolean> fn, String error, A a)
    {
        return fn.of(a)
               ? Chat.Left(error)
               : Chat.Right(a);
    }

    /**
     * <p> In Jadle, we have various config files that specify file names/dir names.
     * <p> In order to keep ourselves sane, let's impose some restrictions on what can be allowed in filenames
     *
     */
    public static Chat<String> checkPathNameIsReasonable(FileTypeEnum fileType, String name)
    {
        // @formatter:off
         return Chat.Right(name)
                 .flatMap(n -> n == null ? Chat.Left(" is null") : Chat.Right(n))
                 .flatMap(n -> n.isEmpty() ? Chat.Left(" can't be the empty string") : Chat.Right(n))
                 .flatMap(n -> !allowedCharsPattern.matcher(n).matches() ? Chat.Left(" can only contain " + allowedChars) : Chat.Right(n))
                 .flatMap(n -> fileType == FileTypeEnum.File && Eq.uals(n, ".") ? Chat.Left(" can't be .") : Chat.Right(n))
                 .flatMap(n -> fileType == FileTypeEnum.File && Eq.uals(n, "..") ? Chat.Left(" can't be ..") : Chat.Right(n))
                 .flatMap(n -> n.length() > MAX_FILE_NAME ? Chat.Left(" can't be longer than " + MAX_FILE_NAME + " characters") : Chat.Right(n));

        // @formatter:on
    }

    public static void assertFileIsAFileAndWritableOrDoesNotExist(Path path)
    {
        if (Files.exists(path))
        {
            assertIsAFile(path);
            assertFileIsNotADirectory(path);
            assertFileIsWritable(path);
        }
    }

    public static void assertIsAFile(Path path)
    {
        if (!isAFile(path))
            throw FileProblem.create(path, "to be a file as defined by 'Files.isRegularFile'");
    }

    public static void assertParentDirExists(Path path)
    {
        if (!Files.exists(path.toAbsolutePath().normalize().getParent()))
            throw FileProblem.create(path, "to have a parent dir that exists");
    }

    public static void assertFileExists(Path path)
    {
        if (!exists(path))
            throw FileProblem.create(path, "to exist");
    }

    public static boolean exists(Path path)
    {
        return Files.exists(path);
    }

    public static void assertFileIsReadable(Path path)
    {
        assertFileExists(path);

        if (!isReadable(path))
            throw FileProblem.create(path, "to be readable");
    }

    public static void assertFileIsNotADirectory(Path path)
    {
        if (isADirectory(path))
            throw FileProblem.create(path, "to not be a directory");
    }

    public static void assertFileIsADirectory(Path path)
    {
        if (!isADirectory(path))
            throw FileProblem.create(path, "to be a directory");
    }

    public static boolean isADirectory(Path path)
    {
        return Files.isDirectory(path.toAbsolutePath().normalize());
    }

    public static boolean isAFile(Path path)
    {
        return Files.isRegularFile(path.toAbsolutePath().normalize());
    }

    public static void assertFileIsWritableEmptyDirOrDoesNotExist(Path path)
    {
        if (Files.exists(path))
        {
            assertFileIsADirectory(path);
            assertFileIsWritable(path);
            assertDirIsEmpty(path);
        }
    }

    private static void assertDirIsEmpty(Path path)
    {
        try
        {
            Stream<Path> ps = Files.list(path);

            if (ps.findAny().isPresent())
                throw FileProblem.create(path, "to be empty");
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }

        //        if (path.toFile().list().length > 0)
        //            throw FileProblem.create(path, "to be empty");
    }

    public static void assertFileIsWritable(Path path)
    {
        if (!isWritable(path))
            throw FileProblem.create(path, "to be writable");
    }

    public static Chat<ImList<Path>> listChildren(Path path)
    {
        try
        {
            if (!isADirectory((path)))
                return Chat.Right(ImList.on());
            else if (!isReadable(path))
            {
                return Chat.Left("" + path + " is not readable");
            }
            else
            {
                return Chat.Right(ImList.onIterator(Files.list(path).iterator()));
            }
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }

        //        File[] files = path.toFile().listFiles();
        //
        //        return isADirectory(path)
        //               ? files == null
        //                 ? Chat.Left("" + path + " is not readable")
        //                 : Chat.Right(ImList.on(files).map(f -> f.toPath()))
        //               : Chat.Right(ImList.on());

    }

    /**
     * <p> An in-order list of paths - starting with
     * {@code path }
     * .
     * <p> There is a Java utility to do this - Files::walk -  but the problem with it is in the way that it handles the case when it can't
     * read a dir.
     * <p> It just throws an exception and returns no paths.
     * <p> I really want it to do something a bit more sensible. I would like it to return the list of paths it can get to and then
     * I can tell where there must be paths missing because certain dirs won't be readable.
     * <p> I want to use this when I delete a whole tree in FileUtil::deleteDirRecursively. I can go through the dirs, trying to delete them.
     * The ones that work will - well - work.
     * <p> There will be paths that I can't delete because:
     * <ol>
     * <li>
     * <p> the dir is not readable and it contains some files
     * </li>
     * <li>
     * <p> the parent of the dir is not writable
     * </li>
     * </ol>
     * <p> I will return these and then I can display them to the user as errors
     *
     */
    public static ImList<Path> pathList(Path path)
    {
        return pathList(path, false);
    }

    public static ImList<Path> pathList(Path path, boolean force)
    {
        Chat<ImList<Path>> chat = listChildren(path);

        if (chat.isOk())
            return chat.right.flatMap(p -> pathList(p, force)).push(path);
        else
        {
            if (force)
            {
                // Try setting the path to be readable
                setReadable(path, true);
                return pathList(path, false);
            }
            else
                return ImList.on(path);
        }
    }

    /**
     * <p> Try to delete the directory
     * {@code pathToDelete }
     * . If successful, return the empty list else return the
     * paths that we were unable to delete.
     * <p> If
     * {@code pathToDelete }
     *  is a file then delete it
     *
     */
    public static ImList<Path> deleteDirRecursively(Path pathToDelete)
    {
        return deleteDirRecursively(pathToDelete, true);
    }

    public static ImList<Path> deleteDirRecursively(Path pathToDelete, boolean force)
    {
        if (!exists(pathToDelete))
            return ImList.on();
        else if (isADirectory(pathToDelete))
            return FileUtil.pathList(pathToDelete, force).reverse().flatMap(p -> FileUtil.deleteIfExists(p, force));
        else if (isAFile(pathToDelete))
            return FileUtil.deleteIfExists(pathToDelete, force);
        else
            return ImList.on(pathToDelete);
    }

    /**
     * <p> Delete path. If succesful return the empty list else return a list on
     * {@code pathToDelete }
     * If the path is a directory this requires it to be empty
     *
     */
    public static ImList<Path> deleteIfExists(Path pathToDelete, boolean force)
    {
        try
        {

            Files.deleteIfExists(pathToDelete);
            return ImList.on();
        } catch (IOException e)
        {
            if (force)
            {
                Path parent = getPath(pathToDelete);
                setWritable(parent, true);
                return deleteIfExists(pathToDelete, false);
            }
            else
                return ImList.on(pathToDelete);
        }
    }

    /**
     * <p> Create a directory with the path
     * {@code path }
     *
     */
    public static Chat<Path> createDir(Path path)
    {
        if (isADirectory(path))
            return Chat.Right(path);
        else if (isAFile(path))
            return Chat
                    .Left("the directory " + path
                            + " could not be created because that name refers to an existing file (ie not a directory)");
        else
        {
            return filesCreateDir(path)
                   ? Chat.Right(path)
                   : Chat.Left("the directory " + path + " could not be created");
        }
    }

    private static boolean filesCreateDir(Path path)
    {
        try
        {
            Files.createDirectories(path);
            return true;
        } catch (IOException e)
        {
            Say.say(e);
            return false;
        }
    }

    public static Path createTempDir()
    {
        return createTempDir(null);
    }

    public static Path createTempDir(String prefix)
    {
        try
        {
            Path path = Files.createTempDirectory(null);

            setReadable(path, true);
            setWritable(path, true);

            // Add a shutdown hook to delete the files in the directory
            addShutdownHookToDelete(path);

            return path;
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    /**
     * <p> Create a temporary path in
     * {@code parentPath }
     *  that will be deleted on exit
     *
     */
    public static Path createTempPathUnder(Path parentPath)
    {
        try
        {
            Path p = Files.createTempFile(parentPath, null, null);
            setWritable(p, true);
            p.toFile().deleteOnExit();
            return p;
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public static Path createTempPath()
    {
        return createTempPathUnder(createTempDir());
    }

    public static Chat<ImList<Path>> makeFiles(ImList<ImPair<Path, String>> pairs)
    {
        ImList<Chat<Path>> chats = pairs.map(p -> makeFile(p.fst, p.snd)).flush();
        return Chat.combine(chats);
    }

    public static Chat<Path> makeFile(Path path, String contents)
    {
        // @formatter:off
        return exists(path)
                        ? isADirectory(path)
                                ? Chat.Left("the name refers to an existing directory")
                                : makeFile3(path, contents)
                        : getParent(path)
                                .flatMap(p -> FileUtil.createDir(p))
                                .flatMap(p -> makeFile3(path, contents));
         // @formatter:on
    }

    private static Chat<Path> makeFile3(Path path, String contents)
    {

        //
        //        try (FileOutputStream fos = new FileOutputStream(path.toFile());
        //                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"))))
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path)))
        {
            out.print(contents);

            setWritable(path, true);
            setReadable(path, true);
            return Chat.Right(path);
        } catch (AccessDeniedException e)
        {
            return Chat.Left("the file permissions do not allow it to be written");
        } catch (IOException e)
        {
            return Chat.Left("the file could not be written - the error was: " + getMessage(e));
        }
    }

    private static void addShutdownHookToDelete(Path dir)
    {
        Runnable r = () ->
        {

            ImList<Path> failed = deleteDirRecursively(dir, true);

            if (!failed.isEmpty())
            {
                System.err.println("failed to delete temp files:");
                System.err.println(failed);
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(r));
    }

    /**
     * <p> Set owner, group and other to have read permissions. Leave the other permissions as they are.
     */
    public static boolean setReadable(Path path, boolean beReadable)
    {
        try
        {
            HashSet<PosixFilePermission> perms = new HashSet<>(Files.getPosixFilePermissions(path));

            if (beReadable)
                perms.addAll(READS);
            else
                perms.removeAll(READS);

            Files.setPosixFilePermissions(path, perms);

            return true;

        } catch (IOException e)
        {
            return false;
        }
    }

    /**
     * <p> {@code true }
     *  if owner, group and other have read permissions
     *
     */
    public static boolean isReadable(Path path)
    {
        try
        {
            return Files.getPosixFilePermissions(path).containsAll(READS);

        } catch (IOException e)
        {
            return false;
        }
    }

    /**
     * <p> Set owner, group and other to have write permissions. Leave the other permissions as they are.
     */
    public static boolean setWritable(Path path, boolean beWriteable)
    {
        try
        {
            HashSet<PosixFilePermission> perms = new HashSet<>(Files.getPosixFilePermissions(path));

            if (beWriteable)
                perms.addAll(WRITES);
            else
                perms.removeAll(WRITES);

            Files.setPosixFilePermissions(path, perms);

            return true;

        } catch (IOException e)
        {
            return false;
        }
    }

    /**
     * <p> {@code true }
     *  if owner, group and other have write permissions
     *
     */
    public static boolean isWritable(Path path)
    {
        try
        {
            return Files.getPosixFilePermissions(path).containsAll(WRITES);

        } catch (IOException e)
        {
            return false;
        }
    }

    public static Chat<ImList<String>> readLines(Path path)
    {
        //assertFileIsReadable(path);

        try
        {
            return Chat.Right(ImList.on(Files.readAllLines(path)));
        } catch (NoSuchFileException e)
        {
            return Chat.Left("the file does not exist");
        } catch (AccessDeniedException e)
        {
            return Chat.Left("the file permissions do not allow it to be read");
        } catch (IOException e)
        {
            return Equals.isEqual(e.getMessage(), "Is a directory")
                   ? Chat.Left("it is a directory - not a file")
                   : Chat.Left("the file could not be read - the error was: " + getMessage(e));
        } catch (Exception e)
        {
            return Chat.Left("the file could not be read -  the error was: " + getMessage(e));
        }
    }

    private static String getMessage(Exception e)
    {
        return shortClassName(e) + ": " + e.getMessage();
    }

    public static Path getPath(Path parentDir, String... otherNames)
    {
        return otherNames.length == 0
               ? parentDir
               : normalize(Paths.get(parentDir.toAbsolutePath().toString(), otherNames));
    }

    /**
     * <p> When reading a path from a file such as a Jadle project file we will need to accept relative and absolute
     * names.
     * <p> If absolute we need to use it - otherwise we need to resolve it against the parent dir where the file is
     *
     */
    public static Path resolve(Path parentDir, String otherPathAsString)
    {
        return normalize(parentDir.resolve(otherPathAsString));
    }

    public static Path normalize(Path path)
    {
        return path.toAbsolutePath().normalize();
    }

    public static ImMaybe<Path> getRealPath(Path path)
    {
        try
        {
            return ImMaybe.just(path.toRealPath());
        } catch (IOException e)
        {
            return ImMaybe.nothing;
        }
    }

    public static Chat<Path> getParent(Path path)
    {
        Path parent = normalize(path).getParent();

        return parent == null
               ? Chat.Left("the root directory does not have a parent")
               : Chat.Right(parent);
    }

    public static boolean isAncestor(Path possibleAncestor, Path pathToTest)
    {
        return isAncestor$(normalize(possibleAncestor), normalize(pathToTest));
    }

    public static boolean isAncestor$(Path possibleAncestor, Path pathToTest)
    {
        if (Eq.uals(pathToTest, possibleAncestor))
        {
            return true;
        }
        else
        {
            Path parent = pathToTest.getParent();

            return parent == null ? false : isAncestor$(possibleAncestor, parent);
        }
    }

    /**
     * <p> The current working dir
     */
    public static Path cwd()
    {
        return normalize(Paths.get("."));
    }

    public static Path getPath(String name)
    {
        return Paths.get(name);
    }

    /**
     * <p> Copy
     * {@code src }
     *  to `dest, recursively
     * <p> copy /a/b/c/src  x/y/z/dest
     *
     * <pre>{@code
     *          Before                              After
     *
     *     a              x                     a              x
     *     .              .                     .              .
     *     b              y                     b              y
     *     .                                    .              .
     *     c                                    c              z
     *     .                                    .              .
     *    src                                  src            dest
     *     .                                    .              .
     *     1                                    1              1
     *     .                                    .              .
     *  ........                             ........       ........
     *  .   .  .                             .   .  .       .   .  .
     *  2   3  4                             2   3  4       2   3  4
     * }</pre>
     * <p> If the destination path does not exist it will be created (along with all its parents)
     * <p> This is
     * <em>different from</em>
     *  how the unix cp -R command works.
     * <p> I hope it is more flexible
     * <p> this is a deceptively simple operation that is actually quite complicated to get right
     * How do we report failures? The destination can't be created? The source can't be read?
     * <p> What can go wrong?
     * <p> src is an ancestor of dest
     * dest is an ancestor of src
     * src == dest
     * src can't be read
     * One of the descendents of src can't be read
     * dest exists
     * dest exists and can't be written
     * dest does not exist and can't be created
     * src is a file, dest is a non-empty dir
     * <p> If dest does not exist but dest.getParent() exists then it is easy to see what we should do.
     * If dest exists and is a dir and so is src then is the idea to overlay src on dest, maintaining all the existing nodes
     * where we can and overwriting when we can't. Or should we overwright where we can and report an error where we can't
     * Or should we give priority to existing nodes in dest.
     * <p> I have not solved many of these problems yet. I only copy to work in some special circumstances
     * <p> I am using Chat to try to sovle some of the problems - to an extent
     *
     */

    public static Chat<ImList<Path>> copy(Path src, Path dest)
    {
        if (!exists(src))
            return Chat.Left("" + src + " does not exist");
        else if (isAncestor(src, dest))
            return Chat.Left("" + src + " is an ancestor of " + dest);
        else if (isAncestor(dest, src))
            return Chat.Left("" + dest + " is an ancestor of " + src);
        else
            return Chat.combine(copyPaths(createDir(dest.getParent()), src, dest, pathList(src)));
    }

    /**
     * <p> Copy
     * {@code paths }
     *  to
     * {@code dest }
     *  where the starting path is
     * {@code src }
     * <p> If there is a problem with reading a path then abort and return the error in the chat
     * <p> I tried to use foldl but had problems compiling it so I am doing the recursion by hand
     *
     */
    private static ImList<Chat<Path>> copyPaths(Chat<Path> previousResult, Path src, Path dest, ImList<Path> paths)
    {
        if (paths.isEmpty() || !previousResult.isOk())
            return ImList.on();
        else
        {
            Path n = paths.head();
            Chat<Path> res = copySingle(n, addSuffix(dest, removePrefix(n, src)));
            return ImList.cons(res, copyPaths(res, src, dest, paths.tail()));
        }
    }

    /**
     * <p> Copy src to target
     * <p> After the copy we expect
     * <p> if isAFile(src) then
     * isAFile(target)
     * else
     * isADirectory(target)
     * <p> I would like to make this a public method - but I haven't got time to do all the error handling now
     *
     */
    private static Chat<Path> copySingle(Path src, Path target)
    {
        try
        {
            //say(table("src", src, "target", target))

            // Let's check the file is readable
            Chat<Path> pathChat = checkAccess(Readable, src);

            // If the target parent doesn't exist

            return pathChat.isOk()
                   ? Chat.Right("" + src + " -> " + target, copyFile(src, target))
                   : pathChat.prependToFirstLine("" + src);

        } catch (IOException e)
        {
            // Just in case - we report any exception as a chat
            return Chat.Left("Exception - " + e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
    }

    private static Path copyFile(Path src, Path target) throws IOException
    {
        return FileUtil.isADirectory(target)
               ? target
               : Files.copy(src, target);
    }

    /**
     * <p> Let's think of paths as lists of strings
     * <p> This function "adds" two paths together
     * <p> plus(a/b, c/d/e) = a/b/c/d/e
     *
     */
    public static Path addSuffix(Path prefix, Path suffix)
    {
        return prefix.resolve(suffix);
    }

    /**
     * <p> Let's think of paths as lists of strings
     * <p> This function removes
     * {@code prefix }
     *  from
     * {@code prefixPlusSuffix }
     *  to leave the suffix. In a sense it is subtracting
     * {@code prefix }
     *  from
     * {@code prefixPlusSuffix }
     * <p> minus( a/b/c/d/e, a/b) = c/d/e
     * <p> We can also think of this as giving the relative path from
     * {@code prefix }
     *  to
     * {@code prefixPlusSuffix }
     * <p> minus(plus(p,s), p) === s
     *
     */
    public static Path removePrefix(Path prefixPlusSuffix, Path prefix)
    {
        return prefix.relativize(prefixPlusSuffix);
    }

    public static BufferedReader newBufferedReader(Path path)
            throws IOException
    {
        Reader reader = new InputStreamReader(Files.newInputStream(path));
        return new BufferedReader(reader);
    }

    /**
     * <p> Read
     * {@code count }
     *  lines from
     * {@code path }
     * <p> where
     * {@code count >= 0 }
     * <p> If the file contains n lines where
     * {@code n < count }
     *  then the length of the returned list will be n
     *
     */
    public static Chat<ImList<String>> readLines(int count, Path path)
    {
        Throw.Exception.ifLessThan("count", count, 0);

        return checkPath(path, FileUtil.ExistsEnum.Exists, FileUtil.Readable, FileUtil.File)
                .flatMap(p -> Chat.Right(ImList.on(readLines$(count, path))));
    }

    private static List<String> readLines$(int count, Path path)
    {
        try (BufferedReader reader = newBufferedReader(path))
        {
            List<String> result = new ArrayList<>();
            int readCount = 0;

            while (true)
            {
                if (readCount >= count)
                    return result;

                String line = reader.readLine();

                if (line == null)
                    break;
                else
                    result.add(line);

                readCount++;
            }

            return result;

        } catch (Exception e)
        {
            throw new UnexpectedChecked(e);
        }

    }

    public static boolean trySocket(String host, int port)
    {
        try
        {
            try (Socket ignored = new Socket(host, port))
            {
                return true;
            } catch (ConnectException ex)
            {
                return false;
            }
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

}