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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.qt;

import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 16/01/13
 * Time: 13:37
 */
public class QTParams implements ProcessParams {

    private ConanParameter rampartConfig;
    private ConanParameter outputDir;
    private ConanParameter createConfigs;

    public QTParams() {

        this.rampartConfig = new PathParameter(
                "qtConfig",
                "The rampart configuration file describing the libraries to quality trim",
                false);

        this.outputDir = new PathParameter(
                "qtOutput",
                "The directory to place the quality trimmed libraries",
                false);

        this.createConfigs = new FlagParameter(
                "createConfigs",
                "Whether or not to create separate RAMPART configuration files for RAW and QT datasets in the output directory");
    }

    public ConanParameter getRampartConfig() {
        return rampartConfig;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getCreateConfigs() {
        return createConfigs;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.rampartConfig,
                        this.outputDir,
                        this.createConfigs
                }));
    }
}
