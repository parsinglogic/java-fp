package dev.javafp.file;

import dev.javafp.eq.Eq;
import dev.javafp.ex.ArgumentShouldNotBeLessThan;
import dev.javafp.ex.FileProblem;
import dev.javafp.lst.ImList;
import dev.javafp.net.ImUrl;
import dev.javafp.set.ImMap;
import dev.javafp.set.ImSet;
import dev.javafp.tree.ImRoseTree;
import dev.javafp.tree.ImRoseTreeShapes;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.Chat;
import dev.javafp.util.Say;
import dev.javafp.util.TestUtils;
import dev.javafp.util.TextUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.javafp.file.FileUtil.assertFileIsADirectory;
import static dev.javafp.file.FileUtil.assertFileIsAFileAndWritableOrDoesNotExist;
import static dev.javafp.file.FileUtil.assertFileIsNotADirectory;
import static dev.javafp.file.FileUtil.assertParentDirExists;
import static dev.javafp.file.FileUtil.assertPathExists;
import static dev.javafp.file.FileUtil.assertPathIsReadable;
import static dev.javafp.file.FileUtil.copy;
import static dev.javafp.file.FileUtil.createDir;
import static dev.javafp.file.FileUtil.createTempDir;
import static dev.javafp.file.FileUtil.createTempFile;
import static dev.javafp.file.FileUtil.cwd;
import static dev.javafp.file.FileUtil.deleteDirRecursively;
import static dev.javafp.file.FileUtil.deleteIfExists;
import static dev.javafp.file.FileUtil.exists;
import static dev.javafp.file.FileUtil.getParent;
import static dev.javafp.file.FileUtil.getPath;
import static dev.javafp.file.FileUtil.getRealPath;
import static dev.javafp.file.FileUtil.isAncestor;
import static dev.javafp.file.FileUtil.makeFile;
import static dev.javafp.file.FileUtil.pathList;
import static dev.javafp.file.FileUtil.readLines;
import static dev.javafp.file.FileUtil.removePrefix;
import static dev.javafp.file.FileUtil.resolve;
import static dev.javafp.file.FileUtil.setReadable;
import static dev.javafp.file.FileUtil.setWritable;
import static dev.javafp.util.Say.say;
import static dev.javafp.util.TestUtils.assertThrows;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by aove215 on 07/05/16.
 */
public class FileUtilTest
{
    private Path userDir;
    private Path tempFile;
    private Path nonExistent;
    private Path tempDir;
    private String contents;
    private Path devTty;

    @Before
    public void before()
    {
        userDir = Paths.get(System.getProperty("user.dir"));
        tempFile = createTempFile();
        contents = "stuff";
        makeFile(tempFile, contents);

        tempDir = createTempDir();

        nonExistent = Paths.get("/i/don/t/exist");

        devTty = Paths.get("/dev/tty");
    }

    @Test
    public void testListChildren()
    {

        setReadable(tempDir, false);

        Chat<ImList<Path>> lstChat = FileUtil.getChildren(tempDir);

        assertEquals(false, lstChat.isOk());
        assertEquals(tempDir.toString() + " is not readable", lstChat.getChatString());

        setReadable(tempDir, true);

        Chat<ImList<Path>> lstChat2 = FileUtil.getChildren(getPath(tempDir, "hkjgkhgkh"));

        assertEquals(false, lstChat2.isOk());
        assertEquals(tempDir.toString() + "/hkjgkhgkh does not exist", lstChat2.getChatString());

    }

    @Test
    public void testPathListForce()
    {
        setReadable(tempDir, false);

        ImList<Path> paths = FileUtil.pathList(tempDir, true);

        assertEquals(ImList.on(tempDir), paths);

        setReadable(tempDir, true);
    }

    @Test
    public void testCanForceDeleteUnwritableFile()
    {
        setWritable(tempFile, false);

        deleteIfExists(tempFile, true);
        assertEquals(false, exists(tempFile));
    }

    @Test
    public void testCanForceDeleteUnwritableFileRecursively()
    {
        Path unReadable = getPath(tempDir, "unreadable");
        Path unWritable = getPath(tempDir, "unreadable", "unwritable");

        setReadable(unReadable, false);
        setWritable(unWritable, false);

        deleteDirRecursively(tempDir, true);

        assertEquals(false, exists(tempDir));
    }

