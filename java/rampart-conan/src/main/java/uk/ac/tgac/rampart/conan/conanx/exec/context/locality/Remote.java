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
package uk.ac.tgac.rampart.conan.conanx.exec.context.locality;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.Scheduler;
import uk.ac.tgac.rampart.conan.conanx.exec.context.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.exec.task.monitor.TaskAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Remote implements Locality {

	private static Logger log = LoggerFactory.getLogger(Remote.class);
	
	private ConnectionDetails connectionDetails;
	private Session session;
    private Channel channel;

	public Remote(ConnectionDetails connectionDetails) {
		this.setConnectionDetails(connectionDetails);
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

            this.channel = this.session.openChannel("exec");


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

        this.channel.disconnect();
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
    public int monitoredExecute(String command, TaskAdapter taskAdapter) {
        throw new UnsupportedOperationException("Can't monitor progress on a remote session");
    }

    @Override
    public int execute(String command) throws ProcessExecutionException, InterruptedException {

        String[] output;
        int exitCode;

        try {

            ((ChannelExec)this.channel).setCommand(command);

            // Connect channel (executes the command on the remote session)
            this.channel.connect();

            // Read output
            output = readProcessOutput(this.channel);

            exitCode = this.channel.getExitStatus();

            log.info("Command: \"" + command + "\" executed on: " + session.getHost());
        }
        catch(JSchException je) {
            throw new ProcessExecutionException(-1, je);
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

		return exitCode;
	}


    /**
     * This is the same as executing a command on a remote session
     * @param command The command that is to be executed in the background
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public void dispatch(String command)
            throws ProcessExecutionException, InterruptedException {

        throw new UnsupportedOperationException("Can't dispatch tasks on a remote session");
    }






    protected String[] readProcessOutput(Channel channel) throws IOException {

        List<String> output = new ArrayList<String>();

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
                output.add(line);
            }

            if(channel.isClosed()){

                log.info("SSH Channel has been closed.  Exit status: " + channel.getExitStatus());
                return output.toArray(new String[]{});
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


    @Override
    public int waitFor(WaitCondition waitCondition) throws ProcessExecutionException, InterruptedException {

        throw new UnsupportedOperationException("Can't wait for dispatched jobs from a remote locality yet");
    }

}
