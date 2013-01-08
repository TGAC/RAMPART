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

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

/**
 * @author Dan Mapleson
 */
public class PairedPathParameter extends AbstractConanParameter implements
		Optionable, Transientable {

	private static final long serialVersionUID = -3887080676113159103L;
	private boolean optional = false;
	private boolean t = false;

	public PairedPathParameter(String name) {
		super(name);
	}

	public PairedPathParameter(String name, boolean isBoolean) {
		super(name, isBoolean);
	}

	public PairedPathParameter(String name, String description) {
		super(name, description);
	}

	public PairedPathParameter(String name, String description, boolean isBoolean) {
		super(name, description, isBoolean);
	}

	@Override
	public boolean validateParameterValue(String value) {
		return !value.contains("~") && !value.startsWith("'") && !value.endsWith("'");
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