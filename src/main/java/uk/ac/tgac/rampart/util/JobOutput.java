package uk.ac.tgac.rampart.util;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class JobOutput {
    private ExecutionResult result;
    private File outputFile;

    public JobOutput(ExecutionResult result, File outputFile) {
        this.result = result;
        this.outputFile = outputFile;
    }

    public ExecutionResult getResult() {
        return result;
    }

    public int getJobId() {
        return this.result.getJobId();
    }

    public File getOutputFile() {
        return outputFile;
    }
}
