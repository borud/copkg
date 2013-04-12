package org.cloudname.copkg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test for StreamConsumer.
 *
 * @author borud
 */
public class StreamConsumerTest {
    private static final int NUM_BYTES = 20 * 1024;
    private final InputStream in = new ByteArrayInputStream(makeByteArray(NUM_BYTES));

    /**
     * Consume entire input without overrun.
     */
    @Test
    public void simpleConsumeTest() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(NUM_BYTES);
        final CountDownLatch done = new CountDownLatch(1);

        final StreamConsumer consumer = new StreamConsumer(in, out, NUM_BYTES);
        consumer.addListener(new StreamConsumer.Listener() {
                @Override
                public void onNotify(StreamConsumer c, Status s) {
                    assertSame(s, StreamConsumer.Listener.Status.DONE);
                    done.countDown();
                }
            });

        new Thread(consumer).start();

        // Give it 100 milliseconds to finish
        assertTrue(done.await(100L, TimeUnit.MILLISECONDS));
        assertEquals(NUM_BYTES, out.size());
    }

    /**
     * Consume input and overrun limit
     *
     */
    @Test
    public void consumeOverrun() throws Exception {
        final OutputStream out = new ByteArrayOutputStream(NUM_BYTES);

        // Instruct consumer to consume one byte less than available,
        // thus triggering overrun.
        final int numBytesToRead = NUM_BYTES - 1;

        final StreamConsumer consumer = new StreamConsumer(in, out, numBytesToRead);
        consumer.addListener(new StreamConsumer.Listener() {
                @Override
                public void onNotify(StreamConsumer c, Status s) {
                    assertSame(s, StreamConsumer.Listener.Status.MAX_READ);
                }
            });

        new Thread(consumer).start();
    }

    /**
     * Make array of specified size and fill it recognizable data.
     */
    private static byte[] makeByteArray(int size) {
        byte[] data = new byte[size];
        Arrays.fill(data, (byte)'a');
        return data;
    }
}