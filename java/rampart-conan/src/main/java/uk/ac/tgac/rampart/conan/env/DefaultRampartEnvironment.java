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
package uk.ac.tgac.rampart.conan.env;

import java.net.ConnectException;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.env.arch.Architecture;
import uk.ac.tgac.rampart.conan.env.locality.Locality;
import uk.ac.tgac.rampart.conan.process.RampartProcess;

public class DefaultRampartEnvironment implements RampartEnvironment {

	private Locality locality;
	private Architecture architecture;
		
	public DefaultRampartEnvironment(Locality locality,
			Architecture architecture) {
		super();
		this.locality = locality;
		this.architecture = architecture;
	}
	
	public Locality getLocality() {
		return locality;
	}
	public void setLocality(Locality locality) {
		this.locality = locality;
	}
	public Architecture getArchitecture() {
		return architecture;
	}
	public void setArchitecture(Architecture architecture) {
		this.architecture = architecture;
	}

	@Override
	public void submitProcess(RampartProcess rampartProcess, EnvironmentArgs args)
			throws IllegalArgumentException, ProcessExecutionException,
			InterruptedException, ConnectException {
		
		if (!this.locality.establishConnection()) {
			throw new ConnectException("Could not establish connection to the terminal.  Process " +
					rampartProcess.getName() + " will not be submitted.");
		}
		
		// Do we need to do any checking around this?
		this.locality.submitProcess(rampartProcess, args, this.architecture);
		
		if (!this.locality.disconnect()) {
			throw new ConnectException("Process was submitted but could not disconnect the terminal session.  Future jobs may not work.");
		}
		
	}

	@Override
	public void setup(Locality locality, Architecture architecture) {
		
		this.setLocality(locality);
		this.setArchitecture(architecture);
	}
	
}
