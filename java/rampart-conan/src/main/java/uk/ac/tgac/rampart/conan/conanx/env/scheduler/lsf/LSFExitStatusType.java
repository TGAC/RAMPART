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

import uk.ac.tgac.rampart.conan.conanx.env.scheduler.ExitStatusType;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:41
 */
public enum LSFExitStatusType {

    ENDED {
        @Override
        public ExitStatusType getExitStatus() {
            return ExitStatusType.COMPLETED_FAILED;
        }

        @Override
        public String getCommand() {
            return "ended";
        }


    },
    DONE {
        @Override
        public ExitStatusType getExitStatus() {
            return ExitStatusType.COMPLETED_SUCCESS;
        }

        public String getCommand() {
            return "done";
        }
    };

    public abstract ExitStatusType getExitStatus();
    public abstract String getCommand();

    public static LSFExitStatusType select(ExitStatusType type) {
        if (type == ExitStatusType.COMPLETED_SUCCESS)
            return DONE;
        else if (type == ExitStatusType.COMPLETED_FAILED) {
            return ENDED;
        }

        return ENDED;
    }
}

