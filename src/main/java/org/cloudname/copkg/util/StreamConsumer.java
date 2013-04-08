package org.cloudname.copkg.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Move data from an InputStream to an OutputStream allowing some
 * maximum number of bytes to pass through.
 *
 * @author borud
 */
public class StreamConsumer implements Runnable {
    private static final Logger log = Logger.getLogger(StreamConsumer.class.getName());

    private static final int BUFFER_SIZE = 64 * 1024;
    private final InputStream in;
    private final OutputStream out;
    private final int maxRead;
    private int byteCount = 0;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final Set<Listener> listeners= new TreeSet<Listener>();

    /**
     * Listener interface for notifications about StreamConsumer
     * state.
     */
    public static interface Listener {
        public enum Status {
            DONE, GOT_EXCEPTION, MAX_READ
        };

        /**
         * On notification.
         *
         * @param consumer the StreamConsumer instance in question.
         * @param status the status we wish to report.
         */
        public void onNotify(StreamConsumer consumer, Status status);
    }

    /**
     * @param in the InputStream we want to read from.
     * @param out the OutputStream we wish to write to.
     * @param maxRead maximum number of bytes to pump.  If this is 0 or less there is no maximum.
     */
    public StreamConsumer(final InputStream in, final OutputStream out, final int maxRead) {
        this.in = in;
        this.out = out;
        this.maxRead = maxRead;
    }

    /**
     * Add a Listener.  The listener is called whenever the
     * number of bytes read and written exceeds the maximum number of
     * bytes as specified in the constructor arguments.
     *
     * @param listener the Listener we wish to add
     */
    public StreamConsumer addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Loop over listeners and notify them of the status given.
     *
     * @param status the status we wish to notify listeners of.
     */
    private void notifyListeners(Listener.Status status) {
        for (Listener listener : listeners) {
            try {
                listener.onNotify(this, status);
            } catch (Exception e) {
                // We should continue on even though we get an
                // exception here since this is a callback and there
                // might still be listeners that are in a valid state.
                log.log(Level.WARNING, "Got exception while notifying", e);
            }
        }
    }

    @Override
    public void run() {
        int n = 0;
        try {
            while((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                byteCount += n;

                // Skip byteCount check if the max limit is 0 or less
                if (maxRead <= 0) {
                    continue;
                }

                // Check if we have exceeded max size
                if (byteCount > maxRead) {
                    notifyListeners(Listener.Status.MAX_READ);
                    return;
                }
            }

            // invariant: read() returned -1
            notifyListeners(Listener.Status.DONE);
        } catch (IOException e) {
            log.log(Level.WARNING, "Got exception", e);
            notifyListeners(Listener.Status.GOT_EXCEPTION);
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                // Ignore.  Nothing we can do about this.
                log.log(Level.WARNING, "Got exception while closing streams", e);
            }
        }
    }
}