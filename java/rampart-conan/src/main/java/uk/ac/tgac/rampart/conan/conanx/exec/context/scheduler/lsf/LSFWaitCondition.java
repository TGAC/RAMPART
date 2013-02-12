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
package uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.lsf;

import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.ExitStatus;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.ExitStatusType;

public class LSFWaitCondition implements WaitCondition {

    private LSFExitStatusType exitStatus;
    private String condition;

    public LSFWaitCondition(LSFExitStatusType exitStatus, String condition) {
        super();
        this.exitStatus = exitStatus;
        this.condition = condition;
    }

    /*public LSFWaitCondition(ExitStatusType exitStatusType, String condition) {
        super();
        this.exitStatus = (LSFExitStatusType) new LSFExitStatus().create(exitStatusType).getExitStatus();
        this.condition = condition;
    }      */

    @Override
    public ExitStatus createExitStatus(ExitStatusType exitStatusType) {

        /*if (exitStatusType == ExitStatusType.COMPLETED_SUCCESS)
            return LSFExitStatusType.DONE;
        else if (exitStatusType == ExitStatusType.COMPLETED_FAILED)
            return LSFExitStatusType.ENDED;  */

        //return exitStatusType.;  //To change body of implemented methods use File | Settings | File Templates.
        return null;
    }

    @Override
    public ExitStatus getExitStatus() {
        return new LSFExitStatus(exitStatus);
    }

    @Override
    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = (LSFExitStatusType) exitStatus;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String getCommand() {
        return "-w " + this.exitStatus.getCommand() + "(" + this.condition + ")";
    }

    @Override
    public String toString() {
        return getCommand();
    }

}
