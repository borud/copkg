package org.cloudname.copkg;

import com.google.common.testing.EqualsTester;

import java.util.Map;
import java.util.HashMap;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit tests for Job class.
 *
 * @author borud
 */
public class JobTest {
    private static final String serviceCoordinate = "1.service.user.dc";
    private static final String packageCoordinate = "com.example:artifact:2.2.1";
    private static final Map params = new HashMap() {{
        put("alpha-key", "alpha value");
        put("beta-key", "beta value");
        put("gamma-key", "gamma value");
        // option that takes parameter but is empty
        put("delta-key", "");
        // option that does not take parameter but is present
        put("9", null);
    }};

    private static final String[] PARAMS_AS_OPTION_ARRAY = {
        "--9",
        "--alpha-key=alpha value",
        "--beta-key=beta value",
        "--delta-key=",
        "--gamma-key=gamma value"
    };

    /**
     * Just a very simple test that proves we can serialize and then
     * parse what we have just serialized.
     */
    @Test
    public void testJson() throws Exception {
        Job job = new Job(serviceCoordinate, packageCoordinate, params);
        Job job2 = Job.parse(job.toJson());

        assertNotNull(job2);

        // We are cheating a bit to not have to implement equals() :-P
        assertEquals(job.toJson(), job2.toJson());
    }

    @Test
    public void testParamArray() throws Exception {
        Job job = new Job(serviceCoordinate, packageCoordinate, params);
        assertEquals(PARAMS_AS_OPTION_ARRAY, job.getOptionArray());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructNull() throws Exception {
        new Job(null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNull() throws Exception {
        Job.parse(null);
    }

    @Test
    public void testEquals() throws Exception {
        new EqualsTester()
            .addEqualityGroup(new Job("1.test.user.dc", "group:artifact:1.2.3", params),
                              new Job("1.test.user.dc", "group:artifact:1.2.3", params))
            .testEquals();


    }
}
