package uk.ac.tgac.rampart.tool.process.kmercount.reads;

import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 20/11/13
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class KmerCountReadsArgs implements ProcessArgs {

    private static final String KEY_ATTR_PARALLEL = "parallel";
    private static final String KEY_ATTR_THREADS = "threads";
    private static final String KEY_ATTR_MEMORY = "memory";


    public static final boolean DEFAULT_RUN_PARALLEL = false;
    public static final int DEFAULT_THREADS = 1;
    public static final int DEFAULT_MEMORY = 0;


    private List<Library> allLibraries;             // All allLibraries available in this job
    private List<EcqArgs> allMecqs;                 // All mecq configurations
    private String jobPrefix;
    private File mecqDir;                           // Where all the output lives
    private boolean runParallel;                    // Whether to run MASS groups in parallel
    private File outputDir;
    private Organism organism;
    private int threadsPerProcess;
    private int memoryPerProcess;

    public KmerCountReadsArgs() {
        this.allLibraries = new ArrayList<>();
        this.allMecqs = new ArrayList<>();
        this.jobPrefix = "kmer-reads";
        this.mecqDir = null;
        this.runParallel = DEFAULT_RUN_PARALLEL;
        this.outputDir = null;
        this.organism = null;
        this.threadsPerProcess = DEFAULT_THREADS;
        this.memoryPerProcess = DEFAULT_MEMORY;
    }


    public KmerCountReadsArgs(Element element, List<Library> allLibraries, List<EcqArgs> allMecqs, String jobPrefix, File mecqDir,
                              File outputDir, Organism organism) {

        this.allLibraries = allLibraries;
        this.allMecqs = allMecqs;
        this.jobPrefix = jobPrefix;
        this.mecqDir = mecqDir;
        this.outputDir = outputDir;
        this.organism = organism;

        // From Xml (optional)
        this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ?
                XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) :
                DEFAULT_RUN_PARALLEL;

        this.threadsPerProcess = element.hasAttribute(KEY_ATTR_THREADS) ?
                XmlHelper.getIntValue(element, KEY_ATTR_THREADS) :
                DEFAULT_THREADS;

        this.memoryPerProcess = element.hasAttribute(KEY_ATTR_MEMORY) ?
                XmlHelper.getIntValue(element, KEY_ATTR_MEMORY) :
                DEFAULT_MEMORY;
    }

    public List<Library> getAllLibraries() {
        return allLibraries;
    }

    public void setAllLibraries(List<Library> allLibraries) {
        this.allLibraries = allLibraries;
    }

    public List<EcqArgs> getAllMecqs() {
        return allMecqs;
    }

    public void setAllMecqs(List<EcqArgs> allMecqs) {
        this.allMecqs = allMecqs;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public int getThreadsPerProcess() {
        return threadsPerProcess;
    }

    public void setThreadsPerProcess(int threadsPerProcess) {
        this.threadsPerProcess = threadsPerProcess;
    }

    public int getMemoryPerProcess() {
        return memoryPerProcess;
    }

    public void setMemoryPerProcess(int memoryPerProcess) {
        this.memoryPerProcess = memoryPerProcess;
    }

    public File getMecqDir() {
        return mecqDir;
    }

    public void setMecqDir(File mecqDir) {
        this.mecqDir = mecqDir;
    }

    public boolean isRunParallel() {
        return runParallel;
    }

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
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

    @Override
    public void parse(String args) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
