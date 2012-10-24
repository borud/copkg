package org.cloudname.copkg;

import java.io.File;
import java.net.URL;

/**
 * Package coordinates borrow their structure, and rough semantics,
 * from Maven coordinates, however this class offers a very simple
 * version.
 *
 * @author borud
 */
public class PackageCoordinate {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String groupIdUrlPath;
    private final String groupIdPath;

    /**
     * Constructor for PackageCoordinate.
     *
     * @param groupId the group id of the artifact
     * @param artifactId the artifact id of the artifact
     * @param version the version of the artifact
     */
    public PackageCoordinate(final String groupId, final String artifactId, final String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        groupIdPath = groupId.replaceAll("\\.", "" + File.separatorChar);
        groupIdUrlPath = groupId.replaceAll("\\.", "/");
    }

    /**
     * @return the group id of the package
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the artifact id of the package
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return the version of the package.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return base filename for package coordinate.
     */
    public String getBaseFilename() {
        return artifactId + "-" + version + "-copkg";
    }

    /**
     * @return filename of package.
     */
    public String getFilename() {
        return getBaseFilename() + ".zip";
    }

    /**
     * @return directory fragment of package for use on filesystems.
     */
    public String getPathFragment() {
        return groupIdPath + File.separatorChar + artifactId + File.separatorChar + version;
    }

    /**
     * @return directory fragment of package for use in URLs
     */
    public String getUrlPathFragment() {
        return groupIdUrlPath + "/" + artifactId + "/" + version;
    }

    public String toString() {
        return asString();
    }

    /**
     * Parseable String format.
     */
    public String asString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}