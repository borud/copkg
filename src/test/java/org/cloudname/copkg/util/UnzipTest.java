package org.cloudname.copkg.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/**
 * Unit tests for Unzip class.
 *
 * @author borud
 */
public class UnzipTest {
    private static final String TEST_ZIP_FILE = "src/test/resources/zip/ziptest.zip";
    private static final String BOGUS_ZIP_FILE = "src/test/resources/zip/does-not-exist.zip";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Unpack a sample ZIP file and verify that all paths are present
     * in the destination directory.
     */
    @Test
    public void testSimple() throws Exception {
        File destination = testFolder.newFolder("dest");
        Unzip.unzip(new File(TEST_ZIP_FILE), destination);

        // make sure all the files and directories are there
        ensureDir(destination, "ziptest");
        ensureDir(destination, "ziptest/dir");
        ensureDir(destination, "ziptest/dir/with_subdir");
        ensureDir(destination, "ziptest/empty_dir");
        ensureDir(destination, "ziptest/otherdir");

        ensureExists(destination, "ziptest/README.txt");
        ensureExists(destination, "ziptest/otherdir/otherfile.txt");
    }

    /**
     * If we give the Unzipper a bogus path it should complain.
     */
    @Test (expected = FileNotFoundException.class)
    public void testZipFileNotFound() throws Exception {
        File destination = testFolder.newFolder("notfound");
        Unzip.unzip(new File(BOGUS_ZIP_FILE), destination);
    }

    private void ensureExists(File prefix, String filename) {
        assertTrue(new File(prefix, filename).isFile());
    }

    private void ensureDir(File prefix, String filename) {
        assertTrue(new File(prefix, filename).isDirectory());
    }

}
