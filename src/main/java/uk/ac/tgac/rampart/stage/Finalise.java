/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.stage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.ArgValidator;
import uk.ac.ebi.fgpt.conan.core.param.NumericParameter;
import uk.ac.ebi.fgpt.conan.core.param.ParameterBuilder;
import uk.ac.ebi.fgpt.conan.core.param.PathParameter;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.core.process.AbstractProcessArgs;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ResourceUsage;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.AbstractProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.rampart.RampartCLI;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is derived from Richard's FastA-to-AGP script in TGAC tools, which is in turn derived form Shaun Jackman's
 * FastA-to-AGP script in Abyss.
 */
public class Finalise extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Finalise.class);

    public static final String NAME = "Finalise";

    private BufferedReader reader;
    private PrintWriter contigWriter;
    private PrintWriter scaffoldWriter;
    private PrintWriter agpWriter;
    private PrintWriter translationWriter;

    private int scaffoldId;
    private int contigId;

    public Finalise() {
        this(null);
    }

    public Finalise(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Finalise(ConanExecutorService ces, Args args) {
        super(ces, args);

        this.reader = null;
        this.contigWriter = null;
        this.scaffoldWriter = null;
        this.agpWriter = null;
        this.translationWriter = null;

        this.scaffoldId = 0;
        this.contigId = 0;
    }

    public Args getArgs() {
        return (Args)this.getProcessArgs();
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        log.info("Finaliser stage is operational.");
        return true;
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        this.scaffoldId = 0;
        this.contigId = 0;

        Args args = this.getArgs();

        try {

            File selectMassDir = new File(args.getSampleDir(sample), RampartStage.MASS_SELECT.getOutputDirName());
            File ampDir = new File(args.getSampleDir(sample), RampartStage.AMP.getOutputDirName());

            File input = args.inputFromMass ?
                    new File(selectMassDir, "best.fa") :
                    new File(ampDir, "final.fa");

            this.reader = new BufferedReader(new FileReader(input));
            this.contigWriter = new PrintWriter(new BufferedWriter(new FileWriter(args.getContigsFile(sample))));
            this.scaffoldWriter = new PrintWriter(new BufferedWriter(new FileWriter(args.getScaffoldsFile(sample))));
            this.agpWriter = new PrintWriter(new BufferedWriter(new FileWriter(args.getAGPFile(sample))));
            this.translationWriter = new PrintWriter(new BufferedWriter(new FileWriter(args.getTranslationFile(sample))));

            String currentId = "";
            StringBuilder currentContig = new StringBuilder();

            String line = null;
            while((line = this.reader.readLine()) != null) {

                line = line.trim();

                if (line.startsWith(">")) {
                    if (currentContig.length() > 0) {
                        processObject(currentId, currentContig.toString());
                    }
                    currentContig = new StringBuilder();
                    currentId = line.substring(1);
                }
                else {
                    currentContig.append(line);
                }
            }

            if (currentContig.length() > 0) {
                processObject(currentId, currentContig.toString());
            }

            log.info("Finishing finalising assembly successfully.");
        }
        catch(IOException ioe) {
            throw new ProcessExecutionException(3, ioe);
        }
        finally {
            try {
                if (this.reader != null) this.reader.close();
                if (this.contigWriter != null) this.contigWriter.close();
                if (this.scaffoldWriter != null) this.scaffoldWriter.close();
                if (this.agpWriter != null) this.agpWriter.close();
                if (this.translationWriter != null) this.translationWriter.close();
            }
            catch (IOException ioe) {
                throw new ProcessExecutionException(4, ioe);
            }
        }

        //TODO Just compresses the assembly for now... may want to include plots and results too maybe....
        ExecutionResult result = null;
        if (args.compress) {

            log.info("Compressing output");

            String pwdFull = new File(".").getAbsolutePath();
            String pwd = pwdFull.substring(0, pwdFull.length() - 2);

            String command = "cd " + args.getStageDir(sample).getAbsolutePath() + "; " +
                    "tar -cvzf " + args.getCompressedFile(sample).getName() + " " +
                    args.getContigsFile(sample).getName() + " " +
                    args.getScaffoldsFile(sample).getName() + " " +
                    args.getAGPFile(sample).getName() + " " +
                    args.getTranslationFile(sample).getName() + "; " +
                    "cd " + pwd;

            result = this.conanExecutorService.executeProcess(command, args.getOutputDir(), args.getJobPrefix() + "-compress", 1, 0, 0, false);

            log.info("Output compressed to: " + args.getCompressedFile(sample).getAbsolutePath());
        }

        stopWatch.stop();

        return new DefaultTaskResult(
                sample.name + "-" + args.stage.getOutputDirName(),
                true,
                new ArrayList<ExecutionResult>(),
                stopWatch.getTime() / 1000L);
    }

    private void processObject(String currentHeader, String currentContig) {

        Args args = this.getArgs();

        int scaffoldLen = currentContig.length();
        String scaffoldHeader = args.getOutputPrefix() + "_scaffold_" + (++scaffoldId) + "-size_" + scaffoldLen;

        this.scaffoldWriter.println(">" + scaffoldHeader);
        this.scaffoldWriter.println(currentContig);

        this.translationWriter.println(scaffoldHeader + "\t" + currentHeader);

        String[] contigSeqs = currentContig.split("[Nn]{" + args.getMinN() + ",}");

        int lineNum = 1;
        int pos = 1;
        for(String contig : contigSeqs) {

            if (!contig.isEmpty()) {
                int contigLen = contig.length();

                int end = pos + contigLen - 1;
                this.agpWriter.print(scaffoldHeader + "\t" + pos + "\t" + end + "\t" + lineNum + "\t");

                if (contig.startsWith("N") || contig.startsWith("n")) {
                    this.agpWriter.print("N\t" + contigLen + "\tscaffold\tyes\tpaired-ends\n");
                }
                else {
                    String contigHeader = args.getOutputPrefix() + "_contig_" + (++contigId) + "-size_" + contigLen;

                    this.agpWriter.print("W\t" + contigHeader + "\t1\t" + contigLen + "\t+\n");
                    this.contigWriter.print(">" + contigHeader + "\n" + contig + "\n");
                    this.translationWriter.print(contigHeader + "\t" + currentHeader + "\n");
                }
                lineNum++;
                pos += contigLen;
            }
        }

    }

    public static class Args extends RampartProcessArgs {

        public static final String KEY_ATTR_PREFIX = "prefix";
        public static final String KEY_ATTR_MIN_N = "min_n";
        public static final String KEY_ATTR_COMPRESS = "compress";

        public static final int DEFAULT_MIN_N = 10;

        private String outputPrefix;
        private boolean inputFromMass;
        private int minN;
        private boolean compress;


        public Args() {

            super(RampartStage.FINALISE);
            this.jobPrefix = "";
            this.outputPrefix = "rampart";
            this.outputDir = RampartCLI.CWD;
            this.minN = DEFAULT_MIN_N;
            this.compress = true;
        }

        public Args(Element element, File outputDir, String jobPrefix, List<Mecq.Sample> samples, Organism organism, String institution, boolean inputFromMass) throws IOException {

            super(RampartStage.FINALISE, outputDir, jobPrefix, samples, organism);

            // Check there's nothing unexpected in this element
            if (!XmlHelper.validate(element,
                    new String[0],
                    new String[] {
                        KEY_ATTR_PREFIX,
                        KEY_ATTR_MIN_N,
                        KEY_ATTR_COMPRESS
                    },
                    new String[0],
                    new String[0])) {
                throw new IllegalArgumentException("Found unrecognised element or attribute in Finaliser");
            }

            this.inputFromMass = inputFromMass;

            String shortInstitute = this.getInitials(institution, "_");
            String shortOrg = this.getInitials(organism.getName(), "_");

            String start = shortInstitute.isEmpty() && shortOrg.isEmpty() ? "rampart" : shortInstitute + shortOrg;
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

            String derivedPrefix = start + today;

            this.outputPrefix = element.hasAttribute(KEY_ATTR_PREFIX) ?
                    XmlHelper.getTextValue(element, KEY_ATTR_PREFIX) :
                    derivedPrefix;

            this.minN = element.hasAttribute(KEY_ATTR_MIN_N) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_MIN_N) :
                    DEFAULT_MIN_N;

            this.compress = element.hasAttribute(KEY_ATTR_COMPRESS) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_COMPRESS) :
                    true;

            if (this.outputPrefix.contains(".") || this.outputPrefix.contains("|")) {
                throw new IllegalArgumentException("Will not use dots or pipes in the assembly headers because this can cause " +
                        "problems for downstream tools.");
            }
        }

        private String getInitials(String string, String suffix) {

            String[] stringParts = string.trim().split(" ");

            String shortString = "";
            if (stringParts.length > 1) {
                for(String part : stringParts) {
                    shortString += part.charAt(0);
                }
                shortString += suffix;
            }
            else if (stringParts.length == 1) {
                shortString = stringParts[0];
                shortString += suffix;
            }

            return shortString;
        }

        public String getOutputPrefix() {
            return outputPrefix;
        }

        public void setOutputPrefix(String outputPrefix) {
            this.outputPrefix = outputPrefix;
        }

        public boolean isInputFromMass() {
            return inputFromMass;
        }

        public void setInputFromMass(boolean inputFromMass) {
            this.inputFromMass = inputFromMass;
        }

        public int getMinN() {
            return minN;
        }

        public void setMinN(int minN) {
            this.minN = minN;
        }

        public boolean isCompress() {
            return compress;
        }

        public void setCompress(boolean compress) {
            this.compress = compress;
        }


        public File getContigsFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), this.outputPrefix + ".contigs.fa");
        }

        public File getScaffoldsFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), this.outputPrefix + ".scaffolds.fa");
        }

        public File getAGPFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), this.outputPrefix + ".agp");
        }

        public File getTranslationFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), this.outputPrefix + ".translation");
        }

        public File getCompressedFile(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), this.outputPrefix + ".tar.gz");
        }

        @Override
        public void parseCommandLine(CommandLine cmdLine) {

        }

        @Override
        public ParamMap getArgMap() {
            return null;
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }


        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}
