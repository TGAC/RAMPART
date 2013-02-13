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
package uk.ac.tgac.rampart.pipeline.tool.proc.internal.util.clean;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;

public class CleanJobProcess extends AbstractConanProcess {

    public CleanJobProcess(CleanJobArgs args) {
        super("", args, new CleanJobParams());
    }

    @Override
    public String getName() {
        return "CleanJob";
    }

    @Override
    public String getCommand() {

        RampartJobFileStructure jobFs = new RampartJobFileStructure(((CleanJobArgs) this.getProcessArgs()).getJobDir());

        String[] cmdParts = new String[]{
                "rm -R -f " + jobFs.getReadsDir().getPath(),
                "rm -R -f " + jobFs.getMassDir().getPath(),
                "rm -R -f " + jobFs.getImproverDir().getPath(),
                "rm -R -f " + jobFs.getReportDir().getPath(),
                "rm -R -f " + jobFs.getLogDir().getPath()
        };

        String command = StringUtils.join(cmdParts, "; ");

        return command;
    }
}
