package org.cloudname.copkg;

import org.cloudname.copkg.util.Unzip;

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
 * Package Manager - provides methods for downloading, unpacking and
 * installing copkg packages.
 *
 * @author borud
 */
public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());

    private static final int REQUEST_TIMEOUT_MS = (5 * 60 * 1000);
    private static final int MAX_RETRY_ON_IOEXCEPTION = 5;
    private static final int MAX_CONNECTIONS_PER_HOST = 3;
    private static final int MAX_NUM_REDIRECTS = 3;

    private Configuration config;

    /**
     * Create a package manager for a given base package directory.
     *
     * @param config the configuration for the package manager.
     */
    public Manager (final Configuration config) {
        this.config = config;
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
        final String downloadFilename = config.downloadFilenameForCoordinate(coordinate);

        final File destinationFile = new File(downloadFilename);
        final File destinationDir = destinationFile.getParentFile();

        // Ensure directories exist
        destinationDir.mkdirs();

        final String url = coordinate.toUrl(config.getPackageBaseUrl());

        log.info("destination dir  = " + destinationDir.getAbsolutePath());
        log.info("destination file = " + destinationFile.getAbsolutePath());

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
            destinationFile.delete();
            log.warning("Download failed. Status = " + response.getStatusCode() + ", msg = " + response.getStatusText());
        }
        return response.getStatusCode();
    }

    /**
     * Download, verify, unpack and verify installed
     */
    public void install(PackageCoordinate coordinate) {
        // Verify zip-file
        // Unzip into destination dir
        // verify
        // move into place
    }
}
