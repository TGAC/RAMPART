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
package uk.ac.tgac.rampart.conan.tool.abyss;

import java.io.File;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.parameter.GEArgs;
import uk.ac.tgac.rampart.conan.parameter.tools.DeBrujinAssembler;
import uk.ac.tgac.rampart.conan.parameter.tools.DeBrujinAssemblerArgs;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;

@ServiceProvider
public class AbyssV134LsfProcess extends AbstractRampartLsfProcess implements DeBrujinAssembler {

	private static final long serialVersionUID = -7311765587559938252L;
	
	public static final String NAME = "Abyss_V1.3.4-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.ABYSS_1_3_4;
	public static final String COMMAND = "abyss-pe";
	public static final String PARAM_PREFIX = "";
	
	private File workingDir;
	
	public AbyssV134LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				AbyssV134Param.values());
		
		this.setMemoryRequired(60000);
		this.setThreads(8);
		
		// TGAC Cluster specific command (Abyss doesn't like Intel nodes, so avoid those)
		this.setExtraLsfOptions("-Rselect[hname!='n57142.tgaccluster']");
		
		this.workingDir = null;
	}
	
	@Override
	public String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		StringBuilder sb = new StringBuilder();
		sb.append(COMMAND);
		sb.append(" ");		
		for(Map.Entry<ConanParameter, String> param : parameters.entrySet()) {
			sb.append(param.getKey());
			sb.append("=");
			sb.append(param.getValue());
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}
	
	protected void setFromGEArgs(GEArgs geArgs) {
		this.setJobName(geArgs.getJobName());
		this.setProjectName(geArgs.getProjectName());
		this.setQueueName(geArgs.getQueueName());
		this.setMemoryRequired(geArgs.getMemoryMB());
		this.setThreads(geArgs.getThreads());
	}

	@Override
	public void execute(DeBrujinAssemblerArgs assemblerArgs, GEArgs geArgs, File workingDir) 
			throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
		
		setFromGEArgs(geArgs);
		
		//TODO Somehow need to change dir
		
		this.execute(assemblerArgs.getParameterValuePairs());
		
		//TODO And change back to where we were before! :s
	}


	@Override
	public boolean makesScaffolds() {
		return true;
	}
}