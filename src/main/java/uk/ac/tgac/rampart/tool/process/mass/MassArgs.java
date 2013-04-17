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
package uk.ac.tgac.rampart.tool.process.mass;

import org.ini4j.Profile;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.rampart.data.RampartConfiguration;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassParams;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public abstract class MassArgs implements ProcessArgs {

    // Keys for config file
    public static final String MASS_TOOL = "tool";
    public static final String MASS_KMIN = "kmin";
    public static final String MASS_KMAX = "kmax";
    public static final String MASS_STEP = "step";
    public static final String MASS_THREADS = "threads";
    public static final String MASS_MEMORY = "memory";
    public static final String MASS_PARALLEL = "parallel";
    public static final String MASS_CVG_CUTOFF = "cutoff";
    public static final String MASS_OUTPUT_LEVEL = "output_level";

    // Constants
    public static final int KMER_MIN = 11;
    public static final int KMER_MAX = 125;
    public static final int DEFAULT_KMER_MIN = 51;
    public static final int DEFAULT_KMER_MAX = 85;
    public static final StepSize DEFAULT_STEP_SIZE = StepSize.MEDIUM;
    public static final int DEFAULT_THREADS = 8;
    public static final int DEFAULT_MEM = 50000;
    public static final ParallelismLevel DEFAULT_PARALLELISM_LEVEL = ParallelismLevel.PARALLEL_ASSEMBLIES_ONLY;
    public static final int DEFAULT_CVG_CUTOFF = -1;
    public static final OutputLevel DEFAULT_OUTPUT_LEVEL = OutputLevel.CONTIGS;

    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Class vars
    private String assembler;
    private int kmin;
    private int kmax;
    private StepSize stepSize;
    private List<Library> libs;
    private String jobPrefix;
    private File outputDir;
    private int threads;
    private int memory;
    private ParallelismLevel parallelismLevel;
    private int coverageCutoff;
    private OutputLevel outputLevel;

    public enum ParallelismLevel {

        LINEAR,
        PARALLEL_ASSEMBLIES_ONLY,
        PARALLEL_MASS_ONLY,
        FULL;

        public static ParallelismLevel P0 = LINEAR;
        public static ParallelismLevel P1 = PARALLEL_ASSEMBLIES_ONLY;
        public static ParallelismLevel P2 = PARALLEL_MASS_ONLY;
        public static ParallelismLevel P3 = FULL;

        public boolean doParallelAssemblies() {
            return this == PARALLEL_ASSEMBLIES_ONLY || this == FULL;
        }

        public boolean doParallelMass() {
            return this == PARALLEL_MASS_ONLY || this == FULL;
        }
    }

    public enum OutputLevel {

        UNITIGS,
        CONTIGS,
        SCAFFOLDS
    }


    public MassArgs() {
        this.assembler = null;
        this.kmin = DEFAULT_KMER_MIN;
        this.kmax = DEFAULT_KMER_MAX;
        this.stepSize = DEFAULT_STEP_SIZE;
        this.libs = new ArrayList<Library>();
        this.jobPrefix = "";
        this.outputDir = null;
        this.threads = DEFAULT_THREADS;
        this.memory = DEFAULT_MEM;
        this.parallelismLevel = DEFAULT_PARALLELISM_LEVEL;
        this.coverageCutoff = DEFAULT_CVG_CUTOFF;
        this.outputLevel = DEFAULT_OUTPUT_LEVEL;
    }


    public int getKmin() {
        return kmin;
    }

    public void setKmin(int kmin) {
        this.kmin = kmin;
    }

    public int getKmax() {
        return kmax;
    }

    public void setKmax(int kmax) {
        this.kmax = kmax;
    }

    public StepSize getStepSize() {
        return stepSize;
    }

    public void setStepSize(StepSize stepSize) {
        this.stepSize = stepSize;
    }

    public String getAssembler() {
        return assembler;
    }

    public void setAssembler(String assembler) {
        this.assembler = assembler;
    }

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int mem) {
        this.memory = mem;
    }

    public ParallelismLevel getParallelismLevel() {
        return parallelismLevel;
    }

    public void setParallelismLevel(ParallelismLevel parallelismLevel) {
        this.parallelismLevel = parallelismLevel;
    }

    public int getCoverageCutoff() {
        return coverageCutoff;
    }

    public void setCoverageCutoff(int coverageCutoff) {
        this.coverageCutoff = coverageCutoff;
    }

    public OutputLevel getOutputLevel() {
        return outputLevel;
    }

    public void setOutputLevel(OutputLevel outputLevel) {
        this.outputLevel = outputLevel;
    }

    public void parseConfig(File config) throws IOException {

        RampartConfiguration rampartConfig = new RampartConfiguration();

        rampartConfig.load(config);
        this.setLibs(rampartConfig.getLibs());
        Profile.Section section = rampartConfig.getMassSettings();

        if (section != null) {
            for (Map.Entry<String, String> entry : section.entrySet()) {

                if (entry.getKey().equalsIgnoreCase(MASS_TOOL)) {
                    this.setAssembler(entry.getValue());
                } else if (entry.getKey().equalsIgnoreCase(MASS_KMIN)) {
                    this.setKmin(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_KMAX)) {
                    this.setKmax(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_STEP)) {
                    this.setStepSize(StepSize.valueOf(entry.getValue().toUpperCase()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_THREADS)) {
                    this.setThreads(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_MEMORY)) {
                    this.setMemory(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_PARALLEL)) {
                    this.setParallelismLevel(ParallelismLevel.valueOf(entry.getValue().trim().toUpperCase()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_CVG_CUTOFF)) {
                    this.setCoverageCutoff(Integer.parseInt(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(MASS_OUTPUT_LEVEL)) {
                    this.setOutputLevel(OutputLevel.valueOf(entry.getValue().trim().toUpperCase()));
                }
            }
        }
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.assembler != null)
            pvp.put(params.getAssembler(), this.assembler);

        pvp.put(params.getKmin(), String.valueOf(this.kmin));
        pvp.put(params.getKmax(), String.valueOf(this.kmax));
        pvp.put(params.getThreads(), String.valueOf(this.threads));
        pvp.put(params.getMemory(), String.valueOf(this.memory));

        if (this.stepSize != null) {
            pvp.put(params.getStepSize(), this.stepSize.toString());
        }

        if (this.parallelismLevel != null) {
            pvp.put(params.getParallelismLevel(), this.parallelismLevel.toString());
        }

        if (this.outputLevel != null) {
            pvp.put(params.getOutputLevel(), this.outputLevel.toString());
        }

        if (this.coverageCutoff > -1) {
            pvp.put(params.getCoverageCutoff(), Integer.toString(this.coverageCutoff));
        }

        // TODO not sure the toString method is sufficient here.
        if (this.libs != null && this.libs.size() > 0)
            pvp.put(params.getLibs(), this.libs.toString());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        if (this.jobPrefix != null)
            pvp.put(params.getJobPrefix(), this.jobPrefix);


        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getAssembler().getName())) {
                this.assembler = entry.getValue();
            } else if (param.equals(this.params.getKmin().getName())) {
                this.kmin = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getKmax().getName())) {
                this.kmax = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getThreads().getName())) {
                this.threads = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getMemory().getName())) {
                this.memory = Integer.parseInt(entry.getValue());
            } else if (param.equals(this.params.getStepSize().getName())) {
                this.stepSize = StepSize.valueOf(entry.getValue());
            } else if (param.equals(this.params.getLibs().getName())) {
                //TODO need to implement this at some point!
                this.libs = new ArrayList<Library>();
            } else if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equalsIgnoreCase(this.params.getParallelismLevel().getName())) {
                this.parallelismLevel = ParallelismLevel.valueOf(entry.getValue());
            } else if (param.equalsIgnoreCase(this.params.getCoverageCutoff().getName())) {
                this.coverageCutoff = Integer.parseInt(entry.getValue());
            } else if (param.equalsIgnoreCase(this.params.getOutputLevel().getName())) {
                this.outputLevel = OutputLevel.valueOf(entry.getValue());
            }
        }
    }


    /**
     * Determines whether or not the supplied kmer range in this object is valid.  Throws an IllegalArgumentException if not.
     */
    public boolean validateKmers(int kmin, int kmax) {

        //TODO This logic isn't bullet proof... we can still nudge the minKmer above the maxKmer

        if (kmin < KMER_MIN || kmax < KMER_MIN)
            throw new IllegalArgumentException("K-mer values must be >= " + KMER_MIN + "nt");

        if (kmin > KMER_MAX || kmax > KMER_MAX)
            throw new IllegalArgumentException("K-mer values must be <= " + KMER_MAX + "nt");

        if (kmin > kmax)
            throw new IllegalArgumentException("Error: Min K-mer value must be <= Max K-mer value");

        return true;
    }
}
