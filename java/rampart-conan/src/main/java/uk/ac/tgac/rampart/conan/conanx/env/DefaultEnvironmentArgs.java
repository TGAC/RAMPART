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
package uk.ac.tgac.rampart.conan.conanx.env;

import java.io.File;

/**
 * User: maplesod
 * Date: 08/01/13
 * Time: 14:22
 */
public class DefaultEnvironmentArgs implements EnvironmentArgs {


    @Override
    public EnvironmentArgs copy() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getMemoryMB() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMemoryMB(int memoryMB) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getThreads() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setThreads(int threads) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getJobName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJobName(String jobName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getCmdLineOutputFile() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCmdLineOutputFile(File cmdLineOutputFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
