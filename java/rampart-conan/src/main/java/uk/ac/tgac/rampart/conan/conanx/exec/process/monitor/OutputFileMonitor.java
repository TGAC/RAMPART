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
package uk.ac.tgac.rampart.conan.conanx.exec.process.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 17:38
 */
public class OutputFileMonitor implements Runnable {

    private static Logger log = LoggerFactory.getLogger(OutputFileMonitor.class);

    private ProcessAdapter processAdapter;
    private final File outputFile;
    private final int interval;

    private boolean running;

    private boolean fileExisted;
    private long lastModified;
    private long lastLength;

    public OutputFileMonitor(ProcessAdapter processAdapter, int interval) {
        this.processAdapter = processAdapter;
        this.outputFile = processAdapter.getFile();
        this.interval = interval;

        this.running = true;
        this.fileExisted = false;
        this.lastModified = -1;
        this.lastLength = -1;
    }

    public void run() {
        log.debug("Starting file monitor for " + outputFile.getAbsolutePath());
        while (running) {
            synchronized (outputFile) {
                // check the outputFile exists
                if (outputFile.exists()) {
                    // we have found our file
                    if (!fileExisted) {
                        // the lsfOutputFile has been detected for the first time
                        fileExisted = true;
                        log.debug("File detected at " + outputFile.lastModified() + " " +
                                "(size " + outputFile.length() + ")");
                        processAdapter.fireOutputFileDetectedEvent(outputFile.lastModified());
                    } else {
                        // check for modifications
                        if (outputFile.lastModified() > lastModified ||
                                outputFile.length() != lastLength) {
                            // the lsfOutputFile has been updated since we last checked
                            log.debug("File updated: " +
                                    "modified -  " + outputFile.lastModified() + " " +
                                    "(previously " + lastModified + "); " +
                                    "size - " + outputFile.length() + " " +
                                    "(previously " + lastLength + ")");
                            processAdapter.fireOutputFileUpdateEvent(outputFile.lastModified());
                        }
                    }
                } else {
                    if (fileExisted) {
                        // the lsfOutputFile was found before, so it definitely existed at some point...
                        // therefore it has been deleted by an proc proc
                        log.debug("File previously existed but has been deleted");
                        processAdapter.fireOutputFileDeleteEvent(outputFile.lastModified());
                    }
                }

                // updated the lastModified time
                lastModified = outputFile.lastModified();
                lastLength = outputFile.length();
            }

            // sleep for interval seconds
            synchronized (this) {
                try {
                    wait(interval * 1000);
                } catch (InterruptedException e) {
                    // if interrupted, die
                    log.debug("Interrupted exception causing thread to die");
                    stop();
                }
            }
        }
        log.debug("Stopping file monitor for " + outputFile.getAbsolutePath());
    }

    public void stop() {
        running = false;
    }
}

