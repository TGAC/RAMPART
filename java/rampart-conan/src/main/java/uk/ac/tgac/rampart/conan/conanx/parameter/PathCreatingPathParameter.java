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
package uk.ac.tgac.rampart.conan.conanx.parameter;

import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Dan Mapleson
 */
public class PathCreatingPathParameter extends DefaultConanParameter implements
		Optionable {

	private static final long serialVersionUID = -4219816838322594970L;
	
	public PathCreatingPathParameter(String name) {
		this(name, name, false);
	}

	public PathCreatingPathParameter(String name, String description,
			boolean isOptional) {
		super(name, description, false, isOptional, false);
	}

	@Override
	public boolean validateParameterValue(String value) {
		File f = new File(value);
		try {
			if (!StringUtils.containsWhitespace(value) && !value.contains("~")) {
				if (!f.exists()) {
					if (f.isDirectory()) {
						return (f.mkdirs());
					} else {
						if (!f.getParentFile().exists())
							f.getParentFile().mkdirs();
						return f.createNewFile() && f.delete();
					}
				}
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}