    @Test
    public void testPathListLarge()
    {

        /**
         * So this test shows that FileUtil.pathList will return only the paths "that it can get to"
         * There might be some paths that are not readable - For example in this hierarchy:
         *
         *
         *   │   └── 1
         *   │       └── 2
         *   │           └── 3
         *   │               └── 4
         *
         * if 3 and 4 are not readable then we should only see 1 and 2 in the list
         *
         * So I decided to do the test 'properly'. Instead of jsut setting up a tree of paths, setting the readable attribute
         * on a few of them and then asserting that I got the write set back for that one test, I will set up all the possible shapes
         * of trees and then take all the possible subsets of these and set readable to false and then assert that all of these give
         * the correct results.
         *
         * Hmm - this took me almost a whole day
         *
         * As ever, testing code properly is more work than writing the code in the first place.
         * I am not even testing that the list is in the correct order.
         *
         * The difficulty is compounded by the fact that the function I am testing is a function that I need to use in the tidying up after
         * the test.
         */
        // Make some shapes
        ImList<ImRoseTree<String>> shapes = ImRoseTreeShapes.allTreesWithSize(4);

        /**
         *  If we so the tests with size 4 we will get shapes like these:
         *
         *
         *
         *
         *   1
         *   └── 2
         *       └── 3
         *           └── 4
         *   1
         *   └── 2
         *       ├── 3
         *       └── 4
         *   1
         *   ├── 2
         *   │   └── 3
         *   └── 4
         *
         *   1
         *   ├── 2
         *   └── 3
         *       └── 4
         *
         *   1
         *   ├── 2
         *   ├── 3
         *   └── 4
         *
         *
         *
         */

        shapes.foreach(
                s ->
                {

                    // Make a dir that has the name of the shape in it to create the dirs in
                    Path parent = FileUtil.getPath(tempDir, "tests", s.toString());

                    // Create matching dirs in the file system
                    // We get a list of pairs mapping the short name of the dir to the path
                    ImList<ImPair<String, Path>> dirs = makeDirTreeMatching(s, parent);

                    // Turn it into a map from name to path
                    ImMap<String, Path> namesToPaths = ImMap.fromPairs(dirs);

                    // Get a map from name to tree
                    ImMap<String, ImRoseTree<String>> namesToTrees = ImMap.fromPairs(s.toImList().map(t -> ImPair.on(t.getElement(), t)));

                    //                System.out.println(TopDownBox.withAll(namesToTrees));
                    //                System.out.println(TopDownBox.withAll(namesToPaths));

                    ImList<String> nodeNames = namesToPaths.keys();
                    ImSet<String> nodes = ImSet.onAll(nodeNames);

                    // for each subset of the nodes
                    nodeNames.powerSet().foreach(
                            set ->
                            {
                                // Set the readable state on each dir so that the nodes in `set` are not readable but the others are
                                nodeNames.foreach(k -> FileUtil.setReadable(namesToPaths.get(k), !set.contains(k)));

                                // List the path. Actually we are considering just the nodes generated by the shapes so we are not interested in
                                // the top node
                                ImList<Path> pathList = FileUtil.pathList(parent, false).tail();

                                //System.out.println("pathlist " + pathList);

                                // Assert that all the descendants of the unreadables should be missing. All others present

                                // What is the set of all the descendants of the unreadable nodes?
                                // It is the union of the proper descendents of all the unreadable nodes
                                //     1
                                //    ...
                                //    2 3
                                //     ...
                                //     4 5
                                //       .
                                //       6
                                // If 1 is unreadable then 2 3 4 5 should be missing from the list
                                // if 3 and 5 are unreadable then 4 5 6 should be missing

                                // Get the descendents for each tree in the set and add them to the unreadable set

                                ImSet<String> empty = ImSet.empty();
                                ImSet<String> unreadable = set
                                        .foldl(empty, (ss, tree) -> ss.union(getDescendents(namesToTrees.get(tree))));

                                //                        System.out.println("start unreadable " + set);
                                //                        System.out.println("end unreadable " + unreadable);

                                // get a list of the paths that we should see

                                ImList<Path> expectedPaths = nodes.minus(unreadable).toList().map(n -> namesToPaths.get(n));

                                // Assert that these are the paths that we see
                                TestUtils.assertSetsEqual("unreadable + " + unreadable, expectedPaths, pathList);
                            }
                    );
                }
        );

        //namesToPaths.keys().foreach(k -> FileUtil.setReadable(namesToPaths.get(k), !set.contains(k)));
    }

