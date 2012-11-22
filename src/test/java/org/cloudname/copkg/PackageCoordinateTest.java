package org.cloudname.copkg;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for PackageCoordinate class.
 *
 * @author borud
 */
public class PackageCoordinateTest {

    /**
     * Perform simple tests.
     */
    @Test
    public void testSimple() throws Exception {
        PackageCoordinate c = new PackageCoordinate("org.cloudname",
                                                    "something",
                                                    "1.2.3");
        // Silly getter tests
        assertEquals("org.cloudname", c.getGroupId());
        assertEquals("something", c.getArtifactId());
        assertEquals("1.2.3", c.getVersion());

        // filename tests
        assertEquals("something-1.2.3-copkg", c.getBaseFilename());
        assertEquals("something-1.2.3-copkg.zip", c.getFilename());

        // test path fragment methods
        assertEquals("org/cloudname/something/1.2.3", c.getUrlPathFragment());

        // path fragments will be different on Windows and other platforms
        // since the separator chars are different.
        final String pathFragment = "org"
            + File.separatorChar
            + "cloudname"
            + File.separatorChar
            + "something"
            + File.separatorChar
            + "1.2.3";
        assertEquals(pathFragment, c.getPathFragment());

        assertEquals("org.cloudname:something:1.2.3", c.asString());
        assertEquals("org.cloudname:something:1.2.3", PackageCoordinate.parse(c.asString()).asString());
        assertFalse(PackageCoordinate.parse("a:b:c").equals(PackageCoordinate.parse("b:c:d")));
    }

    /**
     * Test generation of URL from coordinate.
     */
    @Test
    public void testUrl() throws Exception {
        String baseUrl = "http://example.com/base/path";
        String fullUrl = "http://example.com/base/path/com/comoyo/someservice/2.4.11/someservice-2.4.11-copkg.zip";
        PackageCoordinate coordinate = PackageCoordinate.parse("com.comoyo:someservice:2.4.11");

        // Try out with and without trailing "/" to indicate directory.
        assertEquals(fullUrl, coordinate.toUrl(baseUrl));
        assertEquals(fullUrl, coordinate.toUrl(baseUrl + "/"));
    }
}
