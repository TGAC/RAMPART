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
package uk.ac.tgac.rampart.conan.process.lsf;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.GEArgs;
import uk.ac.tgac.rampart.conan.parameter.tools.ToolParameter;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;

public abstract class AbstractRampartLsfProcess extends AbstractLSFProcess {

	private static final long serialVersionUID = -1289512647365536443L;

	private static Logger log = Logger.getLogger(AbstractRampartLsfProcess.class);
	
	private String name;
	private String componentName;
	private String command;
	private String paramPrefix;
	private ToolParameter[] params;
	
	private String projectName;
	private Integer memoryRequired;
	private Integer threads;
	private LsfWaitCondition waitCondition;
	private boolean openmpi;
	private String extraLsfOptions;
	
	public AbstractRampartLsfProcess(String name, String componentName, String command, String paramPrefix,
			ToolParameter[] params) {
		
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
		
		this.name = name;
		this.componentName = componentName;
		this.command = command;
		this.paramPrefix = paramPrefix;
		this.params = params;
	}
	
	
	public String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {

		StringBuilder sb = new StringBuilder();
		sb.append(this.command);
		sb.append(" ");
		for (Map.Entry<ConanParameter, String> param : parameters.entrySet()) {
			sb.append(this.paramPrefix);
			sb.append(param.getKey().getName());
			sb.append(" ");
			sb.append(param.getValue());
			sb.append(" ");
		}

		return sb.toString().trim();

	}
	
		
	@Override
	protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		final File parentDir = new File(System.getProperty("user.home"));

		// files to write output to
		File outputDir = new File(parentDir, ".rampart");

		// lsf output file
		return new File(outputDir, getName() + ".lsfoutput.txt")
				.getAbsolutePath();
	}
	
	@Override
	protected String getCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		this.logExecuteStart(log,parameters);
				
		String loadCommand = this.loadToolCommand();
		
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
	
	@Override
	public String getJobName() {
		return super.getJobName();
	}
	
	@Override
	public void setJobName(String jobName) {
		super.setJobName(jobName);
	}
	
	@Override
	public String getQueueName() {
		return super.getQueueName();
	}
	
	@Override
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
	
	public String getName() {
		return this.name;
	}
	
	@Override
	protected String getComponentName() {
		return this.componentName;
	}
	
	public Collection<ConanParameter> getParameters() {
		Collection<ConanParameter> parameters = new ArrayList<ConanParameter>();
		for(ToolParameter p : this.params) {
			parameters.add(p.getConanParameter());
		}
		return parameters;
	}
	
	public String loadToolCommand() {
		return ToolCommandLoader.getInstance().getLoadToolCommand(this.componentName);
	}
}
