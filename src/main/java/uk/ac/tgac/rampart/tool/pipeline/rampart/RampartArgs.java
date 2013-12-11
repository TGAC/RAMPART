/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.tool.pipeline.rampart;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.util.AbstractXmlJobConfiguration;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartJobFileSystem;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;
import uk.ac.tgac.rampart.tool.pipeline.RampartStageList;
import uk.ac.tgac.rampart.tool.pipeline.amp.AmpArgs;
import uk.ac.tgac.rampart.tool.process.finalise.FinaliseArgs;
import uk.ac.tgac.rampart.tool.process.kmercount.reads.KmerCountReadsArgs;
import uk.ac.tgac.rampart.tool.process.mass.MassArgs;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;
import uk.ac.tgac.rampart.tool.process.mecq.MecqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 07/02/13
 * Time: 19:26
 */
public class RampartArgs extends AbstractXmlJobConfiguration implements ProcessArgs {

    public static final String KEY_ELEM_LIBRARIES       = "libraries";
    public static final String KEY_ELEM_LIBRARY         = "library";
    public static final String KEY_ELEM_PIPELINE        = "pipeline";
    public static final String KEY_ELEM_MECQ            = "mecq";
    public static final String KEY_ELEM_KMER_READS      = "kmer-reads";
    public static final String KEY_ELEM_MASS            = "mass";
    public static final String KEY_ELEM_AMP             = "amp";
    public static final String KEY_ELEM_FINALISE        = "finalise";


    private RampartParams params = new RampartParams();

    private RampartStageList stages;
    private List<Library> libs;
    private MecqArgs mecqArgs;
    private KmerCountReadsArgs kmerCountReadsArgs;
    private MassArgs massArgs;
    private AmpArgs ampArgs;
    private FinaliseArgs finaliseArgs;
    private RampartJobFileSystem rampartJobFileSystem;
    private ExecutionContext executionContext;



    public RampartArgs(File configFile, File outputDir, String jobPrefix, RampartStageList stages)
            throws IOException {

        super(configFile, outputDir, jobPrefix);

        this.stages = stages;
        this.libs = new ArrayList<>();
        this.mecqArgs = null;
        this.kmerCountReadsArgs = null;
        this.massArgs = null;
        this.ampArgs = null;
        this.finaliseArgs = null;
        this.rampartJobFileSystem = new RampartJobFileSystem(outputDir.getAbsoluteFile());
    }



    @Override
    protected void internalParseXml(Element element) throws IOException {

        // All libraries
        Element librariesElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_LIBRARIES);
        NodeList libraries = librariesElement.getElementsByTagName(KEY_ELEM_LIBRARY);
        this.libs = new ArrayList<>();
        for(int i = 0; i < libraries.getLength(); i++) {
            this.libs.add(new Library((Element)libraries.item(i), this.getOutputDir().getAbsoluteFile()));
        }

        Element pipelineElement = XmlHelper.getDistinctElementByName(element, KEY_ELEM_PIPELINE);

