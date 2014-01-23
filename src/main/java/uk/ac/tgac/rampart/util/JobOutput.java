package uk.ac.tgac.rampart.util;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class JobOutput {
    private int jobId;
    private File outputFile;

    public JobOutput(int jobId, File outputFile) {
        this.jobId = jobId;
        this.outputFile = outputFile;
    }

    public int getJobId() {
        return jobId;
    }

    public File getOutputFile() {
        return outputFile;
    }
}
