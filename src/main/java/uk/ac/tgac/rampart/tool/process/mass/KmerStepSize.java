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

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 11:14
 */
public enum KmerStepSize {
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
            int mod1 = (kmer - 1) % 10;
            int mod2 = (kmer - 5) % 10;

            if (mod1 == 0) {
                return kmer + 4;
            } else if (mod2 == 0) {
                return kmer + 6;
            } else {
                throw new IllegalArgumentException("Kmer values have somehow got out of step!!");
            }
        }

        @Override
        public int firstValidKmer(int kmin) {
            if (kmin <= 11)
                return 11;

            String kminStr = String.valueOf(kmin);
            if (kminStr.charAt(kminStr.length() - 1) == '1') {
                return kmin;
            } else {
                char tensDigit = kminStr.charAt(kminStr.length() - 2);

                int tens = (Integer.parseInt(String.valueOf(tensDigit)) + 1) * 10;

                int rest = 0;

                if (kminStr.length() > 2) {
                    String restStr = kminStr.substring(0, kminStr.length() - 3) + "00";
                    rest = Integer.parseInt(restStr);
                }

                return rest + tens + 1;
            }
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
