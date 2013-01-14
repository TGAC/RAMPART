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
package uk.ac.tgac.rampart.conan.tool.external.r;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;

public class RV2122Args implements ProcessArgs {

	private List<String> args;
	private File script;
	private File output;
	
	public List<String> getArgs() {
		return args;
	}
	public void setArgs(List<String> args) {
		this.args = args;
	}
	public File getScript() {
		return script;
	}
	public void setScript(File script) {
		this.script = script;
	}
	public File getOutput() {
		return output;
	}
	public void setOutput(File output) {
		this.output = output;
	}
	
	@Override
	public Map<ConanParameter, String> getParameterValuePairs() {
		
		Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();
		
		/*if (this.args != null) {
			pvp.put(RV2122Param.ARGS.getConanParameter(), StringUtils.join(this.args, " "));
		}
		
		if (this.script != null) {
			pvp.put(RV2122Param.SCRIPT.getConanParameter(), this.script.getPath());
		}
		
		if (this.output != null) {
			pvp.put(RV2122Param.OUTPUT.getConanParameter(), this.script.getPath());
		}   */
		
		return pvp;
	}
}
