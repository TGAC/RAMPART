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
package uk.ac.tgac.rampart.conan.conanx.param;

import org.springframework.util.StringUtils;

/**
 * @author Dan Mapleson
 */
public class PathParameter extends DefaultConanParameter {

	private static final long serialVersionUID = -7472585120471067656L;

	public PathParameter(String name) {
		this(name, name, false);
	}

	public PathParameter(String name, String description, boolean isOptional) {
		super(name, description, false, isOptional, false);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return !StringUtils.containsWhitespace(value) && !value.contains("~");
	}
}