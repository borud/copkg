package org.cloudname.copkg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.concurrent.ExecutionException;

/**
 * Package Manager.
 *
 * @author borud
 */
public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());

    // Name of download directory relative to basePackageDir
    private static final String DOWNLOAD_DIR = ".download";

    private final Downloader downloader = new Downloader();

    private final String basePackageDir;
    private final String downloadDir;
    private final String baseUrl;

    /**
     * Create a package manager for a given base package directory.
     *
     * @param basePackageDir the base directory for where packages are
     *   installed.
     */
    public Manager (final String basePackageDir, final String baseUrl) {
        this.basePackageDir = basePackageDir;
        this.baseUrl = baseUrl;

        // Populate this but don't touch filesystem yet
        downloadDir = basePackageDir
            + (basePackageDir.endsWith("/") ? "" : "/")
            + DOWNLOAD_DIR;
    }

    /**
     * Download, verify, unpack and verify installed
     */
    public void install(PackageCoordinate coordinate) {
    }

    /**
     * Download package
     */
    public void download(PackageCoordinate coordinate)
        throws IOException,
               InterruptedException,
               ExecutionException
    {
        // TODO(borud) Replace this with some code that actually puts
        //   the file in a full pathFragment because if we do it this
        //   way two packages with the same artifact name and version
        //   but different groups will clash.
        ensureDownloadDirExists();
        final String filename = downloadDir + "/" + coordinate.getFilename();
        final String url = coordinate.toUrl(baseUrl);

        downloader.download(url, filename);

        // If it goes wrong, remove the file.
        File emptyFile = new File(filename);
        if (emptyFile.length() == 0L) {
            emptyFile.delete();
        }
    }

    public void verifyPackage(File packageFile) {
    }

    public void uninstall(PackageCoordinate coordinate) {
    }

    /**
     * @return the download directory path.
     */
    public String getDownloadDir() {
        return downloadDir;
    }

    /**
     * Ensure that the download dir exists.
     */
    private void ensureDownloadDirExists() {
        File d = new File(downloadDir);
        if (! d.exists()) {
            d.mkdir();
        }
    }
}