    private ImList<String> getDescendents(ImRoseTree<String> tree)
    {
        ImList<String> trees = tree.toImList().map(t -> t.getElement());
        return trees.tail();
    }

    private ImList<ImPair<String, Path>> makeDirTreeMatching(ImRoseTree<String> tree, Path parent)
    {
        Chat<Path> dir1 = createDir(getPath(parent, tree.getElement()));

        Path dir = dir1.right;

        // Note that we do a flush here.
        return tree.getSubTrees().flatMap(t -> makeDirTreeMatching(t, dir)).flush().push(ImPair.on(tree.getElement(), dir));
    }

    @Test
    public void testListChildrenOnUrbanSpaceman()
    {
        Chat<ImList<Path>> lstChat = FileUtil.getChildren(nonExistent);

        assertEquals(false, lstChat.isOk());
        assertEquals("/i/don/t/exist does not exist", lstChat.getChatString());
    }

    @Test
    public void testAssertFileIsWritableOrDoesNotExist()
    {
        assertFileIsAFileAndWritableOrDoesNotExist(tempFile);

        assertFileIsAFileAndWritableOrDoesNotExist(nonExistent);

        assertThrows(() -> assertFileIsAFileAndWritableOrDoesNotExist(userDir), FileProblem.class);
    }

    @Test
    public void testAssertParentDirExists()
    {
        assertParentDirExists(userDir);
        assertThrows(() -> assertParentDirExists(Paths.get(tempDir.toString(), "foo", "bar")), FileProblem.class);

        // Make a "parent" that is not  a dir
        FileUtil.makeFile(getPath(tempDir, "foo"), "foo");

        assertThrows(() -> assertParentDirExists(Paths.get(tempDir.toString(), "foo", "bar")), FileProblem.class);
    }

    @Test
    public void testAssertFileExists()
    {
        assertPathExists(tempFile);
        assertThrows(() -> assertPathExists(nonExistent), FileProblem.class);
    }

    @Test
    public void testAssertFileIsReadable()
    {
        assertPathIsReadable(tempFile);
    }

    @Test
    public void testAssertFileIsNotADirectory()
    {
        assertThrows(() -> assertFileIsNotADirectory(tempDir), FileProblem.class);
    }

    @Test
    public void testAssertFileIsADirectory()
    {
        assertFileIsADirectory(tempDir);
    }

    @Test
    public void testAssertIsAFileWhenItIsAFile()
    {
        FileUtil.assertIsAFile(tempFile);

    }

    @Test
    public void testAssertIsAFileWhenItIsNotAFile()
    {
        assertThrows(() -> assertFileIsNotADirectory(tempDir), FileProblem.class);
    }

    @Test
    public void testDeleteIfExists()
    {
        deleteIfExists(tempFile, false);
        assertThrows(() -> assertPathExists(tempFile), FileProblem.class);
    }

    @Test
    public void testCreateTempDir()
    {
        assertFileIsADirectory(tempDir);
    }

    @Test
    public void testMakeDirs()
    {
        String error = createDir(Paths.get("/dev/tty")).left.toString();

        boolean ok = Eq.uals("[the directory /dev/tty could not be created]", error) ||
                Eq.uals("[/dev/tty: Device not configured]", error);

        if (!ok)
        {
            fail("Expected error one of two errors but got " + error);
        }

        assertEquals(true, createDir(tempDir).isOk());
        assertEquals(ImList.on("the directory " + tempFile
                        + " could not be created because that name refers to an existing file (ie not a directory)"),
                createDir(tempFile).left);
    }

    @Test
    public void testWrite()
    {
        //        Path path = getPath(tempDir, "foo", "bar");
        //        System.out.println(path);
        //
        //        System.out.println(Files.write(path, contents.getBytes()));

        //        Path path2 = Paths.get("/dev/tty");
        //        System.out.println(path2);
        //
        //        System.out.println(Files.write(path2, contents.getBytes()));

        //System.out.println(Files.write(tempDir, contents.getBytes()));
        //
        //        setWritable(tempFile, false);
        //
        //        System.out.println(Files.write(tempFile, contents.getBytes()));
    }

