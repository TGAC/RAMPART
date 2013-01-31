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

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.Scheduler;
import uk.ac.tgac.rampart.conan.conanx.env.scheduler.WaitCondition;

import java.io.*;

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

            // This avoids any issues with checking the RSA fingerprint from the server
            session.setConfig("StrictHostKeyChecking", "no");

			session.connect();

			this.session = session;

		}
		catch(JSchException je) {

			log.error("Could not connect to remote machine", je);
			return false;
		}

        log.info("Connected to: " + connectionDetails.getUsername() + "@" + connectionDetails.getHost());
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

        log.info("Disconnected from: " + connectionDetails.getHost());
		return true;
	}

    @Override
    public int executeCommand(String command) throws ProcessExecutionException, InterruptedException {
        return this.executeCommand(command, null);
    }

    @Override
	public int executeCommand(String command, Scheduler scheduler)
            throws ProcessExecutionException, InterruptedException {

        String commandToExecute = scheduler.createCommand(command);

        int exitValue = -1;

        try {
            Channel channel = this.session.openChannel("exec");

            ((ChannelExec)channel).setCommand(commandToExecute);

            // Connect channel (executes the command on the remote session)
            channel.connect();

            // Read output
            String output = readProcessOutput(channel);

            // Disconnect channel
            channel.disconnect();

            log.info("Command: \"" + commandToExecute + "\" executed on: " + session.getHost());

            exitValue = 0;
        }
        catch(JSchException je) {
            log.error("Remote session is still connected!");
            throw new ProcessExecutionException(exitValue, je);
        }
        catch(IOException ioe) {
            log.error("Problem reading stdout and stderr from remote process");
            throw new ProcessExecutionException(exitValue, ioe);
        }

		return exitValue;
	}

    protected String readProcessOutput(Channel channel) throws IOException {

        StringBuilder output = new StringBuilder();

        InputStream inStream = channel.getInputStream();
        BufferedReader fromChannel = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
        OutputStream outStream = channel.getOutputStream();
        PrintWriter toChannel = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"));
        InputStream errStream = ((ChannelExec) channel).getErrStream();
        BufferedReader errChannel = new BufferedReader(new InputStreamReader(errStream, "UTF-8"));

        // Read the input stream
        while(true){
            String line = null;
            while((line = fromChannel.readLine()) != null){
                output.append(line);
                output.append("\n");
            }

            if(channel.isClosed()){

                log.info("SSH Channel has been closed.  Exit status: " + channel.getExitStatus());
                return output.toString();
            }

            // Consumed everything for now... wait a bit and try again
            try {
                Thread.sleep(1000);
            }
            catch(Exception ee) {
                // Ignore...
            }
        }


    }

    /**
     * This is the same as executing a command on a remote session
     * @param command The command that is to be executed in the background
     * @param scheduler The architecture describing the environment on which the command is to be executed.
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public void dispatchCommand(String command, Scheduler scheduler)
            throws ProcessExecutionException, InterruptedException {

        executeCommand(command, scheduler);
    }

    @Override
    public int waitFor(WaitCondition waitCondition, Scheduler architecture) throws ProcessExecutionException, InterruptedException {

        // I can't think of an easy way to wait for a job to finish remotely yet.
        return -1;
    }

}
