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

    @Test public void testSimple() {
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
    }
}