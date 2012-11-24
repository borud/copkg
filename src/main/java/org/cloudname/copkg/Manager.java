package org.cloudname.copkg;

import com.ning.http.client.Response;
import com.ning.http.client.SimpleAsyncHttpClient;
import com.ning.http.client.consumers.FileBodyConsumer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.concurrent.Future;

/**
 * Package Manager.
 *
 * @author borud
 */
public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());

    // Name of download directory relative to basePackageDir
    private static final String DOWNLOAD_DIR = ".download";

    private static final int REQUEST_TIMEOUT_MS = (5 * 60 * 1000);
    private static final int MAX_RETRY_ON_IOEXCEPTION = 5;
    private static final int MAX_CONNECTIONS_PER_HOST = 3;
    private static final int MAX_NUM_REDIRECTS = 3;

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
     * Download package into the download directory.
     *
     * <p>For library use this method needs a better API for
     * communicating back a bit more than just the return code.
     *
     * @param coordinate Package Coordinate of the package we wish to download.
     */
    public int download(PackageCoordinate coordinate) throws Exception {
        final String destinationDir = destinationDirForCoordinate(coordinate);
        final String destinationFile = destinationFileForCoordinate(coordinate);

        // Ensure directories exist
        new File(destinationDir).mkdirs();
        final String url = coordinate.toUrl(baseUrl);

        log.info("destination dir  = " + destinationDir);
        log.info("destination file = " + destinationFile);

        // Make client
        SimpleAsyncHttpClient client = new SimpleAsyncHttpClient.Builder()
            .setRequestTimeoutInMs(REQUEST_TIMEOUT_MS)
            .setFollowRedirects(true)
            .setMaximumNumberOfRedirects(MAX_NUM_REDIRECTS)
            .setMaxRequestRetry(MAX_RETRY_ON_IOEXCEPTION)
            .setMaximumConnectionsPerHost(MAX_CONNECTIONS_PER_HOST)
            .setUrl(url)
            .build();

        Response response = client.get(new FileBodyConsumer(new RandomAccessFile(destinationFile, "rw"))).get();

        // If the response code indicates anything other than 200 we
        // will end up with a file that contains junk.  We have to
        // make sure we delete it.
        if (response.getStatusCode() != 200) {
            new File(destinationFile).delete();
            log.warning("Download failed. Status = " + response.getStatusCode() + ", msg = " + response.getStatusText());
        }
        return response.getStatusCode();
    }

    public void verifyPackage(File packageFile) {
    }

    public void uninstall(PackageCoordinate coordinate) {
    }

    /**
     * The destination path inside the download directory for the coordinate.
     *
     * @param coordinate the package coordinate
     * @return the destination directory name for the package when downloaded.
     */
    public String destinationDirForCoordinate(PackageCoordinate coordinate) {
        return downloadDir
            + File.separatorChar
            + coordinate.getPathFragment();
    }

    /**
     * Destination file path inside the download directory for the coordinate.
     * @param coordinate the package coordinate.
     * @return the destination file name for the package when downloaded.
     */
    public String destinationFileForCoordinate(PackageCoordinate coordinate) {
        return destinationDirForCoordinate(coordinate) + File.separatorChar + coordinate.getFilename();
    }

}
