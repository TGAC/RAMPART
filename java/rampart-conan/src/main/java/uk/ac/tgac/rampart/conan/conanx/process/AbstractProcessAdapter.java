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
package uk.ac.tgac.rampart.conan.conanx.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: maplesod
 * Date: 24/01/13
 * Time: 17:27
 */
public abstract class AbstractProcessAdapter extends File implements ProcessAdapter {

    private final OutputFileMonitor fileMonitor;
    private final Set<ProcessListener> listeners;

    private Thread fileMonitorThread;

    private boolean complete = false;
    private int exitValue = -1;
    private String processExecutionHost = "unknown";

    protected List<String> processOutput = new ArrayList<String>();

    private int lastLineReadIndex = -1;

    private static Logger log = LoggerFactory.getLogger(AbstractProcessAdapter.class);

    public AbstractProcessAdapter(String pathname, int monitoringPeriod) {
        super(pathname);
        this.fileMonitor = new OutputFileMonitor(this, monitoringPeriod);
        this.listeners = new HashSet<ProcessListener>();
    }

    public AbstractProcessAdapter(String parent, String child, int monitoringPeriod) {
        super(parent, child);
        this.fileMonitor = new OutputFileMonitor(this, monitoringPeriod);
        this.listeners = new HashSet<ProcessListener>();
    }

    public AbstractProcessAdapter(File parent, String child, int monitoringPeriod) {
        super(parent, child);
        this.fileMonitor = new OutputFileMonitor(this, monitoringPeriod);
        this.listeners = new HashSet<ProcessListener>();
    }

    public AbstractProcessAdapter(URI uri, int monitoringPeriod) {
        super(uri);
        this.fileMonitor = new OutputFileMonitor(this, monitoringPeriod);
        this.listeners = new HashSet<ProcessListener>();
    }


    @Override
    public void addProcessListener(ProcessListener listener) {

        // do we have existing listeners?
        boolean startMonitor = listeners.isEmpty();
        listeners.add(listener);
        log.debug("Added process listener " + listener);

        // if we had no listeners, we might need to start monitoring
        if (startMonitor) {

            // clear any existing state
            complete = false;
            exitValue = -1;
            lastLineReadIndex = -1;

            // create new thread if required
            if (fileMonitorThread == null || !fileMonitorThread.isAlive()) {
                log.debug("Creating new file monitor thread");
                fileMonitorThread = new Thread(fileMonitor);
            }

            fileMonitorThread.start();
            log.debug("Started file monitor thread");
        }
    }

    @Override
    public void removeProcessListener(ProcessListener listener) {

        listeners.remove(listener);

        // if we have removed the last listener, stop the monitor
        fileMonitor.stop();
        log.debug("Removed process listener " + listener);
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    protected void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public int waitForExitCode() throws InterruptedException {
        while (!isComplete()) {
            wait();
        }
        return exitValue;
    }

    @Override
    public String getProcessExecutionHost() {
        return processExecutionHost;
    }

    protected void setProcessExecutionHost(String processExecutionHost) {
        this.processExecutionHost = processExecutionHost;
    }

    @Override
    public String[] getProcessOutput() {
        return processOutput.toArray(new String[processOutput.size()]);
    }

    @Override
    public File getFile() {
        return this;
    }

    @Override
    public void fireOutputFileDetectedEvent(final long lastModified) {
        try {
            // the list of new lines written to the file
            List<String> lines = new ArrayList<String>();

            // create reader
            LineNumberReader reader = new LineNumberReader(new FileReader(this));
            // read new lines, picking up where we left off
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, lines);
            }

            // update to the last line read
            lastLineReadIndex = reader.getLineNumber();
            reader.close();

            // only fire completion events if already completed
            ProcessEvent evt = new ProcessEvent(lines.toArray(new String[lines.size()]), lastModified, exitValue);
            for (ProcessListener listener : listeners) {
                if (complete) {
                    listener.processComplete(evt);
                    fileMonitor.stop();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fileMonitor.stop();
        }
    }

    @Override
    public void fireOutputFileUpdateEvent(final long lastModified) {
        try {
            // the list of new lines written to the file
            List<String> lines = new ArrayList<String>();

            // create reader
            LineNumberReader reader = new LineNumberReader(new FileReader(this));
            // read new lines, picking up where we left off
            String line;
            while ((line = reader.readLine()) != null) {
                if (reader.getLineNumber() <= lastLineReadIndex) {
                    // already read this line, ignore
                    log.debug("Skipping previously read content: " + line);
                }
                else {
                    parseLine(line, lines);
                }
            }

            // update to the last line read
            lastLineReadIndex = reader.getLineNumber();
            reader.close();

            // now create our event and fire listeners
            ProcessEvent evt = new ProcessEvent(lines.toArray(new String[lines.size()]), lastModified, exitValue);
            for (ProcessListener listener : listeners) {
                if (complete) {
                    listener.processComplete(evt);
                    fileMonitor.stop();
                }
                else {
                    listener.processUpdate(evt);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fileMonitor.stop();
        }
    }

    @Override
    public void fireOutputFileDeleteEvent(final long lastModified) {
        // if the file is deleted, terminate our process
        fileMonitor.stop();

        // now create our event and fire listeners
        ProcessEvent evt = new ProcessEvent(new String[0], lastModified, -1);
        for (ProcessListener listener : listeners) {
            listener.processError(evt);
        }
    }


    protected abstract void parseLine(String line, List<String> lines);

    protected int getExitValue() {
        return exitValue;
    }

    protected void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }



}
