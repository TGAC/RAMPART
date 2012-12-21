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
package uk.ac.tgac.rampart.conan.env.arch.ge;

public class LSFWaitCondition {

	public enum ExitStatus {
		ENDED("ended"),
		DONE("done");
		
		private String cmd;
		
		private ExitStatus(String cmd) {
			this.cmd = cmd;
		}
		
		public String getCommand() {
			return this.cmd;
		}
	}
	
	private ExitStatus exitStatus;
	private String condition;
	
	public LSFWaitCondition(ExitStatus exitStatus, String condition) {
		super();
		this.exitStatus = exitStatus;
		this.condition = condition;
	}

	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	public String getCondition() {
		return condition;
	}
	
	@Override
	public String toString() {
		return "-w " + this.exitStatus.getCommand() + "(" + this.condition + ")";
	}
	
}
