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
package uk.ac.tgac.rampart.conan.tool.internal.mass;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.external.asm.Assembler;
import uk.ac.tgac.rampart.conan.tool.external.asm.Assemblers;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public class MassArgs implements ProcessArgs {

    public static enum StepSize {
        FINE {
            @Override
            public int nextKmer(int kmer) {
                return kmer += 2;
            }
        },
        MEDIUM {
            @Override
            public int nextKmer(int kmer) {
                int mod1 = (kmer - 1) % 10;
                int mod2 = (kmer - 5) % 10;

                if (mod1 == 0) {
                    return kmer + 4;
                }
                else if (mod2 == 0) {
                    return kmer + 6;
                }
                else {
                    throw new IllegalArgumentException("Kmer values have somehow got out of step!!");
                }
            }
        },
        COARSE {
            @Override
            public int nextKmer(int kmer) {
                return kmer += 10;
            }
        };

        /**
         * Retrieves the next k-mer value in the sequence
         * @param kmer The current k-mer value
         * @return The next kmer value
         */
        public abstract int nextKmer(int kmer);
    }


    // Constants
    public static final int KMER_MIN = 11;
    public static final int KMER_MAX = 125;

    // Need access to these
    private MassParams params = new MassParams();

    // Class vars
    private Assembler assembler;
    private Set<Library> libs;
    private int kmin;
    private int kmax;
    private StepSize stepSize;
    private String jobPrefix;
    private File outputDir;

    // Generated vars
    private File unitigsDir;
    private File contigsDir;
    private File scaffoldsDir;
    private File logsDir;

    public MassArgs() {
        this.assembler = null;
        this.libs = new HashSet<Library>();
        this.kmin = 41;
        this.kmax = 95;
        this.stepSize = StepSize.MEDIUM;
        this.jobPrefix = "";
        this.outputDir = null;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;

        this.unitigsDir = new File(this.outputDir, "unitigs");
        this.contigsDir = new File(this.outputDir, "contigs");
        this.scaffoldsDir = new File(this.outputDir, "scaffolds");
        this.logsDir = new File(this.outputDir, "logs");
    }

    public Set<Library> getLibs() {
        return libs;
    }

    public void setLibs(Set<Library> libs) {
        this.libs = libs;
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

    public Assembler getAssembler() {
        return assembler;
    }

    public void setAssembler(Assembler assembler) {
        this.assembler = assembler;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    public File getUnitigsDir() {
        return new File(this.outputDir, "unitigs");
    }

    public File getContigsDir() {
        return new File(this.outputDir, "contigs");
    }

    public File getScaffoldsDir() {
        return new File(this.outputDir, "scaffolds");
    }

    public File getLogsDir() {
        return new File(this.outputDir, "logs");
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.assembler != null)
            pvp.put(params.getAssembler(), this.assembler.getName());

        pvp.put(params.getKmin(), String.valueOf(this.kmin));
        pvp.put(params.getKmax(), String.valueOf(this.kmax));
        pvp.put(params.getStepSize(), this.stepSize.toString());

        if (this.libs != null)
            pvp.put(params.getLibs(), this.libs.toString());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        if (this.jobPrefix != null)
            pvp.put(params.getJobPrefix(), this.jobPrefix);


        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            };

            String param = entry.getKey().getName();

            if (param.equals(this.params.getAssembler().getName())) {
                this.assembler = Assemblers.valueOf(entry.getValue()).create();
            }
            else if (param.equals(this.params.getKmin().getName())) {
                this.kmin = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getKmax().getName())) {
                this.kmax = Integer.parseInt(entry.getValue());
            }
            else if (param.equals(this.params.getStepSize().getName())) {
                this.stepSize = StepSize.valueOf(entry.getValue());
            }
            else if (param.equals(this.params.getJobPrefix().getName())) {
                this.jobPrefix = entry.getValue();
            }
            else if (param.equals(this.params.getOutputDir())) {
                this.outputDir = new File(entry.getValue());
            }
            else if (param.equals(this.params.getLibs().getName())) {
                //TODO Need to be able to parse libs
                this.libs = null;
            }
            else {
                throw new IllegalArgumentException("Unknown parameter found: " + param);
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
