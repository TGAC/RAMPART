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
package uk.ac.tgac.rampart.tool.process.analyser.length;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 16:13
 */
public class LengthAnalysisArgs implements ProcessArgs {

    private LengthAnalysisParams params = new LengthAnalysisParams();

    private File inputDir;
    private File outputDir;
    private RampartStage rampartStage;
    private String dataset;

    public LengthAnalysisArgs() {
        this.inputDir = null;
        this.outputDir = null;
        this.rampartStage = RampartStage.MASS;
        this.dataset = null;
    }

    public File getInputDir() {
        return inputDir;
    }

    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public RampartStage getRampartStage() {
        return rampartStage;
    }

    public void setRampartStage(RampartStage rampartStage) {
        this.rampartStage = rampartStage;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.inputDir != null) {
            pvp.put(params.getInputDir(), this.inputDir.getAbsolutePath());
        }

        if (this.outputDir != null) {
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());
        }

        if (this.rampartStage != null) {
            pvp.put(params.getStage(), this.rampartStage.toString());
        }

        if (this.dataset != null && !this.dataset.isEmpty()) {
            pvp.put(params.getDataset(), this.rampartStage.toString());
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getInputDir().getName())) {
                this.inputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            } else if (param.equals(this.params.getStage().getName())) {
                this.rampartStage = RampartStage.valueOf(entry.getValue());
            } else if (param.equals(this.params.getDataset().getName())) {
                this.dataset = entry.getValue();
            } else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }
}