    // This test does different things when run from IJ and jadle
    public void testMakeFileFailsOnTty()
    {
        assertEquals(ImList.on("the file could not be written - the error was: FileSystemException: /dev/tty: Device not configured"),
                makeFile(devTty, contents).left);
    }

    @Test
    public void testMakeFileFailsOnDir()
    {
        assertEquals(ImList.on("/ - the name refers to an existing directory"),
                makeFile(Paths.get("/"), contents).left);
    }

    @Test
    public void testMakeFileCreatesDir()
    {
        Path path = getPath(tempDir, "foo", "bar");
        assertTrue(makeFile(path, contents).isOk());

        assertEquals(ImList.on(contents), readLines(path).right);
    }

    @Test
    public void testGetAncestor()
    {

        /**
         *           tempDir
         *              .
         *           testCopy
         *              .
         *          .............
         *          .           .
         *         1            x
         *         .            .
         *      ........        y
         *      .   .  .
         *      2   3  4
         *
         */
        Path x = getPath(tempDir, "x");
        Path z = getPath(x, "y", "z");

        Path root = Paths.get("/");

        testIsAncestor(root, root);
        testIsAncestor(root, tempDir);

        testIsAncestor(tempDir, z);
        testIsAncestor(x, z);

    }

    @Test
    public void testFunny()
    {

        Path x = getPath(tempDir, "x");
        Path z = getPath(x, "y", "z");

        assertFalse(FileUtil.exists(x));

        assertTrue(FileUtil.isAncestor(x, z));

    }

    private void testIsAncestor(Path p, Path q)
    {
        assertTrue(isAncestor(p, q));
        assertTrue(isAncestor(p, p));
        assertTrue(isAncestor(q, q));

        // Assert the negation of the converse - if the paths are not equal of course
        if (!Eq.uals(p, q))
        {
            assertFalse(isAncestor(q, p));
        }
    }

    @Test
    public void testCopyReturnsAnErrorWhenSrcIsAncestorOfTargetAndViceVersa()
    {
        Path x = getPath(tempDir, "x");
        Path z = getPath(x, "y", "z");

        // Hmm If we ask x for its children it finds y
        // This is not what I would expect

        FileUtil.deleteDirRecursively(x);

        // Better create the fellas or else we will get another error message
        createDir(z);

        Chat<ImList<Path>> copyChat = copy(x, z);

        assertFalse(copyChat.isOk());
        assertTrue(copyChat.left.head().contains("/x is an ancestor of "));

        Chat<ImList<Path>> copyChat2 = copy(z, x);

        assertFalse(copyChat2.isOk());
        assertTrue(copyChat2.left.head().contains("/x is an ancestor of "));
    }

    @Test
    public void testCopyReturnsAnErrorWhenSrcDoesNotExist()
    {
        Path x = getPath(tempDir, "x");
        Path y = getPath(tempDir, "y");

        FileUtil.deleteDirRecursively(x);

        Chat<ImList<Path>> copyChat = copy(x, y);

        assertFalse(copyChat.isOk());
        assertTrue(Eq.uals("" + x + " does not exist", copyChat.left.head()));
    }

    @Test
    public void testCopyShapes()
    {
        ImList<ImRoseTree<String>> shapes = ImRoseTreeShapes.allTreesWithSize(5);

        say("shapes", shapes);

        shapes.foreach(t -> testCopyShape(t));
    }

