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
package uk.ac.tgac.rampart.tool.process.mass.single;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.WaitCondition;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.AssemblerArgs;
import uk.ac.tgac.conan.process.asm.AssemblerFactory;
import uk.ac.tgac.rampart.tool.process.mass.MassInput;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SingleMassProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(SingleMassProcess.class);

    private SingleMassExecutor singleMassExecutor;


    public SingleMassProcess() {
        this(new SingleMassArgs());
    }

    public SingleMassProcess(SingleMassArgs args) {
        super("", args, new SingleMassParams());
        this.singleMassExecutor = new SingleMassExecutorImpl();
    }


    /**
     * Dispatches assembly jobs to the specified environments
     *
     * @param executionContext The environment to dispatch jobs too
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        try {

            // Make a shortcut to the args
            SingleMassArgs args = (SingleMassArgs) this.getProcessArgs();

            // Initialise the object that makes system calls
            this.singleMassExecutor.initialise(this.conanProcessService, executionContext);

            // Create an assembly object used for other stages in the MASS pipeline.. note this does not include
            // specific kmer settings
            Assembler genericAssembler = AssemblerFactory.createAssembler(args.getTool());

            log.info("Starting Single MASS run for " + args.getName());

            // Make sure the kmer range is reasonable (if it's not already)
            KmerRange validatedKmerRange = this.validateKmerRange(args.getName(), genericAssembler.hasKParam(), args.getKmerRange());

            // Make sure the coverage range reasonable (if it's not already)
            CoverageRange validatedCoverageRange = this.validateCoverageRange(args.getName(), args.getCoverageRange());

            // Make sure the inputs are reasonable
            List<Library> selectedLibs = this.validateInputs(args.getName(), args.getInputs(), args.getAllLibraries(), args.getAllMecqs());

            // Add libs to generic assembler so we know what kind of output to expect
            genericAssembler.getArgs().setLibraries(selectedLibs);

            // Create any required directories for this job
            log.debug("Creating directories");
            this.createSupportDirectories(genericAssembler, args);

            WaitCondition assemblerWait = null;

            // Execute assemblies only if user doesn't just want the statistics from existing assemblies.
            if (!args.isStatsOnly()) {

                // Dispatch an assembly job for coverage and kmer value
                for (Integer cvg : validatedCoverageRange) {

                    for (Integer k : validatedKmerRange) {

                        // Generate a directory name for this assembly
                        String cvgString = CoverageRange.toString(cvg);
                        String dirName = genericAssembler.hasKParam() ? ("cvg-" + cvgString + "_k-" + k) : "cvg-" + cvgString;

                        // This is the output directory for this particular assembly
                        File outputDir = new File(args.getOutputDir(), dirName);

                        log.debug("Starting " + args.getTool() + " in " + outputDir.getAbsolutePath());

                        // Create the actual assembler for these settings
                        Assembler assembler = this.makeAssembler(args, k, cvg, selectedLibs, outputDir);

                        // Make the output directory for this child job (delete the directory if it already exists)
                        if (outputDir.exists()) {
                            FileUtils.deleteDirectory(outputDir);
                        }
                        outputDir.mkdir();

                        // Execute the assembler
                        this.singleMassExecutor.executeAssembler(assembler, args.getJobPrefix() + "-" + dirName, args.isRunParallel());

                        // Create links for outputs from this assembler to known locations
                        this.singleMassExecutor.createAssemblyLinks(assembler, args, args.getName() + "-" + dirName);
                    }
                }

                // If using a scheduler create a wait condition that will be observed by the stats job if running in parallel
                assemblerWait = executionContext.usingScheduler() && args.isRunParallel() ?
                        executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, args.getJobPrefix() + "*") :
                        null;
            }

            // Run analyser job using the original execution context
            log.debug("Analysing and comparing assemblies for MASS group: " + args.getName());
            this.singleMassExecutor.dispatchStatsJobs(genericAssembler, args, assemblerWait, args.getJobPrefix() + "-analyser");

            log.info("Finished MASS group: " + args.getName());

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        } catch (CommandExecutionException cee) {
            throw new ProcessExecutionException(-2, cee);
        }

        return true;
    }

    /**
     * Create output directories that contain symbolic links to to all the assemblies generated during this run
     * @param assembler
     * @param args
     */
    protected void createSupportDirectories(Assembler assembler, SingleMassArgs args) {

        // Create directory for links to assembled contigs
        if (assembler.makesUnitigs()) {
            args.getUnitigsDir().mkdir();
        }

        // Create directory for links to assembled contigs
        if (assembler.makesContigs()) {
            args.getContigsDir().mkdir();
        }

        // Create dir for scaffold links if this asm creates them
        if (assembler.makesScaffolds()) {
            args.getScaffoldsDir().mkdir();
        }
    }

    /**
     * Create an Assembler from the Mass args and details of the current context
     * @param massArgs
     * @param k
     * @param cvg
     * @param selectedLibs
     * @param outputDir
     * @return
     */
    protected Assembler makeAssembler(SingleMassArgs massArgs, int k, int cvg, List<Library> selectedLibs, File outputDir) {

        Assembler asm = AssemblerFactory.valueOf(massArgs.getTool()).create();
        AssemblerArgs asmArgs = asm.getArgs();

        asmArgs.setLibraries(selectedLibs);
        asmArgs.setDesiredCoverage(cvg);
        asmArgs.setKmer(k);
        asmArgs.setThreads(massArgs.getThreads());
        asmArgs.setMemory(massArgs.getMemory());
        asmArgs.setOrganism(massArgs.getOrganism());
        asmArgs.setOutputDir(outputDir);

        return asm;
    }

    protected List<Library> validateInputs(String massName, List<MassInput> inputs, List<Library> allLibraries, List<EcqArgs> allMecqs) throws IOException {

        List<Library> selectedLibs = new ArrayList<Library>();

        for(MassInput mi : inputs) {
            Library lib = mi.findLibrary(allLibraries);
            EcqArgs ecqArgs = mi.findMecq(allMecqs);

            if (lib == null) {
                throw new IOException("Unrecognised library: " + mi.getLib() + "; not processing MASS run: " + massName);
            }

            if (ecqArgs == null) {
                if (mi.getMecq().equalsIgnoreCase(EcqArgs.RAW)) {
                    selectedLibs.add(lib);
                }
                else {
                    throw new IOException("Unrecognised MECQ dataset requested: " + mi.getMecq() + "; not processing MASS run: " + massName);
                }
            }
            else {
                Library modLib = lib.copy();

                List<File> files = ecqArgs.getOutputFiles(lib.getName());

                if (modLib.isPairedEnd()) {
                    if (files.size() != 2) {
                        throw new IOException("Paired end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have two files");
                    }

                    modLib.setFiles(files.get(0), files.get(1));
                }
                else {
                    if (files.size() != 1) {
                        throw new IOException("Single end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have one file");
                    }

                    modLib.setFiles(files.get(0), null);
                }

                selectedLibs.add(modLib);
            }
        }

        return selectedLibs;
    }

    protected KmerRange validateKmerRange(String massName, boolean assemblerSupportsK, KmerRange kmerRange) throws CommandExecutionException {

        if (!assemblerSupportsK) {
            log.info("Selected assembler for: " + massName + " does not support K parameter");
            return new KmerRange();
        }
        else if (kmerRange == null) {
            KmerRange defaultKmerRange = new KmerRange();
            log.info("No K-mer range specified for " + massName + " running assembler with default range: " + defaultKmerRange.toString());
            return defaultKmerRange;
        }
        else if (kmerRange.validate()) {
            log.info("K-mer range for " + massName + " validated: " + kmerRange.toString());
            return kmerRange;
        }
        else {
            throw new CommandExecutionException("Invalid K-mer range: " + kmerRange.toString() + " Not processing MASS run: " + massName);
        }
    }

    protected CoverageRange validateCoverageRange(String massName, CoverageRange coverageRange) throws CommandExecutionException {

        if (coverageRange == null) {
            CoverageRange defaultCoverageRange = new CoverageRange();
            log.info("No coverage range specified for " + massName + " running assembler with default range: " + defaultCoverageRange.toString());
            return defaultCoverageRange;
        }
        else if (coverageRange.validate()) {
            log.info("Coverage range for " + massName + " validated: " + coverageRange.toString());
            return coverageRange;
        }
        else {
            throw new CommandExecutionException("Invalid coverage range: " + coverageRange.toString() + " Not processing MASS run: " + massName);
        }
    }



    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public String getCommand() {
        return null;
    }

}
