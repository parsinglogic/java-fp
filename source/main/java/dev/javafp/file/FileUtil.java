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
import dev.javafp.util.ClassUtils;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.Say;
import dev.javafp.util.TextUtils;
import dev.javafp.util.ThreadUtils;

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
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static dev.javafp.util.ClassUtils.shortClassName;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

/**
 * <p> Some file utilities.
 *
 * <p> So the file system in Java is a very tricky place.
 * <p> Even in an o/s like Linux it is a tricky place
 * <p> full pathnames, relative pathnames, .., .,  case significance,  hard links, symbolic links, permissions, file-systems,
 * imaginary vs real paths, no proper transactions, how to report errors - oh my
 * <p> When we add to this the fact that Java doesn't know much about the file system it is being deployed on and the result is
 * an API that is quite tricky.
 * <p> For example:
 *
 * <pre>{@code
 *  Path x = getPath(tempDir, "x");
 *  Path z = getPath(x, "y", "z");
 * }</pre>
 *
 * <p> If we do the above (and
 * {@code x,y,z}
 *  do not exist) then ask
 * {@code y}
 *  for its parent, it returns
 * {@code x}
 * That is not what I would (naively) expect.
 * It is because
 * {@code getParent}
 *  operates on the path - and a path may well not
 * <strong>actually exist</strong>
 *  in the file system.
 * <p> Sometimes we are supplying a path to a function to create the file. The path does not exist yet - but we still want to be able
 * to ask questions about the hierarchy of the path.
 *
 * <p> There are a number of functions starting with
 * {@code assert}
 * .
 * <p> These are intended to be used by code that wants to assert that certain things are true about the file system before it attempts a particular
 * operation. They make it easier to do this without complicated boiler-plate code to write appropiated error messages.
 * <p> The idea is that these functions make that assertion and then throw a
 * {@link FileProblem}
 * with a reasonable error message about what assertion failed and why.
 *
 * <p> Of course, it is possible that an assertion might succeed, but a few milliseconds later, the filesystem might change and the asssertion
 * will now fail.
 *
 *
 */
public class FileUtil
{

    private static List<PosixFilePermission> WRITES = Arrays.asList(OWNER_WRITE, GROUP_WRITE, OTHERS_WRITE);
    private static List<PosixFilePermission> READS = Arrays.asList(OWNER_READ, GROUP_READ, OTHERS_READ);

    /**
     * Used as an argument to various file utility functions
     */
    public enum FileExists
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

    /**
     * Used as an argument to various file utility functions
     */
    public enum FileType
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

    /**
     * Used as an argument to various file utility functions
     */
    public enum FileAccess
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

    private FileUtil()
    {

    }

    /**
     * <p> Check
     * {@code path}
     *  exists/does not exist, is readable/writable, is a directory/file.
     * <p> If
     * {@code exists == FileExists.Exists}
     *  then if
     * {@code path}
     *  does not exist, do no further checks and return a
     * {@code Chat.Left}
     * .
     * <p> Otherwise,  check that
     * {@code path}
     *  exists and that its file fileAccess matches
     * {@code fileAccess}
     *  and that its file type
     * matches
     * {@code fileType}
     * <p> If any check fails, return a
     * {@code Chat.Left}
     *  that indicates the failure.
     *
     */
    public static Chat<Path> checkPath(Path path, FileExists exists, FileAccess fileAccess, FileType fileType)
    {
        return checkExists(exists, path)
                .flatMap(pth -> checkAccess(fileAccess, pth))
                .flatMap(pth -> checkFileType(fileType, pth));
    }

    private static Chat<Path> checkFileType(FileType fileType, Path path)
    {
        return fileType == FileType.Directory
               ? check(p -> !isADirectory(p), " is not a directory", path)
               : check(p -> isADirectory(p), " is not a file", path);
    }

    private static Chat<Path> checkAccess(FileAccess access, Path path)
    {
        return access == FileAccess.Readable
               ? check(p -> !isReadable(p), " is not readable", path)
               : check(p -> !isWritable(p), " is not writable", path);
    }

