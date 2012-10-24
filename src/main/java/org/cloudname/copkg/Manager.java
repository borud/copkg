package org.cloudname.copkg;

import java.io.File;

/**
 * Package Manager.
 *
 * @author borud
 */
public class Manager {
    private final String basePackageDir;

    /**
     * Create a package manager for a given base package directory.
     *
     * @param basePackageDir the base directory for where packages are
     *   installed.
     */
    public Manager (String basePackageDir) {
        this.basePackageDir = basePackageDir;
    }

    /**
     * Download, verify, unpack and verify installed
     */
    public void install(PackageCoordinate coordinate) {
    }

    public void download(PackageCoordinate coordinate) {
    }

    public void verifyPackage(File packageFile) {
    }

    public void uninstall(PackageCoordinate coordinate) {
    }
}