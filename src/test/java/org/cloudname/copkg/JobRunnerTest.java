package org.cloudname.copkg;

import org.cloudname.copkg.Configuration;

import java.util.HashMap;
import java.io.File;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/**
 * Unit tests for JobRunner
 *
 * @author borud
 */
public class JobRunnerTest {
    private static final Logger log = Logger.getLogger(JobRunnerTest.class.getName());

    private static final String SERVICE_COORDINATE = "1.service.user.dc";
    private static final String PACKAGE_COORDINATE = "com.example:artifact:2.3.4";
    private static final String BOGUS_PACKAGE_COORDINATE = "com.bogus:bogus:2.3.4";

    private static int port;
    private static StaticHttpServer httpServer;
    private static Configuration config;

    // Job that should terminate with success
    private final static Job ok = new Job(SERVICE_COORDINATE,
                                          PACKAGE_COORDINATE,
                                          new HashMap<String,String>() {{
                                                  put("runtime-dir", "/tmp");
                                                  put("exit-success", null);
                                              }});

    // Job that should terminate with success
    private final static Job notfound = new Job(SERVICE_COORDINATE,
                                                BOGUS_PACKAGE_COORDINATE,
                                                new HashMap<String,String>() {{
                                                        put("runtime-dir", "/tmp");
                                                        put("exit-success", null);
                                                    }});

    // Job that should terminate with failure 99
    private final static Job fail99 = new Job(SERVICE_COORDINATE,
                                              PACKAGE_COORDINATE,
                                              new HashMap<String,String>() {{
                                                      put("runtime-dir", "/tmp");
                                                      put("exit-fail", "99");
                                                  }});

    // Job that should time out
    private final static Job timeoutJob = new Job(SERVICE_COORDINATE,
                                                  PACKAGE_COORDINATE,
                                                  new HashMap<String,String>() {{
                                                          put("runtime-dir", "/tmp");
                                                          put("sleep", "5000");
                                                      }});

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    // It is a bit superflous to add the whole download-and-unpack
    // dance here, but it was just a few lines of code and it is
    // quick.  AND we want to make some integration tests here
    // eventually.
    @BeforeClass
    public static void setUpClass() throws Exception {
        port = Net.getFreePort();
        String baseUrl = "http://localhost:" + port;
        File packageFolder = testFolder.newFolder("packages");

        config = new Configuration(packageFolder.getAbsolutePath(), baseUrl, "foo", "bar");

        httpServer = new StaticHttpServer(port, "src/test/resources/staticroot");
        httpServer.start();
        log.info("Started HTTP server " + baseUrl);

        new Manager(config).install(PackageCoordinate.parse(PACKAGE_COORDINATE));
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        httpServer.shutdown();
    }

    @Test
    public void testStartJob() throws Exception {
        JobRunner runner = new JobRunner(config);
        Result result = runner.startJob(ok);
        assertSame(Result.Status.SUCCESS, result.getStatus());
        assertEquals(0, result.getExitCode());
    }
}
