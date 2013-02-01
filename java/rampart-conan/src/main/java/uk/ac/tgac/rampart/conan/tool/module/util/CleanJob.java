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
package uk.ac.tgac.rampart.conan.tool.module.util;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.parameter.PathParameter;
import uk.ac.tgac.rampart.conan.conanx.process.DefaultExtendedConanProcess;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CleanJob extends DefaultExtendedConanProcess {

	private static final long serialVersionUID = 4737931558914661948L;

    public static final String PARAM_JOB_DIR = "job_dir";

    protected CleanJob(String exePath) {
        super(exePath);
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public String getName() {
		
		return "CleanJob-LSF"; 
	}

	@Override
	public Collection<ConanParameter> getParameters() {
		
		Collection<ConanParameter> a = new ArrayList<ConanParameter>();
		a.add(new PathParameter(PARAM_JOB_DIR, "The job directory to clean", false));
		return a;
	}

	protected String buildCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		File jobDir = new File(".");
		if (parameters.containsKey(PARAM_JOB_DIR)) {
			jobDir = new File(parameters.get(PARAM_JOB_DIR));
		}
		
		RampartJobFileStructure jobFs = new RampartJobFileStructure(jobDir);
		
		String[] cmdParts = new String[] {
				"rm -R -f " + jobFs.getReadsDir().getPath(),
				"rm -R -f " + jobFs.getMassDir().getPath(),
				"rm -R -f " + jobFs.getImproverDir().getPath(),
				"rm -R -f " + jobFs.getReportDir().getPath(),
				"rm -R -f " + jobFs.getLogDir().getPath()
		};
		
		String command = StringUtils.join(cmdParts, "; ");
		
		return command;
	}


    @Override
    public String getCommand() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
