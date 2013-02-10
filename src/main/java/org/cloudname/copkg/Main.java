package org.cloudname.copkg;

import java.io.File;
import java.io.IOException;

/**
 * Main class for the copkg package manager command line utility.
 *
 * @author borud
 */
public class Main {
    public static final String COPKG_CONFIG_FILE = "config.json";
    public static final String COPKG_USER_DIR = ".copkg";
    public static final String COPKG_ETC_DIR = "copkg";

    // Last ditch defaults
    public static final String COPKG_HOME_PACKAGE_DIR = "packages";
    public static final String COPKG_DEFAULT_PACKAGE_URL = "http://packages.skunk-works.no/copkg";

    private Manager manager;

    /**
     * Set up Main with appropriate configuration.
     */
    public Main(Configuration config) {
        manager = new Manager(config);
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("\nUsage: install <coordinate>\n");
            return;
        }

        // TODO(borud): add explicit overrides to config so that we
        // can specify config on the command line which will override
        // all other config.
        Configuration config = makeOrFindConfiguration();

        Main m = new Main(config);
        m.dispatch(args);
    }

    /**
     * Dispatch commands
     */
    private void dispatch(String[] args) {
        String command = args[0];

        if ("install".equals(command)) {
            String param = args[1];
            install(param);
            return;
        }

        if ("uninstall".equals(command)) {
            String param = args[1];
            uninstall(param);
            return;
        }


        if ("list".equals(command)) {
            list();
            return;
        }

        System.out.println("Command " + command + " not implemented");
        return;
    }


    /**
     * Install package.
     *
     * @param coordinateString the coordinate string of the package we
     *   wish to install.
     */
    private void install(String coordinateString) {
        PackageCoordinate coordinate = PackageCoordinate.parse(coordinateString);
        manager.install(coordinate);
    }

    /**
     * Uninstall package.
     *
     * @param coordinateString the coordinate string of the package we
     *   wish to uninstall.
     */
    private void uninstall(String coordinateString) {
        // TODO(borud): implement
    }

    /**
     * List installed packages.  Lists package coordinates.
     */
    private void list() {
        // TODO(borud): implement
    }

    /**
     * Find configuration or make an appropriate default
     * configuration.  Will look in user's home directory first, then
     * /etc and then finally revert to defaults.
     */
    private static Configuration makeOrFindConfiguration() throws IOException {
        // First we try the users home directory
        File home = new File(
            System.getProperty("user.home")
            + File.separatorChar
            + COPKG_USER_DIR
            + File.separatorChar
            + COPKG_CONFIG_FILE);
        if (home.exists()) {
            return Configuration.fromFile(home);
        }

        // ...then we try /etc (no need to use platform independent
        // separator chars here)
        File etc = new File("/etc/" + COPKG_ETC_DIR + "/" + COPKG_CONFIG_FILE);
        if (etc.exists()) {
            return Configuration.fromFile(etc);
        }

        // No configuration could be found so we fall back to defaults
        String packageDir = System.getProperty("user.home") + File.separatorChar + COPKG_HOME_PACKAGE_DIR;
        return new Configuration(packageDir, COPKG_DEFAULT_PACKAGE_URL);
    }
}
