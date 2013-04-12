package org.cloudname.copkg;

import static com.google.common.base.Preconditions.checkNotNull;

import org.cloudname.copkg.Configuration;
import org.cloudname.copkg.PackageCoordinate;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class takes care of running a single job which should
 * terminate within a reasonable amount of time.  It is not designed
 * to support long-running jobs.
 *
 * @author borud
 */
public final class JobRunner {
    private static final Logger log = Logger.getLogger(JobRunner.class.getName());

    public static final String SCRIPT_DIR = "script.d";
    public static final String START_SCRIPT = "start.py";
    public static final String STATUS_SCRIPT = "status.py";

    private final Configuration config;

    /**
     * @param config the copkg configuration.
     */
    public JobRunner(final Configuration config) {
        this.config = checkNotNull(config);
    }

    /**
     * Start a Job.  The job is expected to just start the service and
     * then terminate.  If this job hangs for an unacceptably long
     * time or it produces exorbitant amounts of output, we
     * unceremoniously terminate the process.
     *
     * @return a Result instance.
     */
    public Result startJob(final Job job) throws IOException, InterruptedException {
        File startScript = startScriptForJob(job);
        log.info("start script: " + startScript.getAbsolutePath());

        if (! startScript.exists()) {
            return Result.makeError(
                Result.Status.SCRIPT_NOT_FOUND,
                "Script does not exist: " + startScript.getAbsolutePath(),
                0);
        }

        if (! startScript.canExecute()) {
            return Result.makeError(
                Result.Status.SCRIPT_NOT_EXECUTABLE,
                "Script is not executable: " + startScript.getAbsolutePath(),
                0);
        }

        PackageCoordinate packageCoordinate = PackageCoordinate.parse(job.getPackageCoordinate());
        File workingDirectory = new File(config.packageDirectoryForCoordinate(packageCoordinate));

        // TODO(borud): Figure out how this ought to be done on
        // Windows.
        List<String> arguments = new LinkedList<String>();
        arguments.add(startScript.getAbsolutePath());
        Collections.addAll(arguments, job.getOptionArray());

        // Create new process
        Process process = new ProcessBuilder()
            .command(arguments)
            .directory(workingDirectory)
            .start();

        // TODO(borud): use future
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return new Result("", "", Result.Status.SUCCESS, "Script ran successfully", exitCode);
        }

        return Result.makeError(Result.Status.ERROR, "onzero exit code", exitCode);
    }

    /**
     * Figure out what the path of the start script is and return it.
     *
     * @return the File object pointing to the start script or
     *   {@code null} if it was not found.
     */
    public File startScriptForJob(final Job job) {
        checkNotNull(job);
        // There is a good chance you came to this method because you
        // wanted to add support for more script types.  And now you
        // are happy because you realized that I made this easy for
        // you.  This is the point where I tell you to reconsider.
        // Whatever you add support for, you are going to have to live
        // with for a very long time.  And if it isn't portable across
        // platforms you just became part of the problem.  Don't be
        // part of the problem.
        final PackageCoordinate packageCoordinate = PackageCoordinate.parse(job.getPackageCoordinate());
        return new File(
            config.packageDirectoryForCoordinate(packageCoordinate)
            + File.separatorChar
            + SCRIPT_DIR
            + File.separatorChar
            + START_SCRIPT);
    }
}
