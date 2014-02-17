package uk.ac.tgac.rampart.tool.process.finalise;

import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.Rampart;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageArgs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 19/11/13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class FinaliseArgs implements RampartStageArgs {


    public static final String KEY_ATTR_PREFIX = "prefix";
    public static final String KEY_ATTR_MIN_N = "min_n";

    public static final int DEFAULT_MIN_N = 10;

    private String outputPrefix;
    private File inputFile;
    private File outputDir;
    private int minN;


    public FinaliseArgs() {
        this.outputPrefix = "rampart";
        this.inputFile = null;
        this.outputDir = Rampart.CWD;
        this.minN = DEFAULT_MIN_N;
    }

    public FinaliseArgs(Element element, File inputFile, File outputDir, Organism organism, String institution) {

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
    public void setFromArgMap(ParamMap pvp) throws IOException, ConanParameterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public List<ConanProcess> getExternalProcesses() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
