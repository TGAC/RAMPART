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
import uk.ac.tgac.rampart.conan.tool.DeBrujinAssembler;
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


    // Constants
    public static final int KMER_MIN = 11;
    public static final int KMER_MAX = 125;


    // Class vars
    private DeBrujinAssembler assembler;
    private Set<Library> libs;
    private int kmin;
    private int kmax;
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

    public DeBrujinAssembler getAssembler() {
        return assembler;
    }

    public void setAssembler(DeBrujinAssembler assembler) {
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
    public Map<ConanParameter, String> getParameterValuePairs() {

        MassParams params = new MassParams();

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.assembler != null)
            pvp.put(params.getAssembler(), this.assembler.getName());

        pvp.put(params.getKmin(), String.valueOf(this.kmin));
        pvp.put(params.getKmax(), String.valueOf(this.kmax));

        if (this.libs != null)
            pvp.put(params.getLibs(), this.libs.toString());

        if (this.outputDir != null)
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());

        if (this.jobPrefix != null)
            pvp.put(params.getJobPrefix(), this.jobPrefix);


        return pvp;
    }
     /*
    @Override
    public void setFromParameterValuePairs(Map<ConanParameter, String> pvp) {

        for(Map.Entry<ConanParameter, String> arg : pvp.entrySet()) {

            if (!arg.getKey().validateParameterValue(arg.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + arg.getKey() + " : " + arg.getValue());
            };

            if (arg.getKey() == MassParams.ASSEMBLER) {
                this.assembler = arg.getValue();
            }

        }
    } */

    /**
     * Determines whether or not the supplied kmer range in this object is valid.  Throws an IllegalArgumentException if not.
     */
    public boolean validateKmers() {

        //TODO This logic isn't bullet proof... we can still nudge the minKmer above the maxKmer

        if (kmin < KMER_MIN || kmax < KMER_MIN)
            throw new IllegalArgumentException("K-mer values must be >= " + KMER_MIN + "nt");

        if (kmin > KMER_MAX || kmax > KMER_MAX)
            throw new IllegalArgumentException("K-mer values must be <= " + KMER_MAX + "nt");

        if (kmin > kmax)
            throw new IllegalArgumentException("Error: Min K-mer value must be <= Max K-mer value");

        // This test isn't required... we just make a best effort between the range provided.
        //if (!validKmer(kmin) || !validKmer(kmax))
        //	throw new IllegalArgumentException("Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.");
        return true;
    }
}
