package uk.ac.tgac.rampart.stage.analyse.asm;

import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartCLI;
import uk.ac.tgac.rampart.stage.RampartStageArgs;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by maplesod on 15/06/14.
 */
public abstract class AnalyseAssembliesArgs extends AbstractProcessArgs implements RampartStageArgs {

    private static final String KEY_ATTR_TYPES = "types";
    private static final String KEY_ATTR_PARALLEL = "parallel";
    private static final String KEY_ATTR_THREADS = "threads";
    private static final String KEY_ATTR_WEIGHTINGS = "weightings_file";

    public static final boolean DEFAULT_RUN_PARALLEL = false;
    public static final int DEFAULT_THREADS = 1;

    public static final File DEFAULT_SYSTEM_WEIGHTINGS_FILE = new File(RampartCLI.ETC_DIR, "weightings.tab");
    public static final File    DEFAULT_USER_WEIGHTINGS_FILE = new File(RampartCLI.USER_DIR, "weightings.tab");
    public static final File    DEFAULT_WEIGHTINGS_FILE = DEFAULT_USER_WEIGHTINGS_FILE.exists() ?
            DEFAULT_USER_WEIGHTINGS_FILE : DEFAULT_SYSTEM_WEIGHTINGS_FILE;


    private String[] asmAnalyses;
    private File analyseReadsDir;
    private File outputDir;
    private Organism organism;
    private File weightingsFile;
    private int threadsPerProcess;
    private boolean runParallel;
    private String jobPrefix;

    public AnalyseAssembliesArgs(AnalyseAssembliesParams params) {

        super(params);

        this.asmAnalyses = null;
        this.analyseReadsDir = null;
        this.outputDir = null;
        this.organism = null;
        this.weightingsFile = DEFAULT_WEIGHTINGS_FILE;
        this.threadsPerProcess = 1;
        this.runParallel = false;
        this.jobPrefix = "assembly-analyses";
    }

    public AnalyseAssembliesArgs(AnalyseAssembliesParams params, Element element, File analyseReadsDir, File outputDir,
                Organism organism, String jobPrefix) {

        super(params);

        this.analyseReadsDir = analyseReadsDir;
        this.outputDir = outputDir;
        this.organism = organism;
        this.jobPrefix = jobPrefix;

        this.asmAnalyses = element.hasAttribute(KEY_ATTR_TYPES) ?
                XmlHelper.getTextValue(element, KEY_ATTR_TYPES).split(",") :
                null;

        this.threadsPerProcess = element.hasAttribute(KEY_ATTR_THREADS) ?
                XmlHelper.getIntValue(element, KEY_ATTR_THREADS) :
                DEFAULT_THREADS;

        this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ?
                XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) :
                DEFAULT_RUN_PARALLEL;

        this.weightingsFile = element.hasAttribute(KEY_ATTR_WEIGHTINGS) ?
                new File(XmlHelper.getTextValue(element, KEY_ATTR_WEIGHTINGS)) :
                DEFAULT_WEIGHTINGS_FILE;

    }

    public String[] getAsmAnalyses() {
        return asmAnalyses;
    }

    public void setAsmAnalyses(String[] asmAnalyses) {
        this.asmAnalyses = asmAnalyses;
    }

    public File getAnalyseReadsDir() {
        return analyseReadsDir;
    }

    public void setAnalyseReadsDir(File analyseReadsDir) {
        this.analyseReadsDir = analyseReadsDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public File getWeightings() {
        return weightingsFile;
    }

    public void setWeightings(File weightingsFile) {
        this.weightingsFile = weightingsFile;
    }

    public int getThreadsPerProcess() {
        return threadsPerProcess;
    }

    public void setThreadsPerProcess(int threadsPerProcess) {
        this.threadsPerProcess = threadsPerProcess;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    @Override
    protected void setOptionFromMapEntry(ConanParameter param, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void setArgFromMapEntry(ConanParameter param, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void parse(String args) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ParamMap getArgMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ConanProcess> getExternalProcesses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
