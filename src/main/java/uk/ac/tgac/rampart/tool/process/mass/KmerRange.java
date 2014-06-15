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
package uk.ac.tgac.rampart.tool.process.mass;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import uk.ac.tgac.conan.core.util.XmlHelper;

import java.util.ArrayList;

/**
 * User: maplesod
 * Date: 14/08/13
 * Time: 10:28
 */
public class KmerRange extends ArrayList<Integer> {

    // Limits
    public static final int KMER_MIN = 11;
    public static final int KMER_MAX = 125;

    // Xml Config keys
    private static final String KEY_ATTR_K_MIN = "min";
    private static final String KEY_ATTR_K_MAX = "max";
    private static final String KEY_ATTR_STEP = "step";
    private static final String KEY_ATTR_LIST = "list";


    /**
     * Default Kmer range (just K61)
     */
    public KmerRange() {
        this(61, 61, StepSize.MEDIUM);
    }

    /**
     * Generate Kmer Range from properties
     * @param minKmer Minimum kmer length (may be automatically adjusted by stepper to first valid kmer)
     * @param maxKmer Maximum kmer length
     * @param stepSize How big the jump between kmer values should be
     */
    public KmerRange(int minKmer, int maxKmer, StepSize stepSize) {

        // Init arraylist
        super();

        // Generate list of kmers from range properties
        this.setFromProperties(minKmer, maxKmer, stepSize);
    }

    /**
     * Generates a kmer range from a comma separated list
     * @param list kmer values separated by commas.  Whitespace is tolerated.
     */
    public KmerRange(String list) {

        // Init arraylist
        super();

        // Split string and generate kmers from list
        this.setFromString(list);
    }

    /**
     * Creates a kmer range object from an Xml Config element.  If there's a list attribute then use that.  If not then
     * we assume a range has been specified, and we generate a list from those attributes.
     * @param ele
     */
    public KmerRange(Element ele) {

        if (ele.hasAttribute(KEY_ATTR_LIST)) {
            this.setFromString(XmlHelper.getTextValue(ele, KEY_ATTR_LIST));
        }
        else {
            this.setFromProperties(XmlHelper.getIntValue(ele, KEY_ATTR_K_MIN),
                XmlHelper.getIntValue(ele, KEY_ATTR_K_MAX),
                StepSize.valueOf(XmlHelper.getTextValue(ele, KEY_ATTR_STEP).toUpperCase()));
        }
    }

    protected final void setFromProperties(int minKmer, int maxKmer, StepSize stepSize) {
        for(int k = stepSize.firstValidKmer(minKmer); k <= maxKmer; k = stepSize.nextKmer(k)) {
            this.add(k);
        }
    }

    protected final void setFromString(String list) {
        String[] parts = list.split(",");
        for(String part : parts) {
            this.add(Integer.parseInt(part.trim()));
        }
    }

    public int getFirstKmer() {
        return this.get(0);
    }

    public int getLastKmer() {
        return this.get(this.size() - 1);
    }


    public enum StepSize {
        FINE {
            @Override
            public int nextKmer(int kmer) {
                return kmer += 2;
            }

            @Override
            public int firstValidKmer(int kmin) {
                return nextOddNumber(kmin);
            }
        },
        MEDIUM {
            @Override
            public int nextKmer(int kmer) {
                return kmer += 4;
            }

            @Override
            public int firstValidKmer(int kmin) {
                return nextOddNumber(kmin);
            }
        },
        COARSE {
            @Override
            public int nextKmer(int kmer) {
                return kmer += 10;
            }

            @Override
            public int firstValidKmer(int kmin) {
                return nextOddNumber(kmin);
            }
        };

        private static int nextOddNumber(int number) {
            return (number % 2 == 0) ? number + 1 : number;
        }

        /**
         * Retrieves the next k-mer value in the sequence
         *
         * @param kmer The current k-mer value
         * @return The next kmer value
         */
        public abstract int nextKmer(int kmer);
        public abstract int firstValidKmer(int kmin);
    }

    /**
     * Determines whether or not the supplied kmer range in this object is valid.  Throws an IllegalArgumentException if not.
     */
    public boolean validate() {

        //TODO This logic isn't bullet proof... we can still nudge the minKmer above the maxKmer

        if (this.size() > 0) {

            if (this.getFirstKmer() < KMER_MIN || this.getLastKmer() < KMER_MIN)
                throw new IllegalArgumentException("K-mer values must be >= " + KMER_MIN + "nt");

            if (this.getFirstKmer() > KMER_MAX || this.getLastKmer() > KMER_MAX)
                throw new IllegalArgumentException("K-mer values must be <= " + KMER_MAX + "nt");

            if (this.getFirstKmer() > this.getLastKmer())
                throw new IllegalArgumentException("Error: Min K-mer value must be <= Max K-mer value");

            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "K-mer Range: {" + StringUtils.join(this, ",") + "}";
    }
}
