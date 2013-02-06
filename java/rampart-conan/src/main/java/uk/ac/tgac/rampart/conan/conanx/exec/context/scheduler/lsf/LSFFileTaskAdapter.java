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
package uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler.lsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.tgac.rampart.conan.conanx.exec.task.monitor.AbstractFileTaskAdapter;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 17:51
 */
public class LSFFileTaskAdapter extends AbstractFileTaskAdapter {

    private static Logger log = LoggerFactory.getLogger(LSFFileTaskAdapter.class);

    private boolean commencedStdout = false;
    private boolean finishedStdout = false;

    public LSFFileTaskAdapter(String pathname, int monitoringPeriod) {
        super(pathname, monitoringPeriod);
    }

    public LSFFileTaskAdapter(String parent, String child, int monitoringPeriod) {
        super(parent, child, monitoringPeriod);
    }

    public LSFFileTaskAdapter(File parent, String child, int monitoringPeriod) {
        super(parent, child, monitoringPeriod);
    }

    public LSFFileTaskAdapter(URI uri, int monitoringPeriod) {
        super(uri, monitoringPeriod);
    }


    @Override
    protected void parseLine(String line, List<String> lines) {
        // if the line indicates the LSF task is complete, set flag
        if (line.startsWith("Successfully completed")) {
            log.debug("Read completion content: " + line);
            this.setExitValue(0);
            this.setComplete(true);
        }
        else if (line.startsWith("Exited")) {
            log.debug("Read completion content: " + line);
            if (line.split(" ").length > 4) {
                String exitValStr = line.split(" ")[4].trim();
                log.debug("Read exit value from LSF output file, value was " + exitValStr);
                if (exitValStr.endsWith(".")) {
                    exitValStr = exitValStr.replace(".", "");
                    log.debug("Munged string to remove full stop is now " + exitValStr);
                }
                this.setExitValue(Integer.valueOf(exitValStr));
                log.debug("Exit value: " + this.getExitValue());
            }
            else {
                this.setExitValue(1);
            }
            this.setComplete(true);
        }
        else if (line.startsWith("Job was executed on host")) {
            this.setProcessExecutionHost(line.substring(line.indexOf("<") + 1, line.indexOf(">")));
        }
        else if (line.startsWith("----------------------")) {
            finishedStdout = true;
        }
        else if (line.startsWith("The output (if any) follows:")) {
            commencedStdout = true;
        }
        else {
            if (!finishedStdout || commencedStdout) {
                this.processOutput.add(line);
            }
            else {
                log.debug("Read non-complete content: " + line);
            }
        }

        lines.add(line);
    }
}
