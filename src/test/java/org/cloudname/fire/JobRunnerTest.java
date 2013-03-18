package org.cloudname.fire;

import org.cloudname.copkg.Configuration;

import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for JobRunner
 *
 * @author borud
 */
public class JobRunnerTest {
    private static final String SERVICE_COORDINATE = "1.service.user.dc";
    private static final String PACKAGE_COORDINATE = "group:artifact:1.2.3";
    private static final String PACKAGE_DIR = "src/test/resources/scripts";
    private static final String PACKAGE_BASE_URL = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    // Job that should terminate with success
    private final static Job ok = new Job(SERVICE_COORDINATE,
                                          PACKAGE_COORDINATE,
                                          new HashMap<String,String>() {{
                                                  put("exit-success", null);
                                              }});

    // Job that should terminate with failure 99
    private final static Job fail99 = new Job(SERVICE_COORDINATE,
                                              PACKAGE_COORDINATE,
                                              new HashMap<String,String>() {{
                                                      put("exit-fail", "99");
                                                  }});

    // Job that should time out
    private final static Job timeoutJob = new Job(SERVICE_COORDINATE,
                                                  PACKAGE_COORDINATE,
                                                  new HashMap<String,String>() {{
                                                          put("sleep", "5000");
                                                      }});

    private final static Configuration config = new Configuration(PACKAGE_DIR,
                                                                  PACKAGE_BASE_URL,
                                                                  USERNAME,
                                                                  PASSWORD);
    @Test
    public void testJobRunner() throws Exception {
        JobRunner runner = new JobRunner(config);
        System.out.println(runner.runJob(ok));

    }
}
