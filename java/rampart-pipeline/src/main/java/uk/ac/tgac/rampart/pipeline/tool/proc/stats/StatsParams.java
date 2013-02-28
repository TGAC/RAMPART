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
package uk.ac.tgac.rampart.pipeline.tool.proc.stats;

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 16:13
 */
public class StatsParams implements ProcessParams {

    private ConanParameter inputDir;
    private ConanParameter outputDir;
    private ConanParameter stage;

    public StatsParams() {

        this.inputDir = new PathParameter(
                "input",
                "The input directory containing assemblies to analyse",
                true);

        this.outputDir = new PathParameter(
                "output",
                "The output file which should contain the assembly statistics",
                true);

        this.stage = new DefaultConanParameter(
                "stage",
                "The stage of the RAMPART pipeline we are currently processing.  Values: {MASS, IMPROVER}",
                false,
                true,
                false);
    }

    public ConanParameter getInputDir() {
        return inputDir;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getStage() {
        return stage;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.inputDir,
                        this.outputDir,
                        this.stage}));
    }
}
