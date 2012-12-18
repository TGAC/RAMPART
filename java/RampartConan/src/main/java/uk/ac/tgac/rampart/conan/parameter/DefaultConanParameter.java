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
package uk.ac.tgac.rampart.conan.parameter;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;
import uk.ac.tgac.rampart.conan.parameter.Optionable;
import uk.ac.tgac.rampart.conan.parameter.Transientable;

/**
 * @author Dan Mapleson
 */
public class DefaultConanParameter extends AbstractConanParameter implements Optionable, Transientable  {

	private static final long serialVersionUID = 1454972239759100291L;

	private boolean optional;
	private boolean t;

	public DefaultConanParameter(String name) {
		this(name, name, false);
	}
	
	public DefaultConanParameter(String name, String description, boolean isBoolean) {
		this(name, description, isBoolean, false);
	}
	
	public DefaultConanParameter(String name, String description, boolean isBoolean,
			boolean isOptional) {		
		this(name, description, isBoolean, isOptional, false);
	}
	
	public DefaultConanParameter(String name, String description, boolean isBoolean,
			boolean isOptional, boolean isTransient) {
		super(name, description, isBoolean);
		this.optional = isOptional;
		this.t = isTransient;
	}
	
	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public boolean isTransient() {
		return t;
	}

	@Override
	public void setTransient(boolean t) {
		this.t = t;
	}
}
