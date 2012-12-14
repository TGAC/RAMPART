package uk.ac.tgac.rampart.conan.process.lsf;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

public abstract class AbstractRampartLsfProcess extends AbstractLSFProcess {

	private static final long serialVersionUID = -1289512647365536443L;

	private static Logger log = Logger.getLogger(AbstractRampartLsfProcess.class);
	
	private String projectName;
	private Integer memoryRequired;
	private Integer threads;
	private LsfWaitCondition waitCondition;
	private boolean openmpi;
	private String extraLsfOptions;
	
	public AbstractRampartLsfProcess() {
		// Assume we want to default to production queue
		this.setQueueName("production");
		
		// Standard job name
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		this.setJobName(dateFormat.format(date) + "_" + this.getClass().getSimpleName());
				
		this.projectName = "RAMPART";
		this.memoryRequired = null;
		this.threads = 1;
		this.waitCondition = null;
		this.openmpi = false;
		this.extraLsfOptions = "";
	}
	
	/**
	 * Override this method for loading any dependencies onto the environment PATH variable. 
	 * @return The command that brings the tool onto the PATH
	 */
	protected abstract String getLoadToolCommand(); 
	
	/**
	 * Override this method to return the command that this tool process should execute.
	 * @return The command to execute.
	 */
	protected abstract String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException;
	
		
	@Override
	protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		final File parentDir = new File(System.getProperty("user.home"));

		// files to write output to
		File outputDir = new File(parentDir, ".conan");

		for (ConanParameter parameter : parameters.keySet()) {
			if (parameter.getName().contains("Accession")) {
				outputDir = new File(new File(parentDir, ".conan"),
						parameters.get(parameter));
				break;
			}
		}

		// lsf output file
		return new File(outputDir, getName() + ".lsfoutput.txt")
				.getAbsolutePath();
	}
	
	@Override
	protected String getCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		this.logExecuteStart(log,parameters);
				
		String loadCommand = this.getLoadToolCommand();
		
		List<String> commands = new ArrayList<String>();
		
		if (loadCommand != null && !loadCommand.isEmpty()) {
			commands.add(loadCommand);
		}
		
		commands.add(this.getToolCommand(parameters));
		
		String command = StringUtils.join(commands, "; ");
		
		return command;
	}
	
	
	@Override
	protected String getLSFOptions(Map<ConanParameter, String> parameterStringMap) {
		
		return 	(this.threads == null || this.threads <= 1 ? "" : "-n " + this.threads + " ") +
				(this.openmpi == false ? "" : "-a openmpi ") +
				(this.waitCondition == null ? "" : this.waitCondition.toString() + " ") +
				(this.extraLsfOptions == null || this.extraLsfOptions.equals("") ? "" : this.extraLsfOptions);
	}

	protected String createUsageString(Map<ConanParameter, String> parameterStringMap) {
		
		String span = this.threads != null && this.threads > 1 ? "span[ptile=" + this.threads + "]" : "";
		String rusage = this.memoryRequired != null && this.memoryRequired > 0 ?
				"rusage[mem=" + this.memoryRequired + "]" : "";			
			
		return !span.isEmpty() || !rusage.isEmpty() ? "-R" + rusage + span : "";
	}
	
	public String getJobName() {
		return super.getJobName();
	}
	
	public void setJobName(String jobName) {
		super.setJobName(jobName);
	}
	
	public String getQueueName() {
		return super.getQueueName();
	}
	
	public void setQueueName(String queueName) {
		super.setQueueName(queueName);
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Retrieves the amount of memory that's been requested for this Rampart LSF job in MB. 
	 */
	public int getMemoryRequired() {
		return memoryRequired;
	}

	/**
	 * Sets the amount of memory that will be requested for this Rampart LSF job in MB. 
	 */
	public void setMemoryRequired(int memoryRequired) {
		this.memoryRequired = memoryRequired;
	}

	public void setOpenmpi(boolean openmpi) {
		this.openmpi = openmpi;
	}

	public Integer getThreads() {
		return threads;
	}

	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	public LsfWaitCondition getWaitCondition() {
		return waitCondition;
	}

	public void setWaitCondition(LsfWaitCondition waitCondition) {
		this.waitCondition = waitCondition;
	}

	public Boolean getOpenmpi() {
		return openmpi;
	}

	public void setOpenmpi(Boolean openmpi) {
		this.openmpi = openmpi;
	}

	public String getExtraLsfOptions() {
		return extraLsfOptions;
	}

	public void setExtraLsfOptions(String extraLsfOptions) {
		this.extraLsfOptions = extraLsfOptions;
	}
	
	public void logExecuteStart(Logger executeLogger, Map<ConanParameter, String> parameters) {
		log.debug("Executing " + getName() + 
				" with the following parameters: " + parameters.toString());
	}
	
}
