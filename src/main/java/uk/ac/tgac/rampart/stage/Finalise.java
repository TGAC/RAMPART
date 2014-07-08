package uk.ac.tgac.rampart.stage;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartCLI;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This is derived from Richard's FastA-to-AGP script in TGAC tools, which is in turn derived form Shaun Jackman's
 * FastA-to-AGP script in Abyss.
 */
public class Finalise extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(Finalise.class);

    public static final String NAME = "Finalise";

    private BufferedReader reader;
    private PrintWriter contigWriter;
    private PrintWriter scaffoldWriter;
    private PrintWriter agpWriter;
    private PrintWriter translationWriter;

    private int scaffoldId;
    private int contigId;

    public Finalise() {
        this(new Args());
    }

    public Finalise(Args args) {
        super("", args, new Params());

        this.reader = null;
        this.contigWriter = null;
        this.scaffoldWriter = null;
        this.agpWriter = null;
        this.translationWriter = null;

        this.scaffoldId = 0;
        this.contigId = 0;
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        log.info("Finaliser stage is operational.");
        return true;
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        this.scaffoldId = 0;
        this.contigId = 0;

        try {

            log.info("Starting finalising process to standardise assembly names.  Note that finaliser does not call out to external processes.");

            Args args = this.getArgs();

            // Create the output directory
            if (args.getOutputDir().exists()) {
                FileUtils.deleteDirectory(args.getOutputDir());
            }
            args.getOutputDir().mkdir();

            this.reader = new BufferedReader(new FileReader(args.getInputFile()));
            this.contigWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".contigs.fa"))));
            this.scaffoldWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".scaffolds.fa"))));
            this.agpWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".agp"))));
            this.translationWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".translation"))));

            String currentId = "";
            StringBuilder currentContig = new StringBuilder();

            String line = null;
            while((line = this.reader.readLine()) != null) {

                line = line.trim();

                if (line.startsWith(">")) {
                    if (currentContig.length() > 0) {
                        processObject(currentId, currentContig.toString());
                    }
                    currentContig = new StringBuilder();
                    currentId = line.substring(1);
                }
                else {
                    currentContig.append(line);
                }
            }

            if (currentContig.length() > 0) {
                processObject(currentId, currentContig.toString());
            }

            log.info("Finishing finalising assembly successfully.");
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(3, ioe);
        }
        finally {
            try {
                if (this.reader != null) this.reader.close();
                if (this.contigWriter != null) this.contigWriter.close();
                if (this.scaffoldWriter != null) this.scaffoldWriter.close();
                if (this.agpWriter != null) this.agpWriter.close();
                if (this.translationWriter != null) this.translationWriter.close();
            }
            catch (IOException ioe) {
                throw new ProcessExecutionException(4, ioe);
            }
        }

        return true;
    }

    private void processObject(String currentHeader, String currentContig) {

        Args args = this.getArgs();

        int scaffoldLen = currentContig.length();
        String scaffoldHeader = args.getOutputPrefix() + "_scaffold_" + (++scaffoldId) + "-size_" + scaffoldLen;

        this.scaffoldWriter.println(">" + scaffoldHeader);
        this.scaffoldWriter.println(currentContig);

        this.translationWriter.println(scaffoldHeader + "\t" + currentHeader);

        String[] contigSeqs = currentContig.split("[Nn]{" + args.getMinN() + ",}");

        int lineNum = 1;
        int pos = 1;
        for(String contig : contigSeqs) {

            if (!contig.isEmpty()) {
                int contigLen = contig.length();

                int end = pos + contigLen - 1;
                this.agpWriter.print(scaffoldHeader + "\t" + pos + "\t" + end + "\t" + lineNum + "\t");

                if (contig.startsWith("N") || contig.startsWith("n")) {
                    this.agpWriter.print("N\t" + contigLen + "\tscaffold\tyes\tpaired-ends\n");
                }
                else {
                    String contigHeader = args.getOutputPrefix() + "_contig_" + (++contigId) + "-size_" + contigLen;

                    this.agpWriter.print("W\t" + contigHeader + "\t1\t" + contigLen + "\t+\n");
                    this.contigWriter.print(">" + contigHeader + "\n" + contig + "\n");
                    this.translationWriter.print(contigHeader + "\t" + currentHeader + "\n");
                }
                lineNum++;
                pos += contigLen;
            }
        }

    }

    public static class Args extends AbstractProcessArgs implements RampartStageArgs {


        public static final String KEY_ATTR_PREFIX = "prefix";
        public static final String KEY_ATTR_MIN_N = "min_n";

        public static final int DEFAULT_MIN_N = 10;

        private String outputPrefix;
        private File inputFile;
        private File outputDir;
        private int minN;


        public Args() {

            super(new Params());
            this.outputPrefix = "rampart";
            this.inputFile = null;
            this.outputDir = RampartCLI.CWD;
            this.minN = DEFAULT_MIN_N;
        }

        public Args(Element element, File inputFile, File outputDir, Organism organism, String institution) {

            this();

            String shortInstitute = this.getInitials(institution, "_");
            String shortOrg = this.getInitials(organism.getName(), "_");

            String start = shortInstitute.isEmpty() && shortOrg.isEmpty() ? "rampart" : shortInstitute + shortOrg;
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

            String derivedPrefix = start + today;

            this.inputFile = inputFile;
            this.outputDir = outputDir;

            this.outputPrefix = element.hasAttribute(KEY_ATTR_PREFIX) ?
                    XmlHelper.getTextValue(element, KEY_ATTR_PREFIX) :
                    derivedPrefix;

            this.minN = element.hasAttribute(KEY_ATTR_MIN_N) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_MIN_N) :
                    DEFAULT_MIN_N;

            if (this.outputPrefix.contains(".") || this.outputPrefix.contains("|")) {
                throw new IllegalArgumentException("Will not use dots or pipes in the assembly headers because this can cause " +
                        "problems for downstream tools.");
            }
        }

        private String getInitials(String string, String suffix) {

            String[] stringParts = string.trim().split(" ");

            String shortString = "";
            if (stringParts.length > 1) {
                for(String part : stringParts) {
                    shortString += part.charAt(0);
                }
                shortString += suffix;
            }
            else if (stringParts.length == 1) {
                shortString = stringParts[0];
                shortString += suffix;
            }

            return shortString;
        }

        public String getOutputPrefix() {
            return outputPrefix;
        }

        public void setOutputPrefix(String outputPrefix) {
            this.outputPrefix = outputPrefix;
        }

        public File getInputFile() {
            return inputFile;
        }

        public void setInputFile(File inputFile) {
            this.inputFile = inputFile;
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public int getMinN() {
            return minN;
        }

        public void setMinN(int minN) {
            this.minN = minN;
        }

        @Override
        public void parse(String args) throws IOException {

        }

        @Override
        public ParamMap getArgMap() {
            return null;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }


        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    public static class Params extends AbstractProcessParams {

        private ConanParameter jobPrefix;
        private ConanParameter inputFile;
        private ConanParameter outputDir;
        private ConanParameter minN;

        public Params() {

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
        public ConanParameter[] getConanParametersAsArray() {
            return new ConanParameter[]{
                    this.jobPrefix,
                    this.minN,
                    this.outputDir,
                    this.inputFile
            };
        }

    }

}
