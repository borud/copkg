package org.cloudname.copkg;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.resumable.ResumableAsyncHandler;
import com.ning.http.client.resumable.ResumableListener;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Package Manager.
 *
 * @author borud
 */
public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());

    // Name of download directory relative to basePackageDir
    private static final String DOWNLOAD_DIR = ".download";

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
    public void download(PackageCoordinate coordinate) throws IOException {
        // TODO(borud) Replace this with some code that actually puts
        //   the file in a full pathFragment because if we do it this
        //   way two packages with the same artifact name and version
        //   but different groups will clash.
        ensureDownloadDirExists();
        String filename = downloadDir + "/" + coordinate.getFilename();

        final AsyncHttpClient c = new AsyncHttpClient();
        final RandomAccessFile file = new RandomAccessFile(filename, "rw" );
        final ResumableAsyncHandler a = new ResumableAsyncHandler();
        a.setResumableListener( new ResumableListener() {

                @Override
                public void onBytesReceived(ByteBuffer byteBuffer) throws IOException {
                    file.seek(file.length() );
                    file.write(byteBuffer.array() );
                }

                @Override
                public void onAllBytesReceived() {
                    try {
                        file.close();
                    } catch (IOException e) {
                        // TODO(borud): clean up
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public long length() {
                    try {
                        return file.length();
                    } catch (IOException e) {
                        // TODO(borud): clean up
                        throw new RuntimeException(e);
                    }
                }
            });

        final String url = coordinate.toUrl(baseUrl);

        log.info("Downloading " + url);

        ListenableFuture<Response> r = c.prepareGet(url).execute(a);
        try {
            Response response = r.get();
            if (response.getStatusCode() != 200) {

                // Results in empty file.  Remove it
                File emptyFile = new File(filename);
                if (emptyFile.length() == 0L) {
                    emptyFile.delete();
                }

                // TODO(borud): clean up!
                throw new RuntimeException("Error: " + response.toString());
            }
        } catch (InterruptedException e) {
            // TODO(borud): clean up
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // TODO(borud): clean up
            throw new RuntimeException(e);
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
