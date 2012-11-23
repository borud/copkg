package org.cloudname.copkg;

import java.io.File;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/**
 * Unit test for Package Manager class.
 *
 *
 * @author borud
 */
public class ManagerTest {
    private static final Logger log = Logger.getLogger(ManagerTest.class.getName());

    private static int port;
    private static String baseUrl;
    private static File packageFolder;
    private static StaticHttpServer httpServer;


    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        port = Net.getFreePort();
        baseUrl = "http://localhost:" + port;
        packageFolder = testFolder.newFolder("packages");

        httpServer = new StaticHttpServer(port, "src/test/resources/staticroot");
        httpServer.start();
        log.info("Started HTTP server on port " + port);
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        httpServer.shutdown();
    }

    /**
     * Test downloading a file that exists on the server.
     */
    @Test
    public void testDownloadOk() throws Exception {
        Manager m = new Manager(packageFolder.getAbsolutePath(), baseUrl);
        PackageCoordinate coordinate = PackageCoordinate.parse("com.example:artifact:1.2.3");
        m.download(coordinate);

        log.info("File is now in " + packageFolder.getAbsolutePath());
        File downloadedFile = new File(m.getDownloadDir(), coordinate.getFilename());
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.length() != 0L);
    }
}
