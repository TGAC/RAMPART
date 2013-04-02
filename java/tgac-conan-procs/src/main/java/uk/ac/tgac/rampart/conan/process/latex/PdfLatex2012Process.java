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
package uk.ac.tgac.rampart.conan.process.latex;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;

/**
 * User: maplesod
 * Date: 08/03/13
 * Time: 10:21
 */
public class PdfLatex2012Process extends AbstractConanProcess {

    public static final String EXE = "pdflatex";

    public PdfLatex2012Process() {
        this(new PdfLatex2012Args());
    }

    public PdfLatex2012Process(PdfLatex2012Args args) {
        super(EXE, args, new PdfLatex2012Params());

        if (args.getOutputDir() != null) {
            String pwdFull = new File(".").getAbsolutePath();
            String pwd = pwdFull.substring(0, pwdFull.length() - 1);

            this.addPreCommand("mkdir -p " + args.getOutputDir().getAbsolutePath());
            this.addPreCommand("cd " + args.getOutputDir().getAbsolutePath());
            this.addPostCommand("cd " + pwd);
        }
    }

    @Override
    public String getCommand() {

        PdfLatex2012Args args = (PdfLatex2012Args)this.getProcessArgs();

        return EXE + " -interaction=nonstopmode " + args.getTexFile();
    }

    @Override
    public String getName() {
        return "pdflatex-2012";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

       // We have to run PDF Latex 3 times to ensure the document is fully compiled.
        for(int i = 1; i <= 3; i++) {

            super.execute(executionContext);
        }

        return true;
    }


}
