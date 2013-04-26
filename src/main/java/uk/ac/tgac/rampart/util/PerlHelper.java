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
package uk.ac.tgac.rampart.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

/**
 * User: maplesod
 * Date: 07/01/13
 * Time: 15:36
 */
public enum PerlHelper {

    MASS_GP {
        @Override
        public String getPath() {
            return "/scripts/perl/mass_gp.pl";
        }
    };

    public File getInternalScript() {
        URL scriptUrl = this.getClass().getResource(this.getPath());
        return FileUtils.toFile(scriptUrl);
    }

    public File getExternalScript() {
        return new File(
                RAMPART_DIR,
                this.getPath());
    }

    private static final File RAMPART_DIR = new File(System.getProperty("user.home") + "/.rampart/");
    public abstract String getPath();
}
