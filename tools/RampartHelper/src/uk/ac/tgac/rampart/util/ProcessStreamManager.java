package uk.ac.tgac.rampart.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import static uk.ac.tgac.rampart.util.RampartLogger.LOGGER;

/**
 * Manages streams to and from an external process
 * 
 * @author Dan Mapleson
 */
public class ProcessStreamManager {
	private Process process;
	private String output_prefix;
	private int return_code;
	private List<String> output_data;

	/**
	 * Creates a new instance of a process stream manager for a given process
	 * 
	 * @param process
	 *            The process to manage streams for
	 * @param output_prefix
	 *            An prefix to display before any output messages
	 */
	public ProcessStreamManager(Process process, String output_prefix) {
		this.process = process;
		this.output_prefix = output_prefix;
		this.return_code = 0;
		this.output_data = null;
	}

	/**
	 * Pump data into a running process via its input stream.
	 * 
	 * @param data_in
	 *            The lines of data to pump in
	 * @param terminator
	 *            A terminator message which should indicate to the process that all input has been provided and it can
	 *            now finish.
	 */
	public void enterData(List<String> data_in, String terminator) {
		/*
		 * Should always finish before output and error streams if all went well. Not sure what happens if the
		 * process terminates before all data has been consumed. Possibly we need to put some error handling in here
		 * to ensure this thread is properly terminated in all cases, otherwise we might leave dangling threads.
		 */
		new ProcessInputHandler(process.getOutputStream(), data_in, terminator, output_prefix).start();
	}

	/**
	 * Instructs process to run in the foreground. i.e. forces current thread to wait until the process has completed.
	 * 
	 * @param record_output
	 *            Whether to record all output in this instance for later retrieval by the client
	 * @return return code from the process
	 * @throws Exception
	 *             Thrown if there were any unexpected problems
	 */
	public int runInForeground(boolean record_output) throws Exception {
		ProcessOutputHandler err = new ProcessOutputHandler(process.getErrorStream(), output_prefix, true, false);
		ProcessOutputHandler out = new ProcessOutputHandler(process.getInputStream(), output_prefix, false,
				record_output);

		err.start();
		out.start();

		// Wait until the end of the process
		this.return_code = process.waitFor();

		// Make sure the output streams have finished processing too.
		err.join();
		out.join();

		this.output_data = out.getOutput();

		return this.return_code;
	}

	/**
	 * Instructs the process to run in the background. i.e. the current thread carries on with any other work it has to
	 * do. The managed process will finish of its on accord.
	 * 
	 * @throws Exception
	 *             Thrown if there were any unexpected problems
	 */
	public void runInBackground(boolean echo) throws Exception {
		new ProcessOutputHandler(process.getErrorStream(), output_prefix, true, false).start();
		new ProcessOutputHandler(process.getInputStream(), output_prefix, false, false).start();
	}

	/**
	 * Retrieves the return code of the managed process (will return 0 if called before the process has completed!)
	 * 
	 * @return The process return code.
	 */
	public int getReturnCode() {
		return this.return_code;
	}

	/**
	 * Retrieves any output data that has been captured from the managed process.
	 * 
	 * @return Output data.
	 */
	public List<String> getStandardOutput() {
		return this.output_data;
	}

	private class ProcessInputHandler extends Thread {
		private BufferedWriter dataOut;
		private List<String> data;
		private String prefix;
		private String terminator;
		private boolean success;

		public ProcessInputHandler(OutputStream dataOut, List<String> data, String terminator, String prefix) {
			this.dataOut = new BufferedWriter(new OutputStreamWriter(dataOut));
			this.data = data;
			this.prefix = prefix + ": ";
			this.terminator = terminator;
			this.success = true;
		}

		@Override
		public void run() {
			try {
				for (String line : this.data) {
					this.dataOut.write(line);
					this.dataOut.newLine();

					String message = this.prefix + line;
					LOGGER.log(Level.FINE, message);
				}

				this.dataOut.write(terminator);
				this.dataOut.newLine();

				String message = this.prefix + "terminator";
				LOGGER.log(Level.FINE, message);

				this.dataOut.flush();
				this.dataOut.close();
			} catch (IOException e) {
				String message = "Error in IO with ProcessInputHandler: " + e.getMessage();
				LOGGER.log(Level.WARNING, message);
				LOGGER.log(Level.WARNING, Tools.getStackTrace(e));
				success = false;
			}
		}

		public boolean success() {
			return this.success;
		}
	}

	private class ProcessOutputHandler extends Thread {
		private BufferedReader dataIn;
		private String prefix;
		private List<String> data;
		private boolean errorStream;
		private boolean recordOutput;
		private boolean success;

		public ProcessOutputHandler(InputStream dataIn, String outputPrefix, boolean isErrStream, boolean recordOutput) {
			this.dataIn = new BufferedReader(new InputStreamReader(dataIn));
			this.prefix = outputPrefix + " output: ";
			this.data = new ArrayList<String>();
			this.errorStream = isErrStream;
			this.recordOutput = recordOutput;
			this.success = true;
		}

		@Override
		public void run() {
			String line;
			try {
				while ((line = dataIn.readLine()) != null) {
					String message = this.prefix + line;
					Level l = errorStream ? Level.WARNING : Level.FINE;
					LOGGER.log(l, message);

					if (recordOutput) {
						data.add(line);
					}
				}

				dataIn.close();
			} catch (IOException e) {
				String message = "Error in IO with ProcessOutputHandler: " + e.getMessage();
				LOGGER.log(Level.WARNING, message);
				LOGGER.log(Level.WARNING, Tools.getStackTrace(e));
				this.success = false;
			}
		}

		public List<String> getOutput() {
			return recordOutput ? data : null;
		}

		public boolean success() {
			return success;
		}
	}
}