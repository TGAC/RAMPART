/*
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
 */
package uk.ac.tgac.rampart.stage.util;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import uk.ac.tgac.conan.core.util.XmlHelper;

import java.util.ArrayList;

/**
 * User: maplesod
 * Date: 14/08/13
 * Time: 10:32
 */
public class VariableRange {

    private String name;
    private ArrayList<String> values;

    // Xml Config keys
    private static final String KEY_ATTR_NAME = "name";
    private static final String KEY_ATTR_VALUES = "values";


    /**
     * Generate default coverage range (just 75X and ALL)
     */
    public VariableRange() {

        this.name = "";
        this.values = new ArrayList<>();
    }

    /**
     * Generate a coverage range from a comma separated list
     * @param list Coverage values separated by commas.  Whitespace is tolerated.
     */
    public VariableRange(String name, String list) {
        this();
        this.name = name;
        this.setFromString(list);
    }

    /**
     * Create a coverage range from an Xml element
     * @param ele
     */
    public VariableRange(Element ele) {

        this();

        if (ele.hasAttribute(KEY_ATTR_NAME)) {
            this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
        }
        else {
            throw new IllegalArgumentException("Xml element for variable range does not contain the \"" + KEY_ATTR_NAME + "\" attribute");
        }

        if (ele.hasAttribute(KEY_ATTR_VALUES)) {
            this.setFromString(XmlHelper.getTextValue(ele, KEY_ATTR_VALUES));
        }
        else {
            throw new IllegalArgumentException("Xml element for variable range does not contain the \"" + KEY_ATTR_VALUES + "\" attribute");
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    protected final void setFromString(String list) {
        String[] parts = list.split(",");
        for(String part : parts) {
            this.values.add(part.trim());
        }
    }


    public String toString() {
        return "Variable Range: " + this.name + ": {" + StringUtils.join(this.getValues(), ",") + "}";
    }
}