    public void testCopyShape(ImRoseTree<String> tree)
    {
        /**
         *           tempDir
         *              .
         *           testCopy
         *              .
         *          .............
         *          .           .
         *      srcParent  targetParent
         *         .
         *         1
         *         .
         *      ........
         *      .   .  .
         *      2   3  4
         *
         */

        say("testCopyShape", tree);
        Path topLevel = getPath(tempDir, "testCopy");
        FileUtil.deleteDirRecursively(topLevel);

        FileUtil.createDir(topLevel);

        Path srcParent = getPath(topLevel, "srcParent");
        Path targetParent = getPath(topLevel, "targetParent");

        FileUtil.createDir(srcParent);

        // Create a directory tree that matches the tree
        makeDirTreeMatching(tree, srcParent);

        Path src = getPath(srcParent, "1");
        Path target = getPath(targetParent, "1");

        // Just for grins, add a file to each node
        pathList(src).forEach(p -> makeFile(getPath(p, "file"), p.toString()));

        // copy(src, targetParent);
        copy(src, getPath(targetParent, "" + src.getFileName()));

        // target = tmpDir/x/y/testCopy

        String srcPaths = relativePathList(src).toString("\n");
        String targetPaths = relativePathList(target).toString("\n");

        say("srcPaths", srcPaths);
        say("targetPaths", targetPaths);

        assertEquals(srcPaths, targetPaths);

    }

    private ImList<String> relativePathList(Path srcPath)
    {
        return FileUtil.pathList(srcPath).map(p -> "" + removePrefix(p, srcPath));
    }

    @Test
    public void testMakeFileOverwrites()
    {
        assertEquals(ImList.on(contents), readLines(tempFile).right);

        String newContents = "new";
        assertTrue(makeFile(tempFile, newContents).isOk());

        assertEquals(ImList.on(newContents), readLines(tempFile).right);
        assertEquals(ImList.on(newContents), readLines(tempFile).right);
    }

    @Test
    public void testCanDeleteReadOnlyFile()
    {
        setReadable(tempFile, false);

        deleteIfExists(tempFile, false);
        assertEquals(false, exists(tempFile));
    }

    @Test
    public void testMakeFileWorksWithNonPrintableChars()
    {
        String newContents = "\u0000\u0002\u007F\uFFFA\uE000";
        assertTrue(makeFile(tempFile, newContents).isOk());

        assertEquals(ImList.on(newContents), readLines(tempFile).right);
    }

    @Test
    public void testMakeFileWithSurrogateChars()
    {
        String newContents = "\uD800";
        assertTrue(makeFile(tempFile, newContents).isOk());

        // When we write a surrogate character using UTF-8, when we read it back again, it is read as a question mark
        assertEquals(ImList.on(), readLines(tempFile).right);
    }

    @Test
    public void testSurrogateCharsCannotBeEncodedInUTF8() throws UnsupportedEncodingException
    {
        String newContents = "\uD800";
        byte[] bytes = newContents.getBytes("UTF-8");

        // It gets encoded as a question mark
        // It would be nice to have this as some sort of encoding error but hey
        assertEquals(1, bytes.length);
        assertEquals((byte) 63, bytes[0]);
    }

    @Test
    public void testMakeFileFailsWhenDirReadOnly()
    {
        Path readOnlyTempDir = createTempDir();

        FileUtil.setWritable(readOnlyTempDir, false);

        Path abc = getPath(readOnlyTempDir, "abc");
        Chat<Path> result = makeFile(abc, contents);
        assertEquals(ImList.on("the file permissions do not allow it to be written"), result.left);
    }

    @Test
    public void testMakeFileFailsWhenFileReadOnly()
    {
        Path readOnlyFile = createTempFile();
        FileUtil.setWritable(readOnlyFile, false);
        Chat<Path> result = makeFile(readOnlyFile, contents);
        assertEquals(ImList.on("the file permissions do not allow it to be written"), result.left);
    }

    @Test
    public void testMakeFiles()
    {
        Path root = createTempDir();

        FileUtil.setWritable(root, true);

        Path dir = getPath(root, "readOnly");
        FileUtil.createDir(dir);

        Path ok = getPath(root, "ok");

        // Set up the paths
        ImList<Path> paths = ImList.on(dir, ok, Path.of("/dev/tty"));

        // The content
        ImList<String> content = ImList.repeat("foo");

        // Zip together and make files
        Chat<ImList<Path>> chat = FileUtil.makeFiles(paths.zip(content));

        say(chat);

    }

    @Test
    public void testGetParentOfRoot()
    {
        assertEquals(ImList.on("the root directory does not have a parent"), getParent(Paths.get("/")).left);
        assertEquals(Paths.get("/"), getParent(Paths.get("/a")).right);
    }

    @Test
    public void testGetRealPath()
    {
        assertTrue(getRealPath(Paths.get("/")).isPresent());
        assertTrue(getRealPath(Paths.get("/..")).isPresent());
        assertFalse(getRealPath(Paths.get("/akjashdfkhasfdkjhg")).isPresent());
    }

