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
public class CoverageRange extends ArrayList<Integer> {

    // Xml Config keys
    private static final String KEY_ATTR_CVG_MIN = "min";
    private static final String KEY_ATTR_CVG_MAX = "max";
    private static final String KEY_ATTR_CVG_STEP = "step";
    private static final String KEY_ATTR_ALL = "all";
    private static final String KEY_ATTR_LIST = "list";


    public static final int ALL = -1;


    /**
     * Generate default coverage range (just 75X and ALL)
     */
    public CoverageRange() {

        // Initialise arraylist
        super();

        // Add ALL
        this.add(ALL);
    }

    /**
     * Generate coverage range from parameters (generally 50X - 100X with MEDIUM step size works well in most situations)
     * @param minCoverage Limit on minimum coverage
     * @param maxCoverage Limit on maximum coverage (this excludes coverage level that contains ALL reads)
     * @param stepSize How big the jump between coverage levels should be
     * @param all Whether add a coverage level representing ALL reads
     */
    public CoverageRange(int minCoverage, int maxCoverage, StepSize stepSize, boolean all) {

        // Initialise arraylist
        super();

        this.setFromProperties(minCoverage, maxCoverage, stepSize, all);
    }

    /**
     * Generate a coverage range from a comma separated list
     * @param list Coverage values separated by commas.  Whitespace is tolerated.
     */
    public CoverageRange(String list) {

        // Initialise arraylist
        super();

        this.setFromString(list);
    }

    /**
     * Create a coverage range from an Xml element
     * @param ele An XML element describing this coverage range
     */
    public CoverageRange(Element ele) {

        if (ele.hasAttribute(KEY_ATTR_LIST)) {
            this.setFromString(XmlHelper.getTextValue(ele, KEY_ATTR_LIST));
        }
        else {
            this.setFromProperties(XmlHelper.getIntValue(ele, KEY_ATTR_CVG_MIN),
                    XmlHelper.getIntValue(ele, KEY_ATTR_CVG_MAX),
                    StepSize.valueOf(XmlHelper.getTextValue(ele, KEY_ATTR_CVG_STEP).toUpperCase()),
                    XmlHelper.getBooleanValue(ele, KEY_ATTR_ALL));

        }
    }

    protected final void setFromProperties(int minCoverage, int maxCoverage, StepSize stepSize, boolean all) {
        for(int i = minCoverage; i <= maxCoverage; i = stepSize.nextCoverage(i)) {
            this.add(i);
        }

        if (all) {
            this.add(ALL);
        }
    }

    protected final void setFromString(String list) {
        String[] parts = list.split(",");
        for(String part : parts) {

            if (part.trim().equalsIgnoreCase("ALL")) {
                this.add(-1);
            }
            else {
                this.add(Integer.parseInt(part.trim()));
            }
        }
    }

    public boolean isAllOnly() {
        return this.size() == 1 && this.get(0).intValue() == ALL;
    }

    public enum StepSize {
        FINE {
            @Override
            public int nextCoverage(int coverage) {
                return coverage += 10;
            }
        },
        MEDIUM {
            @Override
            public int nextCoverage(int coverage) {
                return coverage += 25;
            }
        },
        COARSE {
            @Override
            public int nextCoverage(int coverage) {
                return coverage += 50;
            }
        };


        /**
         * Retrieves the next coverage value in the sequence
         *
         * @param coverage The current coverage value
         * @return The next coverage value
         */
        public abstract int nextCoverage(int coverage);
    }

    /**
     * Determines whether or not the supplied kmer range in this object is valid.  Throws an IllegalArgumentException if not.
     * @return True if the coverage range is valid, false otherwise
     */
    public boolean validate() {

        //TODO Assumes the user isn't being stupid for the time being!

        return true;
    }

    public String toString() {
        return "Coverage Range: {" + StringUtils.join(this, ",") + "}";
    }

    public static String toString(int val) {
        if (val == ALL) {
            return "all";
        }

        return Integer.toString(val);
    }
}
