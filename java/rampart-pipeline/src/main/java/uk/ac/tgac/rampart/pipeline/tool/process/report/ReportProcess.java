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
package uk.ac.tgac.rampart.pipeline.tool.process.report;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.process.latex.PdfLatex2012Args;
import uk.ac.tgac.rampart.conan.process.latex.PdfLatex2012Process;
import uk.ac.tgac.rampart.core.data.RampartJobFileStructure;
import uk.ac.tgac.rampart.core.service.RampartJobService;
import uk.ac.tgac.rampart.core.service.VelocityMergerService;

/**
 * User: maplesod
 * Date: 05/02/13
 * Time: 17:48
 */
@Component
public class ReportProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(ReportProcess.class);


    @Autowired
    private RampartJobService rampartJobService;

    @Autowired
    private VelocityMergerService velocityMergerService;


    public ReportProcess() {
        this(new ReportArgs());
    }

    public ReportProcess(ReportArgs args) {
        super("", args, new ReportParams());
    }

    @Override
    public String getName() {
        return "Report";
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        // Make a shortcut to the args
        ReportArgs args = (ReportArgs) this.getProcessArgs();
        ReportResources resources = new ReportResources();

        // Analyse the rampart job directory and build the job exectx object
        try {

            log.info("Starting report generation");

            RampartJobFileStructure jobFS = new RampartJobFileStructure(args.getJobDir());

            // Create the report directories
            jobFS.makeReportDirs();
            log.debug("Created report directories");

            // Make sure the whole directory structure looks ok (will throw if there's a problem)
            jobFS.validate(true, true);
            log.debug("Validated new job directory structure");

            // Copy template resources
            FileUtils.copyFile(resources.getTemplateFile(), jobFS.getReportTemplateFile());
            FileUtils.copyDirectory(resources.getImagesDir(), jobFS.getReportImagesDir());
            log.debug("Copied common resources to report directories");

            // Create the plot files which are to be used in the report
            this.rampartJobService.seperatePlots(jobFS.getMassPlotsFile(), jobFS.getReportImagesDir(), "mass");
            log.debug("Separated mass plots into separate files for report");

            // Create the plot files which are to be used in the report
            this.rampartJobService.seperatePlots(jobFS.getImproverPlotsFile(), jobFS.getReportImagesDir(), "Improver");
            log.debug("Separated Improver plots into separate files for report");

            // Create the job context
            VelocityContext velocityContext = this.rampartJobService.buildContext(jobFS);
            log.debug("Created job context");

            // Merge the template and context
            this.velocityMergerService.merge(jobFS.getReportTemplateFile(), velocityContext, jobFS.getReportMergedFile());
            log.debug("Merged report template and context");

            // Compile report (If there were any errors carry on anyway, we might still be able to log the
            // details in the database
            PdfLatex2012Args pdfLatex2012Args = new PdfLatex2012Args();
            pdfLatex2012Args.setTexFile(jobFS.getReportMergedFile());
            pdfLatex2012Args.setOutputDir(jobFS.getReportDir());
            PdfLatex2012Process pdfLatex2012Process = new PdfLatex2012Process(pdfLatex2012Args);
            this.conanProcessService.execute(pdfLatex2012Process, executionContext);

            log.info("Report built");

        } catch (Exception e) {
            throw new ProcessExecutionException(-1, "Report Generation Failed", e);
        }

        return true;
    }
}