    @Test
    public void testGetRealPathOnDot()
    {
        assertTrue(getRealPath(Paths.get(".")).isPresent());

    }

    @Test
    public void testGetRealPathOnDotParent()
    {
        say(getRealPath(Paths.get(".")).get().getParent());
    }

    @Test
    public void testOpenUnreadableFile()
    {
        Path p = createTempFile();
        setReadable(p, false);
        assertEquals(ImList.on("the file permissions do not allow it to be read"), readLines(p).left);
    }

    @Test
    public void testOpenNonExistentFile()
    {
        assertEquals(ImList.on("the file does not exist"), readLines(nonExistent).left);
    }

    @Test
    public void testOpenADirectoryAsAFile()
    {
        assertEquals(ImList.on("it is a directory - not a file"), readLines(tempDir).left);
    }

    @Test
    public void testSetReadable()
    {
        assertFalse(setReadable(nonExistent, true));
        assertFalse(setReadable(nonExistent, false));
        assertFalse(setReadable(Paths.get("/"), true));

        assertTrue(setReadable(tempFile, false));
        assertFalse(FileUtil.isReadable(tempFile));

        assertTrue(setReadable(tempFile, true));
        assertTrue(FileUtil.isReadable(tempFile));

        assertTrue(setReadable(tempDir, false));
        assertFalse(FileUtil.isReadable(tempDir));

        assertTrue(setReadable(tempDir, true));
        assertTrue(FileUtil.isReadable(tempDir));
    }

    @Test
    public void testSetWritable()
    {
        assertFalse(setWritable(nonExistent, true));
        assertFalse(setWritable(nonExistent, false));
        assertFalse(setWritable(Paths.get("/"), true));

        assertTrue(setWritable(tempFile, false));
        assertFalse(FileUtil.isWritable(tempFile));

        assertTrue(setWritable(tempFile, true));
        assertTrue(FileUtil.isWritable(tempFile));

        assertTrue(setWritable(tempDir, false));
        assertFalse(FileUtil.isWritable(tempDir));

        assertTrue(setWritable(tempDir, true));
        assertTrue(FileUtil.isWritable(tempDir));

    }

    @Test
    public void testResolve()
    {
        Path parentDir = Paths.get("/a/b");

        assertEquals("/a/b/c", resolve(parentDir, "c").toString());
        assertEquals("/a", resolve(parentDir, "..").toString());
        assertEquals("/a/d", resolve(parentDir, "../d").toString());
        assertEquals("/a/d", resolve(parentDir, "../d").toString());
        assertEquals("/", resolve(parentDir, "../../../../..").toString());
        assertEquals("/a/b", resolve(parentDir, ".").toString());
        assertEquals("/a/b", resolve(parentDir, "./////////./.").toString());
        assertEquals("/c", resolve(parentDir, "/c").toString());
    }

    @Test
    public void testCurrentDir()
    {
        assertEquals(resolve(Paths.get("."), ""), cwd());
    }

    @Test
    public void testGetPath()
    {

        /**
         *
         * So there are a few Unicode code points that are not valid as file names
         * 0 is the easiest one to understand
         *
         * The other ones that I have found that cause Paths.get to throw are actually invalid Unicode characters
         *
         * Basic multilingual plane is the set of characters in the range:
         *
         * 0 - 2^16 -1
         * 0 - 65535
         * 0 - ffff
         *
         *
         * https://en.wikibooks.org/wiki/Unicode/Character_reference/D000-DFFF
         *
         *   Unicode range D800–DFFF is used for surrogate pairs in UTF-16 (used by Windows) and CESU-8
         *   transformation formats, allowing these encodings to represent the
         *   supplementary plane code points, whose values are too large to fit in 16 bits. A pair of 16-bit
         *   code points — the first from the high surrogate area (D800–DBFF),
         *   and the second from the low surrogate area (DC00–DFFF) — are combined to form a 32-bit code point
         *   from the supplementary planes.
         *   Unicode and ISO/IEC 10646 do not assign actual characters to any of the code points in the
         *   D800–DFFF range — these code points only have meaning when used
         *   in surrogate pairs. Hence an individual code point from a surrogate pair does not represent a
         *   character, is invalid unless used in a surrogate pair, and is
         *   unconditionally invalid in UTF-32 and UTF-8 (if strict conformance to the standard is applied).⏎
         *
         */

        //                                        0  d800   d868   d8ff
        ImList<Integer> badCodePoints = ImList.on(0, 55296, 55400, 55551);
        //ImList<String> chars = ImRange.inclusive(64000, 128000).map(i -> Character.valueOf((char) i.shortValue()).toString());

        ImList<String> chars = badCodePoints.map(i -> Character.valueOf((char) i.shortValue()).toString());
        ImList<Integer> bad = chars.map(s -> getMinusOneOrCodePointIfError(s)).filter(n -> n >= 0);

        System.out.println(bad.map(i -> i.toHexString(i)).getTopDownBox());
    }

