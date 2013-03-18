package org.cloudname.copkg;

import org.cloudname.copkg.util.LogSetup;
import org.cloudname.copkg.util.Argument;
import org.cloudname.copkg.util.ArgumentParser;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Main class for the copkg package manager command line utility.
 *
 * This is a command line utility so throwing exceptions willy-nilly
 * is just fine.
 *
 * @author borud
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static final String COPKG_CONFIG_FILE = "config.json";
    public static final String COPKG_USER_DIR = ".copkg";
    public static final String COPKG_ETC_DIR = "copkg";

    // Last ditch defaults
    public static final String COPKG_HOME_PACKAGE_DIR = "packages";
    public static final String COPKG_DEFAULT_PACKAGE_URL = "http://packages.skunk-works.no/copkg";

    // Command line options
    private static OptionParser optionParser = new OptionParser();

    private static OptionSpec<String> repository =
        optionParser.accepts("repository").withRequiredArg().ofType(String.class);

    private static OptionSpec<String> packageDir =
        optionParser.accepts("package-dir").withRequiredArg().ofType(String.class);

    private static OptionSpec<String> username =
        optionParser.accepts("username").withRequiredArg().ofType(String.class);

    private static OptionSpec<String> password =
        optionParser.accepts("password").withRequiredArg().ofType(String.class);

    private static OptionSpec<Void> help = optionParser.accepts("help").forHelp();

    private static OptionSet optionSet;

    private Manager manager;
    private Configuration config;

    /**
     * Set up Main with appropriate configuration.
     */
    public Main(Configuration config) {
        this.config = config;
        manager = new Manager(config);
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) throws Exception {
        optionSet = optionParser.parse(args);

        // if --help then bail at once
        if (optionSet.has(help)) {
            printHelp();
            return;
        }

        // Make logging usable for interactive users
        LogSetup.setup();

        // TODO(borud): add explicit overrides to config so that we
        // can specify config on the command line which will override
        // all other config.
        Configuration config = makeOrFindConfiguration();

        Main m = new Main(config);
        m.dispatch();
    }


    /**
     * We could have used the built-in help display in jopt-simple,
     * but this looks a bit clearer.
     */
    private static void printHelp() {
        System.out.println(
            "\n"
            + "Flags:\n"
            + "----------------------------------------------------------------------------------------------------\n"
            + "    --package-dir=<dir>   : where to install packages\n"
            + "    --repository=<dir>    : where to fetch packages from\n"
            + "    --state-dir=<dir>     : where to to keep state when daemon\n"
            + "    --port=<port number>  : which port to make REST interface available on\n"
            + "    --username=<username> : username used for BASIC auth at repository\n"
            + "    --password=<password> : password used for BASUC auth at repository\n"
            + "\n"
            + "Package commands:\n"
            + "----------------------------------------------------------------------------------------------------\n"
            + "  copkg [flags] install <package coordinate>   : install the package\n"
            + "  copkg [flags] uninstall <package coordinate> : uninstall the package\n"
            + "  copkg [flags] resolve <package coordinate>   : print paths and URLs for a given package coordinate\n"
            + "\n"
            + "Service lifecycle management:\n"
            + "----------------------------------------------------------------------------------------------------\n"
            + "  copkg [flags] start <package coordinate> --runtime-dir=<dir> -- [start params]\n"
            + "      : Start service with package coordinate in a given runtime directory.  Parameters\n"
            + "        to service are added after the \"--\" marker\n"
            + "\n"
            + "  copkg [flags] stop --runtime-dir=<dir>\n"
            + "      : Stop service with a given runtime directory\n"
            + "\n"
            + "  copkg [flags] status --runtime-dir=<dir>\n"
            + "      : Show status for a service with a given runtime directory\n"
            + "\n"
            + "  copkg [flags] daemon [--state-dir=<dir> --port=<port number>]\n"
            + "      : Become daemon\n"
            + "\n"
        );
    }

    /**
     * Dispatch commands.
     *
     * <p>
     * This method is a bit more convoluted than one would think was
     * necessary.  However, this has to do with the fact that we are
     * going to handle commands as well as command line options that
     * are not parsed as command line options.  Fun, eh?
     */
    private void dispatch() throws Exception {
        // Deal with the non-option arguments
        List<Argument> arguments = ArgumentParser.parse(optionSet.nonOptionArguments().toArray(new String[] {}));

        // Make sure we at least have a command
        if (arguments.size() == 0) {
            printHelp();
            return;
        }

        // The first non-option argument is a command
        Argument command = arguments.remove(0);
        assert("".equals(command.getPrefix()));
        assert(command.getValue() == null);

        if ("install".equals(command.getOption())) {
            if (arguments.size() == 0) {
                System.out.println("\ninstall error: expected coordinate as argument");
                return;
            }

            Argument coordinateArgument = arguments.remove(0);
            assert("".equals(coordinateArgument.getPrefix()));
            assert(coordinateArgument.getValue() == null);
            install(coordinateArgument.getOption());
            return;
        }

        if ("uninstall".equals(command.getOption())) {
            if (arguments.size() == 0) {
                System.out.println("\nuninstall error: expected coordinate as argument");
                return;
            }

            Argument coordinateArgument = arguments.remove(0);
            assert("".equals(coordinateArgument.getPrefix()));
            assert(coordinateArgument.getValue() == null);
            uninstall(coordinateArgument.getOption());
            return;
        }

        if ("resolve".equals(command.getOption())) {
            if (arguments.size() == 0) {
                System.out.println("\nresolve error: expected coordinate as argument");
                return;
            }

            Argument coordinateArgument = arguments.remove(0);
            assert("".equals(coordinateArgument.getPrefix()));
            assert(coordinateArgument.getValue() == null);
            resolve(coordinateArgument.getOption());
            return;
        }

        // TODO(borud): implement.
        if ("start".equals(command.getOption())) {
            if (arguments.size() < 2) {
                System.out.println("\nstart: expected package coordinate and service coordinate");
                return;
            }

            Argument packageCoordinate = arguments.remove(0);
            assert("".equals(packageCoordinate.getPrefix()));
            assert(packageCoordinate.getValue() == null);

            Argument serviceCoordinate = arguments.remove(0);
            assert("".equals(serviceCoordinate.getPrefix()));
            assert(serviceCoordinate.getValue() == null);

            final Map<String,String> params = new HashMap<String,String>();
            for (Argument arg : arguments) {
                // The rest of the arguments here should be real arguments
                // that are prefixed by "--".  If they are not we kick up
                // a fuss.  It is better to try to force consistency now
                // than to try to reverse bad practices later.
                if (! "--".equals(arg.getPrefix())) {
                    throw new IllegalArgumentException("Argument did not start with '--', please be consistent");
                }

                params.put(arg.getOption(), arg.getValue());
            }

            final Job job = new Job(packageCoordinate.getOption(),
                                    serviceCoordinate.getOption(),
                                    params);

            // Run the job!
            final Result result = new JobRunner(config).runJob(job);
            System.out.println(result.toString());
            return;
        }

        // TODO(borud): implement.
        if ("stop".equals(command.getOption())) {
            System.out.println("\nNot implemented yet: " + command.getOption());
            printHelp();
            return;
        }

        // TODO(borud): implement.
        if ("status".equals(command.getOption())) {
            System.out.println("\nNot implemented yet: " + command.getOption());
            printHelp();
            return;
        }

        // TODO(borud): implement.
        if ("daemon".equals(command.getOption())) {
            System.out.println("\nNot implemented yet: " + command.getOption());
            printHelp();
            return;
        }

        System.out.println("\nUnknown command: " + command.getOption());

        printHelp();
    }


    /**
     * Install package.
     *
     * @param coordinateString the coordinate string of the package we
     *   wish to install.
     */
    private void install(String coordinateString) throws Exception {
        PackageCoordinate coordinate = PackageCoordinate.parse(coordinateString);
        manager.install(coordinate);
    }

    /**
     * Uninstall package.
     *
     * @param coordinateString the coordinate string of the package we
     *   wish to uninstall.
     */
    private void uninstall(String coordinateString) throws Exception {
        PackageCoordinate coordinate = PackageCoordinate.parse(coordinateString);
        manager.uninstall(coordinate);
    }

    /**
     * Given a coordinate, output the install path, the download path
     * etc given the current configuration.
     */
    private void resolve(String coordinateString) {
        PackageCoordinate coordinate = PackageCoordinate.parse(coordinateString);
        System.out.println("");
        System.out.println("installDir       = " + config.getPackageDir() + File.separatorChar + coordinate.getPathFragment());
        System.out.println("downloadUrl      = " + coordinate.toUrl(config.getPackageBaseUrl()));
        System.out.println("downloadFilename = " + config.downloadFilenameForCoordinate(coordinate));
        System.out.println("");
    }

    /**
     * Find configuration or make an appropriate default
     * configuration.  Will look in /etc and the user's home directory
     * as well as take command line options into consideration.
     *
     */
    private static Configuration makeOrFindConfiguration() throws IOException {
        String installDir = System.getProperty("user.home") + File.separatorChar + COPKG_HOME_PACKAGE_DIR;
        String reposUrl = COPKG_DEFAULT_PACKAGE_URL;
        Configuration c = new Configuration(installDir, reposUrl, "", "");

        File etc = new File("/etc/" + COPKG_ETC_DIR + "/" + COPKG_CONFIG_FILE);
        if (etc.exists()) {
            log.info("Getting configuration from " + etc.getAbsolutePath());
            c = Configuration.fromFile(etc);
        }

        File home = new File(
            System.getProperty("user.home")
            + File.separatorChar
            + COPKG_USER_DIR
            + File.separatorChar
            + COPKG_CONFIG_FILE);
        if (home.exists()) {
            log.info("Getting configuration from " + home.getAbsolutePath());
            c = Configuration.fromFile(home);
        }

        // The following is not the most elegant code, but it was
        // quick to write.  The Configuration class should have a
        // cascade/merge method instead.

        if (optionSet.has(packageDir)) {
            c = new Configuration(optionSet.valueOf(packageDir),
                                  c.getPackageBaseUrl(),
                                  c.getUsername(),
                                  c.getPassword());
        }

        if (optionSet.has(repository)) {
            c = new Configuration(c.getPackageDir(),
                                  optionSet.valueOf(repository),
                                  c.getUsername(),
                                  c.getPassword());
        }

        if (optionSet.has(username)) {
            c = new Configuration(c.getPackageDir(),
                                  c.getPackageBaseUrl(),
                                  optionSet.valueOf(username),
                                  c.getPassword());
        }

        if (optionSet.has(password)) {
            c = new Configuration(c.getPackageDir(),
                                  c.getPackageBaseUrl(),
                                  c.getUsername(),
                                  optionSet.valueOf(password));
        }

        return c;
    }
}
