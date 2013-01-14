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
package uk.ac.tgac.rampart.conan.conanx.env.arch.scheduler;

import uk.ac.tgac.rampart.conan.conanx.env.arch.ExitStatus;
import uk.ac.tgac.rampart.conan.conanx.env.arch.ExitStatusType;

/**
 * User: maplesod
 * Date: 11/01/13
 * Time: 14:22
 */
public class LSFExitStatus implements ExitStatus {

    private LSFExitStatusType command;

    public LSFExitStatus() {
        this(LSFExitStatusType.DONE);
    }

    public LSFExitStatus(LSFExitStatusType command) {
        this.command = command;
    }

    @Override
    public ExitStatusType getExitStatus() {
        return this.command.getExitStatus();
    }

    @Override
    public String getCommand() {
        return this.command.getCommand();
    }

    @Override
    public ExitStatus create(ExitStatusType exitStatusType) {

        for (LSFExitStatusType type : LSFExitStatusType.values()) {
            if (type.getExitStatus() == exitStatusType) {
                return new LSFExitStatus(type);
            }
        }

        return null;
    }
}