    @Test
    public void testGetLongPath()
    {
        String s = "1".repeat(256);

        Path path = Paths.get(s);

        System.out.println(path.toString().length());

        System.out.println(FileUtil.createDir(path));

        System.out.println(FileUtil.cwd());
    }

    @Test
    public void testCreateEmptyDirPath()
    {
        Path path = Paths.get("");

        Chat<Path> dir = FileUtil.createDir(path);
        assertTrue(dir.isOk());

        System.out.println(dir);
        System.out.println(FileUtil.unique(path));

        FileUtil.assertPathExists(path);

        assertTrue(FileUtil.exists(path));
    }

    @Test
    public void testUnique()
    {
        // Basic creation allows a path to be specified with arbitrary parts of the path
        Path path = Paths.get("/a", "/b/" + "c/d");

        assertEquals("/a/b/c/d", path.toString());

        // Get the current working dir so we can assert some stuff
        Path cwd = cwd();
        say(cwd);

        // A relative path
        String rel = "a/b/c/d";
        Path path4 = Paths.get(rel);

        // The unique path should have added the relative path to cwd
        Path path4AbsNorm = FileUtil.unique(path4);
        assertEquals(cwd.getNameCount() + path4.getNameCount(), path4AbsNorm.getNameCount());

        // and it should look like this
        assertEquals(FileUtil.getPath(cwd, rel), path4AbsNorm);
        assertEquals(cwd.toAbsolutePath() + "/" + rel, path4AbsNorm.toString());

        // normalising should remove all the duplication and relative stuff
        Path path2 = Paths.get("////a/../a//b/./c/d/..//////..");
        say(FileUtil.unique(path2));

        assertEquals(FileUtil.getPath("/a/b"), FileUtil.unique(path2));
    }

    @Test
    public void testPathsGet()
    {
        //        Path path1 = Paths.get("...");
        //        Path unique = FileUtil.unique(path1);
        //        say("unique", unique);
        //        say(FileUtil.makeFile(unique, ""));

        String tooLong = TextUtils.join(ImList.repeat("0123456789", 30), "");

        Path tooLongPath = Paths.get(tooLong);

        say(tooLongPath);

        TestUtils.assertThrows(() -> Paths.get(String.valueOf(Character.valueOf((char) 0))), InvalidPathException.class);

        Path p = Paths.get("");

        say("p", p);

        say(FileUtil.makeFile(p, ""));

    }

    @Test
    public void testCreateEmptyFilePathFails()
    {
        Path path = Paths.get("/");

        assertEquals("/ - the name refers to an existing directory", makeFile(path, "").left.head());

    }

    private int getMinusOneOrCodePointIfError(String s)
    {
        try
        {
            Paths.get(s);
            return -1;
        } catch (InvalidPathException e)
        {
            return s.codePointAt(0);
        }
    }

    @Test
    public void testReadZeroLinesFromUnreadableDir()
    {
        setReadable(tempDir, false);

        Chat<ImList<String>> chat = FileUtil.readLines(0, tempDir);

        assertEquals(false, chat.isOk());
        assertEquals(" is not readable", chat.getChatString());

        setReadable(tempDir, true);
    }

    @Test
    public void testReadZeroLinesFromUnreadableFile()
    {
        setReadable(tempFile, false);

        Chat<ImList<String>> chat = FileUtil.readLines(0, tempFile);

        assertEquals(false, chat.isOk());
        assertEquals(" is not readable", chat.getChatString());
    }

