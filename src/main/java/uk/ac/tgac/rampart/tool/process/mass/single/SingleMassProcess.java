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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.process.asm.Assembler;
import uk.ac.tgac.conan.process.asm.AssemblerArgs;
import uk.ac.tgac.conan.process.asm.AssemblerFactory;
import uk.ac.tgac.conan.process.ec.ErrorCorrector;
import uk.ac.tgac.rampart.tool.process.mass.MassInput;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mecq.EcqArgs;
import uk.ac.tgac.rampart.tool.process.mecq.MecqExecutorImpl;
import uk.ac.tgac.rampart.tool.process.stats.StatsLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
            CoverageRange validatedCoverageRange = this.validateCoverageRange(args.getName(), args.getOrganism(), args.getCoverageRange());

            // Make sure the inputs are reasonable
            List<Library> selectedLibs = this.validateInputs(args.getName(), args.getInputs(), args.getAllLibraries(), args.getAllMecqs(), args.getMecqDir());

            // Add libs to generic assembler so we know what kind of output to expect
            genericAssembler.getArgs().setLibraries(selectedLibs);

            // Create any required directories for this job
            log.debug("Creating directories");
            this.createSupportDirectories(genericAssembler, args);

            String assemblerWait = null;

            // Execute assemblies only if user doesn't just want the statistics from existing assemblies.
            if (!args.isStatsOnly()) {

                List<Integer> assemblyJobIds = new ArrayList<>();

                // Iterate over coverage range
                for (Integer cvg : validatedCoverageRange) {

                    // Do subsampling for this coverage level if required
                    List<Library> subsampledLibs = doSubsampling(args, genericAssembler.doesSubsampling(), cvg, selectedLibs);

                    // Iterate over kmer range
                    for (Integer k : validatedKmerRange) {

                        // Generate a directory name for this assembly
                        String cvgString = CoverageRange.toString(cvg);
                        String dirName = genericAssembler.hasKParam() ? ("cvg-" + cvgString + "_k-" + k) : "cvg-" + cvgString;

                        // This is the output directory for this particular assembly
                        File outputDir = new File(args.getOutputDir(), dirName);

                        log.debug("Starting " + args.getTool() + " in " + outputDir.getAbsolutePath());

                        // Create the actual assembler for these settings
                        Assembler assembler = this.makeAssembler(args, k, cvg, subsampledLibs, outputDir);

                        // Make the output directory for this child job (delete the directory if it already exists)
                        if (outputDir.exists()) {
                            FileUtils.deleteDirectory(outputDir);
                        }
                        outputDir.mkdirs();

                        // Execute the assembler
                        ExecutionResult result = this.singleMassExecutor.executeAssembler(assembler, args.getJobPrefix() + "-assembly-" + dirName, args.isRunParallel());

                        // Add assembler id to list
                        assemblyJobIds.add(result.getJobId());

                        // Create links for outputs from this assembler to known locations
                        this.singleMassExecutor.createAssemblyLinks(assembler, args, args.getName() + "-" + dirName);
                    }
                }

                // If using a scheduler create a wait condition that will be observed by the stats job if running in parallel
                assemblerWait = executionContext.usingScheduler() && args.isRunParallel() ?
                        executionContext.getScheduler().generatesJobIdFromOutput() ?
                                executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, assemblyJobIds) :
                                executionContext.getScheduler().createWaitCondition(ExitStatus.Type.COMPLETED_SUCCESS, args.getJobPrefix() + "-assembly*") :
                        null;
            }

            // Run analyser job using the original execution context
            log.info("Analysing and comparing assemblies for MASS group: " + args.getName());
            this.singleMassExecutor.dispatchAnalyserJobs(genericAssembler, args, assemblerWait, args.getJobPrefix() + "-analyser");

            // Finish
            log.info("Finished MASS group: " + args.getName());

        } catch (IOException ioe) {
            throw new ProcessExecutionException(-1, ioe);
        } catch (CommandExecutionException cee) {
            throw new ProcessExecutionException(-2, cee);
        }

        return true;
    }

    public List<Integer> getJobIds() {
        return this.singleMassExecutor.getJobIds();
    }

    private List<Library> doSubsampling(SingleMassArgs args, boolean assemblerDoesSubsampling, int coverage, List<Library> libraries)
            throws IOException, InterruptedException, ProcessExecutionException {


        // Check to see if we even need to do subsampling.  If not just return the current libraries.
        if (assemblerDoesSubsampling || coverage == CoverageRange.ALL) {
            return libraries;
        }

        // Try to create directory to contain subsampled libraries
        File subsamplingDir = new File(args.getOutputDir(), "subsampled_libs");

        if (!subsamplingDir.exists()) {
            if (!subsamplingDir.mkdirs()) {
                throw new IOException("Couldn't create subsampling directory for " + args.getName() + " in " + args.getOutputDir().getAbsolutePath());
            }
        }

        // Subsample each library
        List<Library> subsampledLibs = new ArrayList<>();

        for(Library lib : libraries) {

            Library subsampledLib = lib.copy();

            // Subsample to this coverage level if required
            long timestamp = System.currentTimeMillis();
            String fileSuffix = "_cvg-" + coverage + ".fastq";
            String jobPrefix = args.getJobPrefix() + "-subsample-" + lib.getName() + "-" + coverage + "x";

            subsampledLib.setName(lib.getName() + "-" + coverage + "x");

            if (subsampledLib.isPairedEnd()) {

                // This calculation is much quicker if library is uniform, in this case we just calculate from the number
                // of entries, otherwise we have to scan the whole file.
                long sequencedBases = lib.isUniform() ?
                        2 * lib.getReadLength() * this.singleMassExecutor.getNbEntries(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-line_count") :
                        this.singleMassExecutor.getNbBases(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-base_count") +
                                this.singleMassExecutor.getNbBases(lib.getFile2(), subsamplingDir, jobPrefix + "-file2-base_count");

                // Calculate the probability of keeping an entry
                double probability = (double)sequencedBases / (double)args.getOrganism().getEstGenomeSize() / 2.0;

                log.debug("Estimated that library: " + lib.getName() + "; has approximately " + sequencedBases + " bases.  " +
                        "Estimated genome size is: " + args.getOrganism().getEstGenomeSize() + "; so we plan only to keep " +
                        probability + "% of the reads to achieve approximately " + coverage + "X coverage");


                subsampledLib.setFiles(
                        new File(subsamplingDir, lib.getFile1().getName() + fileSuffix),
                        new File(subsamplingDir, lib.getFile2().getName() + fileSuffix)
                );

                this.singleMassExecutor.executeSubsampler(probability, timestamp, lib.getFile1(), subsampledLib.getFile1(), jobPrefix + "-file1");
                this.singleMassExecutor.executeSubsampler(probability, timestamp, lib.getFile2(), subsampledLib.getFile2(), jobPrefix + "-file2");
            }
            else {

                // This calculation is much quicker if library is uniform, in this case we just calculate from the number
                // of entries, otherwise we have to scan the whole file.
                long sequencedBases = lib.isUniform() ?
                        lib.getReadLength() * this.singleMassExecutor.getNbEntries(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-line_count") :
                        this.singleMassExecutor.getNbBases(lib.getFile1(), subsamplingDir, jobPrefix + "-file1-base_count");

                // Calculate the probability of keeping an entry
                double probability = (double)sequencedBases / (double)args.getOrganism().getEstGenomeSize();

                log.debug("Estimated that library: " + lib.getName() + "; has approximately " + sequencedBases + " bases.  " +
                        "Estimated genome size is: " + args.getOrganism().getEstGenomeSize() + "; so we plan only to keep " +
                        probability + "% of the reads to achieve approximately " + coverage + "X coverage");

                subsampledLib.setFiles(
                        new File(subsamplingDir, lib.getFile1().getName() + fileSuffix),
                        null
                );
                this.singleMassExecutor.executeSubsampler(probability, timestamp, lib.getFile1(), subsampledLib.getFile1(), jobPrefix);
            }

            subsampledLibs.add(subsampledLib);
        }

        return subsampledLibs;
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
     * @return An assembler built using a combination of single mass args and user specified args
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

    protected List<Library> validateInputs(String massName, List<MassInput> inputs, List<Library> allLibraries, List<EcqArgs> allMecqs, File mecqDir) throws IOException {

        List<Library> selectedLibs = new ArrayList<>();

        for(MassInput mi : inputs) {
            Library lib = mi.findLibrary(allLibraries);
            EcqArgs ecqArgs = mi.findMecq(allMecqs);

            if (lib == null) {
                throw new IOException("Unrecognised library: " + mi.getLib() + "; not processing MASS run: " + massName);
            }

            if (ecqArgs == null) {
                if (mi.getEcq().equalsIgnoreCase(EcqArgs.RAW)) {
                    selectedLibs.add(lib);
                }
                else {
                    throw new IOException("Unrecognised MECQ dataset requested: " + mi.getEcq() + "; not processing MASS run: " + massName);
                }
            }
            else {
                Library modLib = lib.copy();

                ErrorCorrector ec = new MecqExecutorImpl().makeErrorCorrector(ecqArgs, modLib, mecqDir);
                List<File> files = ec.getArgs().getCorrectedFiles();

                if (modLib.isPairedEnd()) {
                    if (files.size() < 2 || files.size() > 3) {
                        throw new IOException("Paired end library: " + modLib.getName() + " from " + ecqArgs.getName() + " does not have two or three files");
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

            log.info("Found library.  Lib name: " + mi.getLib() + "; ECQ name: " + mi.getEcq() + "; Single MASS name: " + massName);
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

    protected CoverageRange validateCoverageRange(String massName, Organism organism, CoverageRange coverageRange) throws CommandExecutionException {

        if (coverageRange == null) {
            CoverageRange defaultCoverageRange = new CoverageRange();
            log.info("No coverage range specified for " + massName + " running assembler with default range: " + defaultCoverageRange.toString());
            return defaultCoverageRange;
        }
        else if (organism == null || organism.getEstGenomeSize() <= 0) {
            CoverageRange defaultCoverageRange = new CoverageRange();
            log.info("No estimated genome size specified.  Not possible to subsample to desired range without a genome " +
                    "size estimate. Running assembler with default coverage range: " + defaultCoverageRange.toString());
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


    protected File getHighestStatsLevelDir(SingleMassArgs args) {

        if (args.getScaffoldsDir().exists()) {
            return args.getScaffoldsDir();
        }
        else if (args.getContigsDir().exists()) {
            return args.getContigsDir();
        }
        else if (args.getUnitigsDir().exists()) {
            return args.getUnitigsDir();
        }

        return null;
    }

    public AssemblyStatsTable compileResults(SingleMassArgs args) throws IOException {

        AssemblyStatsTable results = new AssemblyStatsTable();

        File statsDir = this.getHighestStatsLevelDir(args);

        if (statsDir == null) {
            return results;
        }

        if (args.getStatsLevels().contains(StatsLevel.CONTIGUITY)) {

            File quastFile = new File(statsDir, "quast/report.txt");

            if (!quastFile.exists()) {
                throw new IOException("Contiguity analysis (Quast) did not complete successfully for: " + args.getName() +
                    "; Could not find: " + quastFile.getAbsolutePath());
            }

            results.mergeWithQuastResults(quastFile, statsDir, args.getName());
        }

        if (args.getStatsLevels().contains(StatsLevel.COMPLETENESS)) {

            List<File> cegmaFiles = this.getCegmaFiles(new File(statsDir, "cegma"));

            for(File cegmaFile : cegmaFiles) {

                if (!cegmaFile.exists()) {
                    throw new IOException("Completeness analysis (CEGMA) did not complete successfully for: " +
                            args.getName() + "; Could not find: " + cegmaFile.getAbsolutePath());
                }

                String assemblyName = FilenameUtils.getBaseName(cegmaFile.getName());

                results.mergeWithCegmaResults(cegmaFile,
                        new File(statsDir, assemblyName + ".fa"),
                        assemblyName,
                        args.getName());
            }
        }

        // Save results to output directory so we have a record
        results.save(new File(args.getOutputDir(), "stats.txt"));

        // Return results
        return results;
    }


    /**
     * Gets all the CEGMA files in the directory specified by the user.
     * @param cegmaDir
     * @return A list of CEGMA files in the current directory
     */
    protected List<File> getCegmaFiles(File cegmaDir) {

        if (cegmaDir == null || !cegmaDir.exists())
            return null;

        List<File> fileList = new ArrayList<>();

        Collection<File> fileCollection = FileUtils.listFiles(cegmaDir, new String[]{"cegma"}, true);

        for(File file : fileCollection) {
            fileList.add(file);
        }

        Collections.sort(fileList);

        return fileList;
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
