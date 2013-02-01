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
package uk.ac.tgac.rampart.conan.tool.module.mass;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.module.mass.single.SingleMassParams;
import uk.ac.tgac.rampart.conan.tool.process.asm.Assembler;
import uk.ac.tgac.rampart.conan.tool.process.asm.AssemblerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:04
 */
public abstract class MassArgs implements ProcessArgs {

    // Constants
    public static final int KMER_MIN = 11;
    public static final int KMER_MAX = 125;

    // Need access to these
    private SingleMassParams params = new SingleMassParams();

    // Class vars
    private Assembler assembler;
    private int kmin;
    private int kmax;
    private StepSize stepSize;


    public MassArgs() {
        this.assembler = null;
        this.kmin = 41;
        this.kmax = 95;
        this.stepSize = StepSize.MEDIUM;
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



    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.assembler != null)
            pvp.put(params.getAssembler(), this.assembler.getName());

        pvp.put(params.getKmin(), String.valueOf(this.kmin));
        pvp.put(params.getKmax(), String.valueOf(this.kmax));
        pvp.put(params.getStepSize(), this.stepSize.toString());



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
                this.assembler = AssemblerFactory.valueOf(entry.getValue()).create();
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
