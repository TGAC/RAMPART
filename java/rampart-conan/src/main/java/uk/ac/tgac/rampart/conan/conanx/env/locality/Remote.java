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
package uk.ac.tgac.rampart.conan.conanx.env.locality;

import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;
import uk.ac.tgac.rampart.conan.conanx.env.arch.Architecture;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Remote implements Locality {

	private static Logger log = LoggerFactory.getLogger(Remote.class);
	
	private ConnectionDetails connectionDetails;
	private Session session;

	public Remote(ConnectionDetails connectionDetails) {
		this.setConnectionDetails(connectionDetails);
	}

	public void createSession()
			throws JSchException {
		
	}

	public void terminateSession() {

		if (this.session != null) {
			this.session.disconnect();
		}
	}

	public ConnectionDetails getConnectionDetails() {
		return connectionDetails;
	}

	public void setConnectionDetails(ConnectionDetails connectionDetails) {
		this.connectionDetails = connectionDetails;
	}

	@Override
	public boolean establishConnection() {
		
		try {
			JSch jsch = new JSch();
	
			Session session = jsch.getSession(connectionDetails.getUsername(),
					connectionDetails.getHost(), connectionDetails.getPort());
	
			session.setPassword(connectionDetails.getPassword());

			session.connect();
	
			this.session = session;
		}
		catch(JSchException je) {
			
			log.error("Could not connect to remote machine", je);
			return false;
		}
		
		return true;
	}

	@Override
	public boolean disconnect() {
		
		this.session.disconnect();
		
		if (this.session.isConnected()) {
			
			// this really shouldn't happen but let's check anyway
			log.error("Remote session is still connected!");
			return false;
		}
					
		return true;
	}

	@Override
	public boolean submitCommand(String command, EnvironmentArgs args, Architecture architecture)
			throws IllegalArgumentException, ProcessExecutionException,
			InterruptedException, ConnectException {

		try {
			
			// this is all rubbish atm.  I know I'll need to do something with the session here though.
			this.session.connect();
			this.session.run();
			architecture.submitCommand(command, args);
		}
		catch(JSchException je) {
			
			ConnectException ce = new ConnectException("Connection issues with remote machine");
			ce.initCause(je);
			throw ce;
		}

		return true;
	}

}
