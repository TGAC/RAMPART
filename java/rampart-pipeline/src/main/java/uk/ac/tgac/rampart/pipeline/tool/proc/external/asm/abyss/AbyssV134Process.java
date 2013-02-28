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
package uk.ac.tgac.rampart.pipeline.tool.proc.external.asm.abyss;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.asm.AbstractAssemblerArgs;
import uk.ac.tgac.rampart.pipeline.tool.proc.external.asm.Assembler;

import java.io.File;
import java.io.FilenameFilter;

/**
 * User: maplesod
 * Date: 07/01/13
 * Time: 12:12
 */
public class AbyssV134Process extends AbstractConanProcess implements Assembler {

    public static final String EXE = "abyss-pe";

    public AbyssV134Process() {
        this(new AbyssV134Args());
    }

    public AbyssV134Process(AbstractAssemblerArgs args) {
        super(EXE, args, new AbyssV134Params());
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), false);
    }

    @Override
    public AbstractAssemblerArgs getArgs() {
        return (AbstractAssemblerArgs) this.getProcessArgs();
    }

    @Override
    public boolean makesUnitigs() {
        return true;
    }

    @Override
    public boolean makesContigs() {
        return true;
    }

    @Override
    public boolean makesScaffolds() {
        return true;
    }

    @Override
    public File getUnitigsFile() {
        File[] files = this.getArgs().getOutputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("-unitigs.fa");
            }
        });

        return (files != null && files.length == 1) ? files[0] : null;
    }

    @Override
    public File getContigsFile() {
        File[] files = this.getArgs().getOutputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("-contigs.fa");
            }
        });

        return (files != null && files.length == 1) ? files[0] : null;
    }

    @Override
    public File getScaffoldsFile() {
        File[] files = this.getArgs().getOutputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("-scaffolds.fa");
            }
        });

        return (files != null && files.length == 1) ? files[0] : null;
    }

    @Override
    public String getName() {
        return "Abyss_V1.3.4";
    }
}
