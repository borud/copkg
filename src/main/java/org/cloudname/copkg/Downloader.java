package org.cloudname.copkg;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.AsyncHttpClientConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Very simple synchronous downloader.
 *
 * @author borud
 */
public class Downloader {
    private static final Logger log = Logger.getLogger(Downloader.class.getName());

    private static final String USER_AGENT = "copkg/1";

    // Initial connect timeout a bit low in order to back off faster.
    private static final int CONNECTION_TIMEOUT_MS = (15 * 1000);

    // When we are connected and things are slow we might as well wait.
    private static final int REQUEST_TIMEOUT_MS = (5 * 60 * 1000);

    private static final int MAX_RETRY_ON_IOEXCEPTION = 5;
    private static final int MAX_CONNECTIONS_PER_HOST = 3;
    private static final int MAX_NUM_REDIRECTS = 3;
    private final AsyncHttpClient client;

    /**
     * Result of download.
     */
    public static class Result {
        private final File file;
        private final int code;

        public Result(final File file, final int code) {
            this.file = file;
            this.code = code;
        }

        /**
         * @return a File instance pointing to the downloaded file or
         *   {@code null} if download failed.
         */
        public File getFile() {
            return file;
        }

        /**
         * @return the http code.
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * Configure and construct the downloader.
     */
    public Downloader() {
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
            .setCompressionEnabled(true)
            .setConnectionTimeoutInMs(CONNECTION_TIMEOUT_MS)
            .setRequestTimeoutInMs(REQUEST_TIMEOUT_MS)
            .setFollowRedirects(true)
            .setMaximumNumberOfRedirects(MAX_NUM_REDIRECTS)
            .setAllowPoolingConnection(true)
            .setMaxRequestRetry(MAX_RETRY_ON_IOEXCEPTION)
            .setMaximumConnectionsPerHost(MAX_CONNECTIONS_PER_HOST)
            .setUserAgent(USER_AGENT)
            .build();
        client = new AsyncHttpClient(config);
    }

    /**
     * Download a file into a specified destination.
     *
     * @param url the URL we wish to download.
     * @param destination the full path of the file we want to download into.
     */
    public void download(final String url, final String destination)
        throws IOException,
               InterruptedException,
               ExecutionException
    {
        final OutputStream outputStream = new FileOutputStream(destination);

        AsyncHandler<File> asyncHandler = new AsyncHandler<File>() {
            @Override
            public AsyncHandler.STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) {
                try {
                    bodyPart.writeTo(outputStream);
                } catch (IOException e) {
                    // Abort the download if we had an error writing
                    // the body part to file since it is unlikely we
                    // can continue.
                    new File(destination).delete();
                    return AsyncHandler.STATE.ABORT;
                }
                return AsyncHandler.STATE.CONTINUE;
            }

            @Override
            public File onCompleted() {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // There isn't really a lot we can do at this
                    // point.  Might as well just swallow the
                    // exception and hope the file is okay.  If it
                    // isn't okay that is something that has to be
                    // determined during the verification phase.
                    // Deleting the file might not be what the user
                    // wants so we leave it alone.
                    log.log(Level.WARNING, "Got IOException", e);
                }
                return new File(destination);
            }

            @Override
            public AsyncHandler.STATE onHeadersReceived(HttpResponseHeaders headers) {
                return AsyncHandler.STATE.CONTINUE;
            }

            @Override
            public AsyncHandler.STATE onStatusReceived(HttpResponseStatus responseStatus) {
                // TODO(borud): need to test what happens when we get
                //   a redirect.  The below code might actually abort
                //   following a redirect and that is not what we
                //   want.
                if (responseStatus.getStatusCode() != 200) {
                    return AsyncHandler.STATE.ABORT;
                }
                return AsyncHandler.STATE.CONTINUE;
            }

            @Override
            public void onThrowable(Throwable t) {
            }
        };

        // Fire in the hole!
        client.prepareGet(url).execute(asyncHandler).get();
    }
}
