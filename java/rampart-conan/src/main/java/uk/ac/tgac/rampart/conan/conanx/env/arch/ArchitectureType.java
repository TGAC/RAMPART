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
package uk.ac.tgac.rampart.conan.conanx.env.arch;

import uk.ac.tgac.rampart.conan.conanx.env.arch.scheduler.SchedulerType;

public enum ArchitectureType {

	SINGLE
	{
		@Override
		public Architecture create() {
			return new Single();
		}
	},
	GRID_ENGINE {
		@Override
		public Architecture create() {

			// Select LSF as the default grid engine.  If user wants PBS they'll have to do the leg work.
            return SchedulerType.LSF.create();
        }
	};
	
	public abstract Architecture create();
	
	public static ArchitectureType GE = GRID_ENGINE;
}
