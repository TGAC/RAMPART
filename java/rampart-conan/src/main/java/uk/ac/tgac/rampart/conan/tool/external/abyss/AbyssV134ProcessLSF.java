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
package uk.ac.tgac.rampart.conan.tool.external.abyss;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.cli.ToolCommandLoader;
import uk.ac.tgac.rampart.conan.conanx.env.arch.scheduler.SchedulerArgs;
import uk.ac.tgac.rampart.conan.tool.AbstractRampartLSFProcess;
import uk.ac.tgac.rampart.conan.tool.args.DeBrujinAssemblerArgs;

import java.io.File;
import java.util.Map;

public class AbyssV134ProcessLSF extends AbstractRampartLSFProcess {

	private static final long serialVersionUID = -7311765587559938252L;
	
	public static final String NAME = "Abyss_V1.3.4-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.ABYSS_1_3_4;
	public static final String COMMAND = "abyss-pe";
	public static final String PARAM_PREFIX = "";

    private AbyssV134Process abyssProcess;
	
	public AbyssV134ProcessLSF(AbyssV134Process abyssProcess) {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				new AbyssV134Params().getConanParameters());
		
		this.abyssProcess = abyssProcess;

        this.setMemoryRequired(60000);
		this.setThreads(8);
		
		// TGAC Cluster specific command (Abyss doesn't like Intel nodes, so avoid those)
		this.setExtraLsfOptions("-Rselect[hname!='n57142.tgaccluster']");
	}

	protected void setFromGEArgs(SchedulerArgs geArgs) {
		this.setJobName(geArgs.getJobName());
		this.setQueueName(geArgs.getQueueName());
		this.setMemoryRequired(geArgs.getMemoryMB());
		this.setThreads(geArgs.getThreads());
	}

	public void execute(DeBrujinAssemblerArgs assemblerArgs, SchedulerArgs geArgs, File workingDir)
			throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
		
		setFromGEArgs(geArgs);

		//TODO Somehow need to change dir
		
		this.execute(assemblerArgs.getParameterValuePairs());
		
		//TODO And change back to where we were before! :s
	}


    @Override
    protected String getCommand(Map<ConanParameter, String> parameters) throws IllegalArgumentException {
        //return this.abyssProcess.getFullCommand();
        return "";
    }
}