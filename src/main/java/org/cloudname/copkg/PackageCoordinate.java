package org.cloudname.copkg;

import java.io.File;
import java.net.URL;

/**
 * Package coordinates borrow their structure, and rough semantics,
 * from Maven coordinates, however this class offers a simplified
 * version of Maven coordinates.
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

    /**
     * Given a base URL this method produces the download URL for this
     * package.  We put this here since this is an integral part of
     * how package coordinates should be used.
     *
     * TODO(borud): use Strings or URL type?
     *
     * @param baseUrl the base URL of the software distribution service.
     * @return
     */
    public String toUrl(final String baseUrl) {
        return baseUrl
            + (baseUrl.endsWith("/") ? "" : "/")
            + getUrlPathFragment()
            + "/"
            + getFilename();
    }

    /**
     * Parse coordinate and return (new) instance.
     *
     * @param coordinate package coordinate of the form
     *  "org.example:artifact:1.2.3"
     * @return a new instance
     */
    public static PackageCoordinate parse(String coordinate) {
        String[] parts = coordinate.split(":", 3);

        // We make no attempt to validate the coordinate -- we leave
        // that to the constructor.
        return new PackageCoordinate(parts[0],parts[1],parts[2]);
    }

    @Override
    public boolean equals(Object other) {
        return asString().equals(other);
    }
}