    @Test
    public void testReadZeroLinesFromOkFile()
    {
        Chat<ImList<String>> chat = FileUtil.readLines(0, tempFile);

        assertEquals(true, chat.isOk());
        assertEquals(ImList.on(), chat.right);
    }

    @Test
    public void testReadOneLineFromOkFile()
    {
        Chat<ImList<String>> chat = FileUtil.readLines(1, tempFile);

        assertEquals(true, chat.isOk());
        assertEquals(ImList.on("stuff"), chat.right);
    }

    @Test
    public void testReadLinesFromOkFile()
    {

        Path temp = createTempFile();
        contents = "line one\nline two";
        makeFile(temp, contents);

        Chat<ImList<String>> chat = FileUtil.readLines(1, temp);
        assertEquals(ImList.on("line one"), chat.right);

        Chat<ImList<String>> chat2 = FileUtil.readLines(2, temp);
        assertEquals(ImList.on("line one", "line two"), chat2.right);

        Chat<ImList<String>> chat3 = FileUtil.readLines(3, temp);
        assertEquals(ImList.on("line one", "line two"), chat3.right);
    }

    @Test
    public void testReadLinesFromOkFileWithNl()
    {
        Path temp = createTempFile();
        contents = "line one\nline two\n";
        makeFile(temp, contents);

        Chat<ImList<String>> chat = FileUtil.readLines(1, temp);
        assertEquals(ImList.on("line one"), chat.right);

        Chat<ImList<String>> chat2 = FileUtil.readLines(2, temp);
        assertEquals(ImList.on("line one", "line two"), chat2.right);

        Chat<ImList<String>> chat3 = FileUtil.readLines(3, temp);
        assertEquals(ImList.on("line one", "line two"), chat3.right);
    }

    @Test
    public void testReadLinesFromEmptyFile()
    {
        Path temp = createTempFile();
        contents = "";
        makeFile(temp, contents);

        Chat<ImList<String>> chat = FileUtil.readLines(100, temp);
        assertEquals(ImList.on(), chat.right);
    }

    @Test
    public void testReadLinesWithNegArg()
    {
        try
        {
            Chat<ImList<String>> chat = FileUtil.readLines(-1, tempFile);
            TestUtils.failExpectedException(ArgumentShouldNotBeLessThan.class);
        } catch (Exception e)
        {
        }
    }

    @Test
    public void testURI()
    {

        String projectFileName = "/abcd-dir/a/jadle.config/abcd.project";
        Say.say(FileUtil.getPath(projectFileName).toUri().toString());
        Say.say(ImUrl.on(FileUtil.getPath(projectFileName).toUri().toString()));
    }

    //    @Test
    //    public void testMemoryfs() throws IOException
    //    {
    //        try (FileSystem fs = EphemeralFsFileSystemBuilder.unixFs().build())
    //        {
    //
    //            Path foo = fs.getPath("/foo");
    //
    //            say("provider", foo.getFileSystem().provider());
    //
    //            FileUtil.createDir(foo);
    //
    //            FileUtil.assertFileIsADirectory(foo);
    //
    //            PosixFileAttributes attrs = Files.getFileAttributeView(foo, PosixFileAttributeView.class).readAttributes();
    //            Say.say("isRegularFile", attrs.isRegularFile());
    //            Say.say("isDirectory", attrs.isDirectory());
    //            Say.say("size", attrs.size());
    //            Say.say("perms", attrs.permissions());
    //
    //            Say.say("owner", attrs.owner().getName());
    //            Say.say("group", attrs.group().getName());
    //            Say.say("perms", PosixFilePermissions.toString(attrs.permissions()));
    //
    //            Files.setPosixFilePermissions(foo, PosixFilePermissions.fromString("r--------"));
    //
    //            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(foo);
    //
    //            Say.say("perms", perms);
    //
    //            HashSet<PosixFilePermission> newPerms = new HashSet<>(perms);
    //
    //            newPerms.remove(PosixFilePermission.OWNER_READ);
    //
    //            Files.setPosixFilePermissions(foo, newPerms);
    //
    //            Say.say("perms after", Files.getPosixFilePermissions(foo));
    //
    //            //            FileUtil.setWritable(foo, false);
    //        }
    //    }

}