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
package uk.ac.tgac.rampart.conan.conanx.env.scheduler.lsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.ExitStatusType;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.AbstractScheduler;
import uk.ac.tgac.rampart.conan.conanx.process.NativeProcessExecutor;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessAdapter;
import uk.ac.tgac.rampart.core.utils.StringJoiner;

import java.io.File;

public class LSF extends AbstractScheduler {

    private static Logger log = LoggerFactory.getLogger(LSF.class);
    
    public static final String BSUB = "bsub";

    public LSF() {
        this(new LSFArgs());
    }

    public LSF(LSFArgs lsfArgs) {
        super(BSUB, lsfArgs);
    }


    @Override
    public ProcessAdapter createProcessAdapter(File monitorFile, int monitorInterval) {
        return new LSFProcessAdapter(monitorFile.getAbsolutePath(), monitorInterval);
    }

    @Override
    public String createCommand(String internalCommand) {

        // get email address to use as backup in case process fails
        //String backupEmail = ConanProperties.getProperty("lsf.backup.email");

        // Create command to execute
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.getSubmitCommand());
        sj.add(this.getArgs() != null, "", this.getArgs().toString());
        //sj.add("-u " + backupEmail);
        sj.add("\"" + internalCommand + " 2>&1\"");

        String cmd = sj.toString();

        return cmd;
    }

    @Override
    public String createWaitCommand(WaitCondition waitCondition) {

        StringJoiner sb = new StringJoiner(" ");
        sb.add(this.getSubmitCommand());
        sb.add(waitCondition.getCommand());

        return sb.toString();
    }

    @Override
    public WaitCondition createWaitCondition(ExitStatusType exitStatus, String condition) {
        return new LSFWaitCondition(LSFExitStatusType.select(exitStatus), condition);
    }
}
