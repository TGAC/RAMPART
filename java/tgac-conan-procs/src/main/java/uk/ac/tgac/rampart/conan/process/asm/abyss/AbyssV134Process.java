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
package uk.ac.tgac.rampart.conan.process.asm.abyss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.tgac.rampart.conan.process.asm.Assembler;
import uk.ac.tgac.rampart.conan.process.asm.AssemblerArgs;

import java.io.File;
import java.io.FilenameFilter;

/**
 * User: maplesod
 * Date: 07/01/13
 * Time: 12:12
 */
public class AbyssV134Process extends AbstractConanProcess implements Assembler {

    private static Logger log = LoggerFactory.getLogger(AbyssV134Process.class);

    public static final String EXE = "abyss-pe";

    public AbyssV134Process() {
        this(new AbyssV134Args());
    }

    public AbyssV134Process(AssemblerArgs args) {
        super(EXE, args, new AbyssV134Params());

        String pwdFull = new File(".").getAbsolutePath();
        String pwd = pwdFull.substring(0, pwdFull.length() - 1);

        this.addPreCommand("cd " + args.getOutputDir().getAbsolutePath());
        this.addPostCommand("cd " + pwd);
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), false);
    }

    @Override
    public AssemblerArgs getArgs() {
        return (AssemblerArgs) this.getProcessArgs();
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

    public File findUnitigsFile() {
        File[] files = this.getArgs().getOutputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("-unitigs.fa");
            }
        });

        return (files != null && files.length == 1) ? files[0] : null;
    }

    public File findContigsFile() {
        File[] files = this.getArgs().getOutputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("-contigs.fa");
            }
        });

        return (files != null && files.length == 1) ? files[0] : null;
    }

    public File findScaffoldsFile() {
        File[] files = this.getArgs().getOutputDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("-scaffolds.fa");
            }
        });

        return (files != null && files.length == 1) ? files[0] : null;
    }

    @Override
    public File getUnitigsFile() {

        AbyssV134Args abyssV134Args = (AbyssV134Args)this.getArgs();
        File unitigsFile = new File(abyssV134Args.getOutputDir(), abyssV134Args.getName() + "-unitigs.fa");
        return unitigsFile;
    }

    @Override
    public File getContigsFile() {

        AbyssV134Args abyssV134Args = (AbyssV134Args)this.getArgs();
        File unitigsFile = new File(abyssV134Args.getOutputDir(), abyssV134Args.getName() + "-contigs.fa");
        return unitigsFile;
    }

    @Override
    public File getScaffoldsFile() {

        AbyssV134Args abyssV134Args = (AbyssV134Args)this.getArgs();
        File unitigsFile = new File(abyssV134Args.getOutputDir(), abyssV134Args.getName() + "-scaffolds.fa");
        return unitigsFile;
    }

    @Override
    public boolean usesOpenMpi() {
        return true;
    }

    @Override
    public String getName() {
        return "Abyss_V1.3.4";
    }
}
