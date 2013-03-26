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

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 08/03/13
 * Time: 10:51
 */
public class PdfLatex2012ProcessTest {

    private String pwd;

    @Before
    public void setup() {

        String pwdFull = new File(".").getAbsolutePath();
        this.pwd = pwdFull.substring(0, pwdFull.length() - 1);
    }

    @Test
    public void testPdfLatexCommand() {

        PdfLatex2012Args args = new PdfLatex2012Args();
        args.setOutputDir(new File(pwd + "pdfLatexOut"));
        args.setTexFile(new File(pwd + "texFile.tex"));

        PdfLatex2012Process process = new PdfLatex2012Process(args);

        String command = process.getCommand();
        String correct = "pdflatex -interaction=nonstopmode " + pwd + "texFile.tex";

        assertTrue(command != null);
        assertTrue(command.length() == correct.length());
        assertTrue(command.equals(correct));
    }

    @Test
    public void testPdfLatexFullCommand() {

        PdfLatex2012Args args = new PdfLatex2012Args();
        args.setOutputDir(new File(pwd + "pdfLatexOut"));
        args.setTexFile(new File(pwd + "texFile.tex"));

        PdfLatex2012Process process = new PdfLatex2012Process(args);

        String command = process.getFullCommand();
        String correct = "mkdir -p " + pwd + "pdfLatexOut; cd " + pwd + "pdfLatexOut; pdflatex -interaction=nonstopmode " + pwd + "texFile.tex 2>&1; cd " + pwd;

        assertTrue(command != null);
        assertTrue(command.length() == correct.length());
        assertTrue(command.equals(correct));
    }
}
