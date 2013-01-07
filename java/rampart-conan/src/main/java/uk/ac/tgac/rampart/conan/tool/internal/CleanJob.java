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
package uk.ac.tgac.rampart.conan.tool.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.PathParameter;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;

public class CleanJob extends AbstractLSFProcess {

	private static final long serialVersionUID = 4737931558914661948L;

	@Override
	public String getName() {
		
		return "CleanJob-LSF"; 
	}

	@Override
	public Collection<ConanParameter> getParameters() {
		
		Collection<ConanParameter> a = new ArrayList<ConanParameter>();
		a.add(new PathParameter("job_dir", "The job directory to clean", false));
		return a;
	}

	@Override
	protected String getComponentName() {
		return "Cleaner";
	}

	@Override
	protected String getCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		File jobDir = new File(".");
		if (parameters.containsKey("job_dir")) {
			jobDir = new File(parameters.get("job_dir"));
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
	protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		final File parentDir = new File(System.getProperty("user.home"));

		// files to write output to
		File outputDir = new File(parentDir, ".rampart");

		// lsf output file
		return new File(outputDir, getName() + ".lsfoutput.txt")
				.getAbsolutePath();
	}

	
}