    private static Chat<Path> checkExists(FileExists exists, Path path)
    {
        return exists == FileExists.Exists
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
     * <p> Check that either
     * {@code path}
     *  exists, is a file and is writable or else that it does not exist.
     * <p> If any of these checks fail then throw
     * {@link FileProblem}
     * which has a message that tries to be clear about what the path was and why it was thrown.
     *
     * If the checks pass then do nothing.
     */
    public static void assertFileIsAFileAndWritableOrDoesNotExist(Path path)
    {
        if (Files.exists(path))
        {
            assertIsAFile(path);
            assertFileIsNotADirectory(path);
            assertFileIsWritable(path);
        }
    }

    /**
     * <p> Check that either
     * {@code path}
     *  is a file.
     * <p> If not then throw
     * {@link FileProblem}
     * which has a message that tries to be clear about what the path was and why it was thrown.
     *
     * <p> If
     * {@code path}
     *  is a file then do nothing.
     *
     *
     */
    public static void assertIsAFile(Path path)
    {
        if (!isAFile(path))
            throw FileProblem.create(path, "to be a file as defined by 'Files.isRegularFile'");
    }

    /**
     * <p> Check that the parent of
     * {@code path}
     *  is a directory
     * <p> If not then throw
     * {@link FileProblem}
     * which has a message that tries to be clear about what the path was and why it was thrown.
     *
     * <p> Otherwise, do nothing.
     *
     */
    public static void assertParentDirExists(Path path)
    {
        if (!isADirectory(path.getParent()))
            throw FileProblem.create(path, "to have a parent dir that exists");
    }

    /**
     * <p> Check that
     * {@code path}
     * exists
     * <p> If not then throw
     * {@link FileProblem}
     * which has a message that tries to be clear about what the path was and why it was thrown.
     *
     * <p> Otherwise, do nothing.
     *
     */
    public static void assertPathExists(Path path)
    {
        if (!exists(path))
            throw FileProblem.create(path, "to exist");
    }

    /**
     * <p>
     * {@code true}
     * if
     * {@code path}
     * exists,
     * {@code false}
     * otherwise
     *
     */
    public static boolean exists(Path path)
    {
        return Files.exists(path);
    }

    /**
     * <p> Assert that
     * {@code path}
     *  is readable in the filesystem.
     * <p> Throw
     * {@link FileProblem}
     * if this is not the case.
     *
     */
    public static void assertPathIsReadable(Path path)
    {
        //        assertPathExists(path);

        if (!isReadable(path))
            throw FileProblem.create(path, "to be readable");
    }

    /**
     * <p> Assert that
     * {@code path}
     *  does not represent a directory in the filesystem.
     * <p> Throw
     * {@link FileProblem}
     * if this is not the case.
     *
     */
    public static void assertFileIsNotADirectory(Path path)
    {
        if (isADirectory(path))
            throw FileProblem.create(path, "to not be a directory");
    }

    /**
     * <p> Assert that
     * {@code path}
     *  represents a directory in the filesystem.
     * <p> Throw
     * {@link FileProblem}
     * if this is not the case.
     *
     */
    public static void assertFileIsADirectory(Path path)
    {
        if (!isADirectory(path))
            throw FileProblem.create(path, "to be a directory");
    }

    /**
     * <p> {@code true}
     *  if
     * {@code path}
     *  represents a directory in the filesystem,
     * {@code false}
     *  otherwise.
     *
     */
    public static boolean isADirectory(Path path)
    {
        return Files.isDirectory(path.toAbsolutePath().normalize());
    }

    /**
     * <p> {@code true}
     *  if
     * {@code path}
     * represents a file (rather than a directory or a special file like
     * {@code /dev/tty}
     * ) in the filesystem,
     *
     * {@code false}
     *  otherwise.
     *
     */
    public static boolean isAFile(Path path)
    {
        return Files.isRegularFile(path.toAbsolutePath().normalize());
    }

    /**
     * <p> Assert that
     * {@code path}
     *  represents a directory in the filesystem, is writable and is empty.
     * <p> Throw
     * {@link FileProblem}
     * if this is not the case.
     *
     */
    public static void assertFileIsWritableEmptyDirOrDoesNotExist(Path path)
    {
        if (Files.exists(path))
        {
            assertFileIsADirectory(path);
            assertFileIsWritable(path);
            assertDirIsEmpty(path);
        }
    }

    /**
     * <p> {@code true}
     *  if
     * {@code path}
     *  represents an empty directory in the filesystem,
     * {@code false}
     *  otherwise.
     *
     */
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

    /**
     * <p> Assert that
     * {@code path}
     *  is writable in the filesystem.
     * <p> Throw
     * {@link FileProblem}
     * if this is not the case.
     *
     */
    public static void assertFileIsWritable(Path path)
    {
        if (!isWritable(path))
            throw FileProblem.create(path, "to be writable");
    }

    /**
     * <p> A list of the children of
     * {@code path}
     * wrapped in a
     * {@link Chat}
     *
     * <p> If
     * {@code path}
     *  does not exist a
     * {@code Chat.Left}
     *  is returned.
     * <p> If
     * {@code path}
     *  exists, but is not a directory, then return the empty list.
     *
     */
    public static Chat<ImList<Path>> getChildren(Path path)
    {
        try
        {
            if (!exists(path))
                return Chat.LeftFormat(path, "does not exist");
            else if (!isADirectory((path)))
                return Chat.Right(ImList.on());
            else if (!isReadable(path))
                return Chat.LeftFormat(path, "is not readable");
            else
                return Chat.Right(ImList.onIterator(Files.list(path).iterator()));
        } catch (IOException e)
        {
            return Chat.Left(TextUtils.format(path, " - exception ", ClassUtils.shortClassName(e), ": ", e.getMessage()));
        }

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

    static ImList<Path> pathList(Path path, boolean force)
    {
        Chat<ImList<Path>> chat = getChildren(path);

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

    static ImList<Path> deleteDirRecursively(Path pathToDelete, boolean force)
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

    /**
     * Create a temporary directory using
     * {@link Files#createTempDirectory(String, FileAttribute[])}
     *
     * The directory is set to be readable and writable after it is created and a shutdown hook is added to the VM
     * to delete it when the VM exists.
     */
    public static Path createTempDir()
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
     * <p> Create a writable temporary empty file in
     * {@code parentPath }
     * that will be deleted on exit
     *
     */
    public static Path createTempFileUnder(Path parentPath)
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

    /**
     * <p> Create a writable temporary empty file in a temporary dir
     * that will be deleted on exit
     *
     */
    public static Path createTempFile()
    {
        return createTempFileUnder(createTempDir());
    }

    /**
     * <p> For each pair -
     * {@code (path, contents)}
     *  in
     * {@code pairs}
     * create a file with path
     * {@code path}
     *  and contents
     * {@code contents}
     * .
     *
     *
     * <p> Return a
     * {@link Chat}
     * that describes what happened.
     */
    public static Chat<ImList<Path>> makeFiles(ImList<ImPair<Path, String>> pairs)
    {
        ImList<Chat<Path>> chats = pairs.map(p -> makeFile(p.fst, p.snd)).flush();
        return Chat.combine(chats);
    }

    /**
     * <p> Create a file with path
     * {@code path}
     *  and contents
     * {@code contents}
     *
     * <p> Return a
     * {@link Chat}
     * that describes what happened.
     *
     */
    public static Chat<Path> makeFile(Path path, String contents)
    {
        return exists(path)
               ? isADirectory(path)
                 ? Chat.Left(path + " - the name refers to an existing directory")
                 : makeFile3(path, contents)
               : getParent(path)
                       .flatMap(p -> FileUtil.createDir(p))
                       .flatMap(p -> makeFile3(path, contents));

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
        ThreadUtils.addShutdownHook(
                () ->
                {
                    ImList<Path> failed = deleteDirRecursively(dir, true);

                    if (!failed.isEmpty())
                    {
                        System.err.println("failed to delete temp files:");
                        System.err.println(failed);
                    }
                }
        );

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
     * <p>
     * {@code true }
     * if owner, group and other have read permissions,
     * {@code false } otherwise
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

    /**
     * <p> Read all the lines of text from the file
     * {@code path}
     *  as a list of
     * {@code Strings}
     *  in the returned
     *
     * {@link Chat}
     * There are many things that could go wrong - all these are reported in the
     *  {@link Chat}
     */
    public static Chat<ImList<String>> readLines(Path path)
    {
        //assertFileIsReadable(path);

        try
        {
            return Chat.Right(ImList.onList(Files.readAllLines(path)));
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

    /**
     * Given a path with components:
     * {@code a₀, a₁ ... aₙ}
     *
     * and
     * {@code otherNames = b₀, b₁, ... bₙ}
     *
     * return the path with components:
     *
     * {@code a₀, a₁, ... aₙ, b₀, b₁, ... bₙ}
     */
    public static Path getPath(Path parentDir, String... otherNames)
    {
        return otherNames.length == 0
               ? parentDir
               : unique(Paths.get(parentDir.toAbsolutePath().toString(), otherNames));
    }

    /**
     * <p> When reading a path from a file such as a Jadle project file we will need to accept relative and absolute
     * names.
     * <p> If absolute we need to use it - otherwise we need to resolve it against the parent dir where the file is
     *
     */
    public static Path resolve(Path parentDir, String otherPathAsString)
    {
        return unique(parentDir.resolve(otherPathAsString));
    }

    /**
     * <p> Return an absolute
     * <strong>and</strong>
     *  normalised path that represents the same path as
     * {@code path}
     * .
     * <p> Making a path absolute means that if it does not start with
     * {@code /}
     *  then prepend the current directory.
     * <p> Normalizing a path means removing any redundant elements taking any
     * {@code .}
     *  or
     * {@code ..}
     *  or repeated
     * {@code /}
     * <p> This path does not need to exist.
     *
     *
     */
    public static Path unique(Path path)
    {
        return path.toAbsolutePath().normalize();
    }

    /**
     * <p> The "real" path that
     * {@code path}
     *  represents, wrapped in an
     * {@link ImMaybe}
     * or
     * {@code Nothing}
     *  if the path does not exist
     */
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

    /**
     * <p> The parent of
     * {@code path}
     *  wrapped in a
     * {@link Chat}
     * or
     * {@code Chat.Left}
     *  if
     * {@code path}
     *  is the root
     * <p> Note that
     * {@code path}
     *  does not have to exist
     *
     */
    public static Chat<Path> getParent(Path path)
    {
        Path actual = unique(path);

        Path parent = actual.getParent();

        return parent == null
               ? Chat.Left("the root directory does not have a parent")
               : Chat.Right(parent);
    }

    /**
     * <p> {@code true}
     *  if
     * {@code possibleAncestor}
     *  is an ancestor of
     * {@code pathToTest}
     * .
     *
     * <pre>{@code
     * isAncestor(p,p) == true
     * }</pre>
     * <p> Neither path has to exist.
     *
     */
    public static boolean isAncestor(Path possibleAncestor, Path pathToTest)
    {
        return isAncestor$(unique(possibleAncestor), unique(pathToTest));
    }

    static boolean isAncestor$(Path possibleAncestor, Path pathToTest)
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
        return unique(Paths.get("."));
    }

    /**
     * <p> The
     * {@link Path}
     * represented by
     * {@code name}
     * <p> Throws
     * {@link java.nio.file.InvalidPathException}
     * if
     * {@code name}
     *  cannot be converted to a
     * {@code Path}
     *
     */
    public static Path getPath(String name)
    {
        return Paths.get(name);
    }

    /**
     * <p> Copy
     * {@code src }
     *  to
     * {@code dest }
     *  recursively
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
            Chat<Path> pathChat = checkAccess(FileAccess.Readable, src);

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
     * <p> The path that is
     * {@code prefix}
     *  followed by
     * {@code suffix}
     *
     */
    public static Path addSuffix(Path prefix, Path suffix)
    {
        return unique(prefix.resolve(suffix));
    }

    /**
     * <p> The relative path obtained by removing
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

    static BufferedReader newBufferedReader(Path path)
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

        return checkPath(path, FileExists.Exists, FileAccess.Readable, FileType.File)
                .flatMap(p -> Chat.Right(ImList.onList(readLines$(count, path))));
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

    /**
     * <p> Try to open a
     * {@link Socket}
     * on port
     * {@code port}
     *  on host
     * {@code host}
     * and return
     * {@code true}
     *  if it succeeded and
     * {@code false}
     *  if a
     * {@link ConnectException}
     * was thrown.
     * <p> The socket is closed if the connection succeeded.
     *
     */
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