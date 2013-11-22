package uk.ac.tgac.rampart.tool.process.finalise;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.*;

/**
 * This is derived from Richard's FastA-to-AGP script in TGAC tools, which is in turn derived form Shaun Jackman's
 * FastA-to-AGP script in Abyss.
 */
public class FinaliseProcess extends AbstractConanProcess {

    private static Logger log = LoggerFactory.getLogger(FinaliseProcess.class);

    public static final String NAME = "Finalise";

    private BufferedReader reader;
    private PrintWriter contigWriter;
    private PrintWriter scaffoldWriter;
    private PrintWriter agpWriter;
    private PrintWriter translationWriter;

    private FinaliseArgs args;

    private int scaffoldId;
    private int contigId;

    public FinaliseProcess() {
        this(new FinaliseArgs());
    }

    public FinaliseProcess(ProcessArgs args) {
        super("", args, new FinaliseParams());

        this.reader = null;
        this.contigWriter = null;
        this.scaffoldWriter = null;
        this.agpWriter = null;
        this.translationWriter = null;

        this.scaffoldId = 0;
        this.contigId = 0;

        this.args = (FinaliseArgs)args;
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
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        this.scaffoldId = 0;
        this.contigId = 0;

        try {

            log.info("Starting finalising process to standardise assembly names.  Note that finaliser does not call out to external processes.");

            // Create the output directory
            if (args.getOutputDir().exists()) {
                FileUtils.deleteDirectory(args.getOutputDir());
            }
            args.getOutputDir().mkdir();

            this.reader = new BufferedReader(new FileReader(args.getInputFile()));
            this.contigWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".contigs.fa"))));
            this.scaffoldWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".scaffolds.fa"))));
            this.agpWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".agp"))));
            this.translationWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(args.getOutputDir(), args.getOutputPrefix() + ".translation"))));

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

        return true;
    }

    private void processObject(String currentHeader, String currentContig) {

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

}
