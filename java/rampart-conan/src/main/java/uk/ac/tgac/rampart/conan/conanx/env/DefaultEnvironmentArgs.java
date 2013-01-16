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

    private int memoryMB;
    private int threads;
    private String jobName;
    private File cmdLineOutputFile;
    private boolean backgroundTask;

    public DefaultEnvironmentArgs() {
        this.memoryMB = 0;
        this.threads = 0;
        this.jobName = "";
        this.cmdLineOutputFile = null;
        this.backgroundTask = false;
    }

    public DefaultEnvironmentArgs(DefaultEnvironmentArgs args) {
        this.memoryMB = args.getMemoryMB();
        this.threads = args.getThreads();
        this.jobName = args.getJobName();
        this.cmdLineOutputFile = new File(args.getCmdLineOutputFile().getAbsolutePath());
        this.backgroundTask = args.isBackgroundTask();
    }

    @Override
    public EnvironmentArgs copy() {
        return new DefaultEnvironmentArgs(this);
    }

    @Override
    public int getMemoryMB() {
        return this.memoryMB;
    }

    @Override
    public void setMemoryMB(int memoryMB) {
        this.memoryMB = memoryMB;
    }

    @Override
    public int getThreads() {
        return this.threads;
    }

    @Override
    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public String getJobName() {
        return this.jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public File getCmdLineOutputFile() {
        return this.cmdLineOutputFile;
    }

    @Override
    public void setCmdLineOutputFile(File cmdLineOutputFile) {
        this.cmdLineOutputFile = cmdLineOutputFile;
    }

    @Override
    public boolean isBackgroundTask() {
        return this.backgroundTask;
    }

    @Override
    public void setBackgroundTask(boolean state) {
        this.backgroundTask = state;
    }
}
