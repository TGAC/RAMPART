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
package uk.ac.tgac.rampart.conan.tool.pipeline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 16:45
 */
public enum RampartStage {

    QT {
        @Override
        public String getStatsID() {
            return null;
        }

        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }
    },
    MASS {
        @Override
        public String getStatsID() {
            return "kmer";
        }

        @Override
        public String translateFilenameToKey(String filename) {

            Pattern pattern = Pattern.compile("^.*k(\\d+)-.+$");
            Matcher matcher = pattern.matcher(filename);

            if (matcher.matches()) {

                return matcher.group(1);
            } else {
                return null;
            }
        }
    },
    BEST {
        @Override
        public String getStatsID() {
            return null;
        }

        @Override
        public String translateFilenameToKey(String filename) {
            return null;
        }
    },
    AMP {
        @Override
        public String getStatsID() {
            return "index";
        }

        @Override
        public String translateFilenameToKey(String filename) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(filename);
            String key = matcher.group(1);

            return key;
        }
    };

    public abstract String getStatsID();

    public abstract String translateFilenameToKey(String filename);
}
