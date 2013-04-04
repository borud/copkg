package org.cloudname.copkg;

import com.ning.http.client.Response;

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
    private static Configuration config;
    private static StaticHttpServer httpServer;

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        port = Net.getFreePort();
        String baseUrl = "http://localhost:" + port;
        File packageFolder = testFolder.newFolder("packages");

        config = new Configuration(packageFolder.getAbsolutePath(), baseUrl, "foo", "bar");

        httpServer = new StaticHttpServer(port, "src/test/resources/staticroot");
        httpServer.start();
        log.info("Started HTTP server " + baseUrl);
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
        Manager m = new Manager(config);
        PackageCoordinate coordinate = PackageCoordinate.parse("com.example:artifact:1.2.3");
        int statusCode = m.download(coordinate);
        assertEquals(200, statusCode);

        File downloadedFile = new File(config.downloadFilenameForCoordinate(coordinate));
        assertTrue("File did not exist", downloadedFile.exists());
        assertTrue("File had zero length", downloadedFile.length() != 0L);
    }

    /**
     * Test downloading a file that does not exists on the server.
     */
    @Test
    public void testDownload404() throws Exception {
        Manager m = new Manager(config);
        PackageCoordinate coordinate = PackageCoordinate.parse("com.example:nonexist:1.2.3");
        int statusCode = m.download(coordinate);
        assertEquals(404, statusCode);

        File downloadedFile = new File(config.downloadFilenameForCoordinate(coordinate));
        assertFalse("File existed", downloadedFile.exists());
    }

    @Test
    public void testInstallUninstall() throws Exception {
        PackageCoordinate coordinate = PackageCoordinate.parse("com.example:artifact:1.2.3");
        Manager m = new Manager(config);
        m.install(coordinate);

        String installDir = config.getPackageDir() + File.separatorChar + coordinate.getPathFragment();
        assertTrue(new File(installDir).exists());
        assertTrue(new File(installDir + File.separatorChar + "bin").exists());
        assertTrue(new File(installDir + File.separatorChar + "etc").exists());
        assertTrue(new File(installDir + File.separatorChar + "lib").exists());
        assertTrue(new File(installDir + File.separatorChar + "script.d").exists());

        // Now uninstall the package
        m.uninstall(coordinate);
        assertFalse(new File(installDir).exists());
    }


    /**
     * Test for isInstalled() predicate.
     */
    @Test
    public void testIsInstalled() throws Exception {
        Manager m = new Manager(config);
        PackageCoordinate coordinate = PackageCoordinate.parse("com.example:artifact:1.2.3");
        assertFalse(m.isInstalled(coordinate));
        m.install(coordinate);
        assertTrue(m.isInstalled(coordinate));
    }

}
