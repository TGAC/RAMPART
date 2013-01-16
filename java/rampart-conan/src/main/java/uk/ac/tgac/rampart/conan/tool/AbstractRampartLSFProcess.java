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
package uk.ac.tgac.rampart.conan.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;
import uk.ac.tgac.rampart.conan.conanx.process.AbstractExtendedLSFProcess;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractRampartLSFProcess extends AbstractExtendedLSFProcess {

	private static final long serialVersionUID = -1289512647365536443L;

	private static Logger log = LoggerFactory.getLogger(AbstractRampartLSFProcess.class);
	
	private String name;
	private String componentName;
	private String command;
	private String paramPrefix;
	private Set<ConanParameter> params;

	public AbstractRampartLSFProcess(String name, String componentName, String command, String paramPrefix,
			Set<ConanParameter> params) {
		
		// Rampart LSF defaults
		this.setQueueName("production");
        this.setProjectName("RAMPART");

		// Standard job name
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		this.setJobName(dateFormat.format(date) + "_" + this.getClass().getSimpleName());

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
		return this.params;
	}
	
	public String loadToolCommand() {
		return ToolCommandLoader.getInstance().getLoadToolCommand(this.componentName);
	}
}
