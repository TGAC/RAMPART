package uk.ac.tgac.rampart.tool.process.finalise;

import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 19/11/13
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class FinaliseParams implements ProcessParams {

    private ConanParameter jobPrefix;
    private ConanParameter inputFile;
    private ConanParameter outputDir;
    private ConanParameter minN;

    public FinaliseParams() {

        this.inputFile = new PathParameter(
                "input",
                "The input assembly in fasta format which will have its name's standardised",
                false);


        this.outputDir = new PathParameter(
                "output",
                "The output directory",
                false);

        this.jobPrefix = new ParameterBuilder()
                .longName("job_prefix")
                .description("The job prefix to apply to all child jobs")
                .argValidator(ArgValidator.DEFAULT)
                .create();

        this.minN = new NumericParameter(
                "min_n",
                "The minimum number of contiguous 'N's, which are necessary to distinguish a contig from a scaffold",
                true);
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    public ConanParameter getInputFile() {
        return inputFile;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getMinN() {
        return minN;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<>(Arrays.asList(
                new ConanParameter[]{
                        this.jobPrefix,
                        this.minN,
                        this.outputDir,
                        this.inputFile
                }));
    }
}
