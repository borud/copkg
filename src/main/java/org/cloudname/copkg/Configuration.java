package org.cloudname.copkg;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Configuration for the package manager.
 *
 * @author borud
 */
public class Configuration {
    // Name of download directory relative to packageDir
    public static final String DOWNLOAD_DIR = ".download";

    private String packageDir;
    private String downloadDir;
    private String packageBaseUrl;

    /**
     * Constructor for package manager configuration.
     *
     * @param packageDir the root of the package directory, where
     *    packages will be installed.
     * @param packageBaseUrl the base URL for the web server from
     *   which packages are distributed.
     */
    @JsonCreator
    public Configuration(@JsonProperty("packageDir") String packageDir,
                         @JsonProperty("packageBaseUrl") String packageBaseUrl)
    {
        this.packageDir = packageDir;
        this.packageBaseUrl = packageBaseUrl;

        // Populate this but don't touch filesystem
        downloadDir = packageDir + (packageDir.endsWith("/") ? "" : "/") + DOWNLOAD_DIR;
    }

    /**
     * @return the base URL for the HTTP server where packages are
     *   distributed from.
     */
    public String getPackageBaseUrl() {
        return packageBaseUrl;
    }

    /**
     * @return the directory into which packages are installed.
     */
    public String getPackageDir() {
        return packageDir;
    }

    /**
     * @return the directory used when downloading packages from the
     *   package distribution server.
     */
    @JsonIgnore // derived property
    public String getDownloadDir() {
        return downloadDir;
    }

    /**
     * Destination file path inside the download directory for the coordinate.
     *
     * @param coordinate the package coordinate.
     * @return the destination file name for the package when downloaded.
     */
    public String downloadFilenameForCoordinate(PackageCoordinate coordinate) {
        return downloadDir
            + File.separatorChar
            + coordinate.getPathFragment()
            + File.separatorChar
            + coordinate.getFilename();
    }

    /**
     * Convert configuration to JSON.
     *
     * @return JSON representation of the configuration.
     */
    public String toJson() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @return Configuration object based on JSON representation.
     */
    public static Configuration fromJson(String json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Configuration.class);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Configuration)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        final Configuration other = (Configuration) o;

        return (packageDir.equals(other.packageDir)
                && packageBaseUrl.equals(other.packageBaseUrl));
    }

    @Override
    public int hashCode() {
        return (packageDir.hashCode() * 23) ^  (packageBaseUrl.hashCode() * 37);
    }
}
