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
package uk.ac.tgac.rampart.pipeline.tool.pipeline.rampart;

import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 07/02/13
 * Time: 19:27
 */
public class RampartParams implements ProcessParams {

    private ConanParameter config;
    private ConanParameter outputDir;

    public RampartParams() {

        this.config = new PathParameter(
                "config",
                "The RAMPART configuration file",
                false);

        this.outputDir = new PathParameter(
                "output",
                "The path to the folder where all RAMPART output should be stored",
                true);

    }

    public ConanParameter getConfig() {
        return config;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    @Override
    public List<ConanParameter> getConanParameters() {

        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.config,
                        this.outputDir}));
    }
}
