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
package uk.ac.tgac.rampart.conan.process.degap.gapcloser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.process.degap.AbstractDegapperProcess;
import uk.ac.tgac.rampart.conan.process.scaffold.sspace.SSpaceBasicV2Args;

import java.io.File;
import java.io.IOException;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 13:44
 */
public class GapCloserV112Process extends AbstractDegapperProcess {

    private static Logger log = LoggerFactory.getLogger(GapCloserV112Process.class);


    public static final String EXE = "GapCloser";

    public GapCloserV112Process() {
        this(new GapCloserV112Args());
    }

    public GapCloserV112Process(GapCloserV112Args args) {
        super(EXE, args, new GapCloserV112Params());
    }

    @Override
    public String getCommand() {
        return this.getCommand(this.getProcessArgs(), true, "-", " ");
    }

    @Override
    public String getName() {
        return "SOAP_GapCloser_v1.12";
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        GapCloserV112Args args = (GapCloserV112Args)this.getProcessArgs();

        // Create the SSPACE lib configuration file from the library list
        try {
            if (args.getLibraryFile() == null) {

                args.setLibraryFile(new File(args.getOutputDir(), "soap_gc.libs"));
            }

            args.createLibraryFile(args.getLibraries(), args.getLibraryFile());
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        }

        if (args.getOutputFile() == null) {
            args.setOutputFile(new File(args.getOutputDir(), "gc.fa"));
        }

        ExecutionContext executionContextCopy = executionContext.copy();

        if (executionContextCopy.usingScheduler()) {
            executionContextCopy.getScheduler().getArgs().setMonitorFile(new File(args.getOutputDir(), args.getOutputFile().getName() + ".scheduler.log"));
        }

        try {
            return super.execute(executionContextCopy);
        }
        catch(ProcessExecutionException pee) {
            log.warn("Gap Closer threw an error.  This is probably not a problem, gap closer always finishes with an error condition but please check output.");
        }

        return true;
    }
}
