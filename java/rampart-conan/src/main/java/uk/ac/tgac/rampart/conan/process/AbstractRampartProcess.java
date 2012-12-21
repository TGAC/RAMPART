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
package uk.ac.tgac.rampart.conan.process;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

public abstract class AbstractRampartProcess implements RampartProcess {

	@Override
	public boolean execute(Map<ConanParameter, String> parameters)
			throws ProcessExecutionException, IllegalArgumentException,
			InterruptedException {
		
		String[] commands = new String[]{
			this.getPreCommand(),
			this.getCommand(null),
			this.getPostCommand()
		};
		
		String command = StringUtils.join(commands, "; ");
		
		return false;
	}


}
