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
package uk.ac.tgac.rampart.conan.conanx.env;

import java.net.ConnectException;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.arch.Architecture;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Locality;

public class DefaultEnvironment implements Environment {

	private Locality locality;
	private Architecture architecture;
    private EnvironmentArgs environmentArgs;
		
	public DefaultEnvironment(Locality locality, Architecture architecture, EnvironmentArgs args) {

        this.locality = locality;
		this.architecture = architecture;
        this.environmentArgs = args;
	}

    public DefaultEnvironment(Environment env) {

        this.locality = env.getLocality();
        this.architecture = env.getArchitecture();
        this.environmentArgs = env.getEnvironmentArgs().copy();
    }

    @Override
	public Locality getLocality() {
		return locality;
	}

	public void setLocality(Locality locality) {
		this.locality = locality;
	}

    @Override
	public Architecture getArchitecture() {
		return architecture;
	}

	public void setArchitecture(Architecture architecture) {
		this.architecture = architecture;
	}

    @Override
    public EnvironmentArgs getEnvironmentArgs() {
        return environmentArgs;
    }

    public void setEnvironmentArgs(EnvironmentArgs environmentArgs) {
        this.environmentArgs = environmentArgs;
    }

    @Override
	public void submitProcess(ConanProcess process)
			throws IllegalArgumentException, ProcessExecutionException,
			InterruptedException, ConnectException {
		
		if (!this.locality.establishConnection()) {
			throw new ConnectException("Could not establish connection to the terminal.  Process " +
					process.getName() + " will not be submitted.");
		}
		
		// Do we need to do any checking around this?
		this.locality.submitProcess(process, this.environmentArgs, this.architecture);
		
		if (!this.locality.disconnect()) {
			throw new ConnectException("Process was submitted but could not disconnect the terminal session.  Future jobs may not work.");
		}
		
	}

    @Override
    public void submitCommand(String command) throws IllegalArgumentException, ProcessExecutionException, InterruptedException, ConnectException {
        if (!this.locality.establishConnection()) {
            throw new ConnectException("Could not establish connection to the terminal.  Command " +
                    command + " will not be submitted.");
        }

        // Do we need to do any checking around this?
        this.locality.submitCommand(command, this.environmentArgs, this.architecture);

        if (!this.locality.disconnect()) {
            throw new ConnectException("Command was submitted but could not disconnect the terminal session.  Future jobs may not work.");
        }
    }

    @Override
	public void setup(Locality locality, Architecture architecture, EnvironmentArgs args) {
		
		this.setLocality(locality);
		this.setArchitecture(architecture);
        this.setEnvironmentArgs(args);
	}

    @Override
    public Environment copy() {
        return new DefaultEnvironment(this);
    }
	
}
