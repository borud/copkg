package org.cloudname.copkg;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ListenableFuture;

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
    private final AsyncHttpClient client = new AsyncHttpClient();

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
                    return AsyncHandler.STATE.ABORT;
                }
                return AsyncHandler.STATE.CONTINUE;
            }

            @Override
            public File onCompleted() {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // There isn't really a lot we can do at this point.
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
