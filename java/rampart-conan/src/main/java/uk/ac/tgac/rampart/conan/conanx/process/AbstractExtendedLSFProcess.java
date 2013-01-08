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
package uk.ac.tgac.rampart.conan.conanx.process;

import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.env.arch.ge.LSFWaitCondition;

import java.util.Map;

public abstract class AbstractExtendedLSFProcess extends AbstractLSFProcess {

	private static final long serialVersionUID = -1289512647365536443L;

	private String projectName;
	private Integer memoryRequired;
	private Integer threads;
	private LSFWaitCondition waitCondition;
	private boolean openmpi;
	private String extraLsfOptions;
	
	public AbstractExtendedLSFProcess() {
		
		this.projectName = "";
		this.memoryRequired = null;
		this.threads = 1;
		this.waitCondition = null;
		this.openmpi = false;
		this.extraLsfOptions = "";
	}

	@Override
	protected String getLSFOptions(Map<ConanParameter, String> parameterStringMap) {
		
		return 	(this.projectName == null || this.projectName.isEmpty() ? "" : "-P " + this.projectName + " ") +
                (this.threads == null || this.threads <= 1 ? "" : "-n " + this.threads + " ") +
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

	public LSFWaitCondition getWaitCondition() {
		return waitCondition;
	}

	public void setWaitCondition(LSFWaitCondition waitCondition) {
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
}
