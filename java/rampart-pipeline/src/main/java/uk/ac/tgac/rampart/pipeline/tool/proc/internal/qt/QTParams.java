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

import uk.ac.ebi.fgpt.conan.core.param.DefaultConanParameter;
import uk.ac.ebi.fgpt.conan.core.param.FlagParameter;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
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
    private ConanParameter qualityTrimmer;
    private ConanParameter libs;
    private ConanParameter outputDir;
    private ConanParameter minLength;
    private ConanParameter minQuality;
    private ConanParameter createConfigs;
    private ConanParameter jobPrefix;
    private ConanParameter runParallel;

    public QTParams() {

        this.rampartConfig = new PathParameter(
                "qtConfig",
                "The rampart configuration file describing the libraries to quality trim",
                true);

        this.qualityTrimmer = new DefaultConanParameter(
                "qualityTrimmer",
                "The quality trimming tool to be used",
                false,
                true);

        this.libs = new DefaultConanParameter(
                "libs",
                "The libraries to be quality trimmed",
                false,
                true);

        this.outputDir = new PathParameter(
                "qtOutput",
                "The directory to place the quality trimmed libraries",
                false);

        this.minLength = new NumericParameter(
                "minLength",
                "The minimum length for trimmed reads.  Any reads shorter than this value after trimming are discarded",
                true);

        this.minQuality = new NumericParameter(
                "minQuality",
                "The minimum quality for trimmed reads.  Any reads with quality scores lower than this value will be trimmed.",
                true);

        this.createConfigs = new FlagParameter(
                "createConfigs",
                "Whether or not to create separate RAMPART configuration files for RAW and QT datasets in the output directory");

        this.jobPrefix = new DefaultConanParameter(
                "jobPrefix",
                "If using a scheduler this prefix is applied to the job names of all child QT processes",
                false,
                true);

        this.runParallel = new FlagParameter(
                "runParallel",
                "If set to true, and we want to run QT in a scheduled execution context, then each library provided to this " +
                        "QT process will be executed in parallel.  A wait job will be executed in the foreground which will " +
                        "complete after all libraries have been quality trimmed");
    }

    public ConanParameter getRampartConfig() {
        return rampartConfig;
    }

    public ConanParameter getQualityTrimmer() {
        return qualityTrimmer;
    }

    public ConanParameter getLibs() {
        return libs;
    }

    public ConanParameter getMinLength() {
        return minLength;
    }

    public ConanParameter getMinQuality() {
        return minQuality;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getCreateConfigs() {
        return createConfigs;
    }

    public ConanParameter getJobPrefix() {
        return jobPrefix;
    }

    public ConanParameter getRunParallel() {
        return runParallel;
    }

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.rampartConfig,
                        this.qualityTrimmer,
                        this.libs,
                        this.minLength,
                        this.minQuality,
                        this.outputDir,
                        this.createConfigs,
                        this.jobPrefix,
                        this.runParallel
                }));
    }
}