        // MECQ
        Element mecqElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_MECQ);
        this.mecqArgs = mecqElement == null ? null :
                new MecqArgs(
                        mecqElement,
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.getJobPrefix() + "-mecq",
                        this.libs);

        this.stages.setArgsIfPresent(RampartStage.MECQ, this.mecqArgs);


        // Kmer counting reads
        Element kmerReadsElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_KMER_READS);
        this.kmerCountReadsArgs = kmerReadsElement == null ? null :
                new KmerCountReadsArgs(
                        kmerReadsElement,
                        this.libs,
                        this.mecqArgs == null ? new ArrayList<EcqArgs>() : this.mecqArgs.getEqcArgList(),
                        this.getJobPrefix() + "-kmer_reads",
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.rampartJobFileSystem.getReadsKmersDir(),
                        this.getOrganism());

        this.stages.setArgsIfPresent(RampartStage.KMER_READS, this.kmerCountReadsArgs);

        // MASS
        Element massElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_MASS);
        this.massArgs = massElement == null ? null :
                new MassArgs(
                        massElement,
                        this.rampartJobFileSystem.getMassDir(),
                        this.rampartJobFileSystem.getMeqcDir(),
                        this.getJobPrefix() + "-mass",
                        this.libs,
                        this.mecqArgs == null ? new ArrayList<EcqArgs>() : this.mecqArgs.getEqcArgList(),
                        this.getOrganism());

        this.stages.setArgsIfPresent(RampartStage.MASS, this.massArgs);

        // AMP
        Element ampElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_AMP);
        this.ampArgs = ampElement == null ? null :
                new AmpArgs(
                        ampElement,
                        this.rampartJobFileSystem.getAmpDir(),
                        this.getJobPrefix() + "-amp",
                        this.rampartJobFileSystem.getMassOutFile(),
                        this.libs,
                        this.mecqArgs == null ? new ArrayList<EcqArgs>() : this.mecqArgs.getEqcArgList(),
                        this.getOrganism());

        this.stages.setArgsIfPresent(RampartStage.AMP, this.ampArgs);

        File finalAssembly = this.ampArgs == null ? this.rampartJobFileSystem.getMassOutFile(): this.ampArgs.getFinalAssembly();

        Element finaliseElement = XmlHelper.getDistinctElementByName(pipelineElement, KEY_ELEM_FINALISE);
        this.finaliseArgs = finaliseElement == null ? null :
                new FinaliseArgs(
                        finaliseElement,
                        finalAssembly,
                        this.rampartJobFileSystem.getFinalDir(),
                        this.getOrganism(),
                        this.getInstitution());

        this.stages.setArgsIfPresent(RampartStage.FINALISE, this.finaliseArgs);
    }

    public RampartStageList getStages() {
        return stages;
    }

    public void setStages(RampartStageList stages) {
        this.stages = stages;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<>();

        if (this.getConfigFile() != null) {
            pvp.put(params.getConfig(), this.getConfigFile().getAbsolutePath());
        }

        if (this.getOutputDir() != null) {
            pvp.put(params.getOutputDir(), this.getOutputDir().getAbsolutePath());
        }

        if (this.stages != null && this.stages.size() > 0) {
            pvp.put(params.getStageList(), this.stages.toString());
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) throws IOException {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getConfig().getName())) {
                this.setConfigFile(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getOutputDir().getName())) {
                this.setOutputDir(new File(entry.getValue()));
            }
            else if (param.equals(this.params.getStageList().getName())) {
                this.stages = RampartStageList.parse(entry.getValue());
            }
        }

        this.rampartJobFileSystem = new RampartJobFileSystem(this.getOutputDir());

        this.parseXml();
    }

    @Override
    public String toString() {

        StringJoiner sj = new StringJoiner("\n");
        sj.add("Job Configuration File: " + this.getConfigFile().getAbsolutePath());
        sj.add("Output Directory: " + this.getOutputDir().getAbsolutePath());
        sj.add("Job Prefix: " + this.getJobPrefix());
        sj.add("Stages: " + ArrayUtils.toString(stages));

        return sj.toString();
    }

    public AmpArgs getAmpArgs() {
        return ampArgs;
    }

    public void setAmpArgs(AmpArgs ampArgs) {
        this.ampArgs = ampArgs;
    }

    public MecqArgs getMecqArgs() {
        return mecqArgs;
    }

    public void setMecqArgs(MecqArgs mecqArgs) {
        this.mecqArgs = mecqArgs;
    }

    public KmerCountReadsArgs getKmerCountReadsArgs() {
        return kmerCountReadsArgs;
    }

    public void setKmerCountReadsArgs(KmerCountReadsArgs kmerCountReadsArgs) {
        this.kmerCountReadsArgs = kmerCountReadsArgs;
    }

    public MassArgs getMassArgs() {
        return massArgs;
    }

    public void setMassArgs(MassArgs massArgs) {
        this.massArgs = massArgs;
    }

    public FinaliseArgs getFinaliseArgs() {
        return finaliseArgs;
    }

    public void setFinaliseArgs(FinaliseArgs finaliseArgs) {
        this.finaliseArgs = finaliseArgs;
    }

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public List<ConanProcess> getRequestedTools() {
        return this.stages.getExternalTools();
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}
