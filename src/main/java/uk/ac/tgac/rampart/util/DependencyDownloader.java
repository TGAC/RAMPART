package uk.ac.tgac.rampart.util;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 25/10/13
 * Time: 11:14
 * To change this template use File | Settings | File Templates.
 */
public class DependencyDownloader {

    // Constants
    public static final String SUBDIR = "rampart_dependencies";

    public static final String DESC = "Downloads source code packages for RAMPART dependencies.";

    // **** Option parameter names ****
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_HELP = "help";

    // **** Options ****
    private File targetDir;
    private boolean verbose;
    private boolean help;

    public DependencyDownloader() {
        this.targetDir = new File("");
        this.verbose = false;
        this.help = false;
    }

    public DependencyDownloader(String[] args) throws ParseException {

        // Parse the command line arguments
        CommandLine cmdLine = new PosixParser().parse(createOptions(), args, true);

        // Extract optional boolean flags
        this.help = cmdLine.hasOption(OPT_HELP);
        this.verbose = cmdLine.hasOption(OPT_VERBOSE);

        // If there's a remaining argument then this will be the target directory, if not then assume the user wants
        // to download packages into the current working directory
        this.targetDir = cmdLine.getArgList().isEmpty() ? new File("") : new File((String)cmdLine.getArgList().get(0));
    }


    private static Options createOptions() {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(new Option("v", OPT_VERBOSE, false, "Output extra information while running."));
        options.addOption(new Option("?", OPT_HELP, false, "Print this message."));

        return options;
    }


    private static void printHelp() {

        CommandLineHelper.printHelp(
                System.err,
                "rampart-download-deps [<target_dir>]",
                "RAMPART dependency downloader tool\n\n" +
                "This tool downloads source code packages for RAMPART dependencies.  Note: that this tool does not try " +
                "to install the downloaded file, it only downloads them to save the user having to do this manually.\n\n" +
                "If no final argument is specified then the packages are downloaded into a sub-directory called \"" + SUBDIR + "\" " +
                "within the current working directory, otherwise the dependencies will be downloaded into a directory " +
                "specified by this final argument.\n\n",
                createOptions());
    }

    public void execute() throws IOException {

        if (this.help) {
            printHelp();
        }
        else {
            run(this.targetDir);
        }
    }

    public void run(File targetDir) throws IOException {

        System.out.println("Downloading RAMPART dependencies into: " + targetDir.getAbsolutePath());

        File dependencyDir = new File(targetDir, SUBDIR);

        // If no job directory is specified assume we want to clean the current directory
        File mecqDir = new File(dependencyDir, "mecq");
        File massDir = new File(dependencyDir, "mass");
        File ampDir = new File(dependencyDir, "amp");
        File statsDir = new File(dependencyDir, "stats");
        File kmerDir = new File(dependencyDir, "kmer");


        // Download source packages

        // Sickle

        File sickleDir = new File(mecqDir, "sickle");
        createDir(sickleDir);

        downloadFromUrl(
                new URL("https://github.com/najoshi/sickle/archive/v1.2.tar.gz"),
                new File(sickleDir, "sickle-v1.2.tar.gz"));

        downloadFromUrl(
                new URL("http://zlib.net/zlib-1.2.8.tar.gz"),
                new File(sickleDir, "zlib-1.2.8.tar.gz"));

        // Quake
        downloadFromUrl(
                new URL("http://www.cbcb.umd.edu/software/quake/downloads/quake-0.3.5.tar.gz"),
                new File(mecqDir, "quake-0.3.5.tar.gz"));



        // Abyss
        File abyssDir = new File(massDir, "abyss");
        createDir(abyssDir);

        downloadFromUrl(
                new URL("https://github.com/bcgsc/abyss/releases/download/1.5.2/abyss-1.5.2.tar.gz"),
                new File(abyssDir, "abyss-1.5.2.tar.gz"));

        downloadFromUrl(
                new URL("http://sourceforge.net/projects/boost/files/boost/1.54.0/boost_1_54_0.tar.gz/download"),
                new File(abyssDir, "boost_1_54_0.tar.gz"));

        downloadFromUrl(
                new URL("https://code.google.com/p/sparsehash/downloads/detail?name=sparsehash-2.0.2.tar.gz&can=2&q="),
                new File(abyssDir, "sparsehash-2.0.2.tar.gz"));

        downloadFromUrl(
                new URL("http://www.open-mpi.org/software/ompi/v1.6/downloads/openmpi-1.6.5.tar.gz"),
                new File(abyssDir, "openmpi-1.6.5.tar.gz"));

        // ALLPATHS-LG
        downloadFromUrl(
                new URL("ftp://ftp.broadinstitute.org/pub/crd/ALLPATHS/Release-LG/latest_source_code/allpathslg-50960.tar.gz"),
                new File(massDir, "allpathslg-50960.tar.gz"));

        // SOAP denovo 2
        downloadFromUrl(
                new URL("http://sourceforge.net/projects/soapdenovo2/files/SOAPdenovo2/src/r240/SOAPdenovo2-src-r240.tgz/download"),
                new File(massDir, "SOAPdenovo2-src-r240.tgz"));

        // Platanus
        downloadFromUrl(
                new URL("http://platanus.bio.titech.ac.jp/Platanus_release/20130901010201/platanus"),
                new File(massDir, "platanus"));

        // SPAdes
        downloadFromUrl(
                new URL("http://spades.bioinf.spbau.ru/release3.1.1/SPAdes-3.1.1.tar.gz"),
                new File(massDir, "SPAdes-3.1.1.tar.gz"));

        // Velvet
        downloadFromUrl(
                new URL("https://www.ebi.ac.uk/~zerbino/velvet/velvet_1.2.10.tgz"),
                new File(massDir, "velvet_1.2.10.tgz"));


        // TGAC subsampler
        downloadFromUrl(
                new URL("https://github.com/homonecloco/subsampler/archive/master.zip"),
                new File(massDir, "tgac_subsampler-master.zip"));

        // CEGMA
        File cegmaDir = new File(statsDir, "cegma");
        createDir(cegmaDir);

        downloadFromUrl(
                new URL("http://korflab.ucdavis.edu/datasets/cegma/cegma_v2.4.010312.tar.gz"),
                new File(cegmaDir, "cegma_v2.4.010312.tar.gz"));

        downloadFromUrl(
                new URL("http://korflab.ucdavis.edu/Unix_and_Perl/FAlite.pm"),
                new File(cegmaDir, "FAlite.pm"));

        downloadFromUrl(
                new URL("ftp://genome.crg.es/pub/software/geneid/geneid_v1.4.4.Jan_13_2011.tar.gz"),
                new File(cegmaDir, "geneid_v1.4.4.Jan_13_2011.tar.gz"));

        downloadFromUrl(
                new URL("ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.2.28/ncbi-blast-2.2.28+-src.tar.gz"),
                new File(cegmaDir, "ncbi-blast-2.2.28+-src.tar.gz"));

        downloadFromUrl(
                new URL("http://www.ebi.ac.uk/~birney/wise2/wise2.4.1.tar.gz"),
                new File(cegmaDir, "wise2.4.1.tar.gz"));

        // Quast
        downloadFromUrl(
                new URL("http://sourceforge.net/projects/quast/files/quast-2.2.tar.gz/download"),
                new File(statsDir, "quast-2.2.tar.gz"));

        // GapCloser
        createDir(ampDir);

        downloadFromUrl(
                new URL("http://sourceforge.net/projects/soapdenovo2/files/GapCloser/src/r6/GapCloser-src-v1.12-r6.tgz/download"),
                new File(ampDir, "GapCloser-src-v1.12-r6.tgz"));

        // Reapr
        downloadFromUrl(
                new URL("ftp://ftp.sanger.ac.uk/pub/resources/software/reapr/Reapr_1.0.17.tar.gz"),
                new File(ampDir, "Reapr_1.0.17.tar.gz")
        );


        // Kmer
        createDir(kmerDir);

        downloadFromUrl(
                new URL("https://github.com/TGAC/KAT/releases/download/V1.0.5/kat-1.0.5.tar.gz"),
                new File(kmerDir, "kat-1.0.5.tar.gz"));

        downloadFromUrl(
                new URL("http://www.cbcb.umd.edu/software/jellyfish/jellyfish-1.1.10.tar.gz"),
                new File(kmerDir, "jellyfish-1.1.10.tar.gz"));

        downloadFromUrl(
                new URL("http://packages.seqan.de/seqan-src/seqan-src-1.4.1.tar.gz"),
                new File(kmerDir, "seqan-src-1.4.1.tar.gz"));

        downloadFromUrl(
                new URL("http://kmergenie.bx.psu.edu/kmergenie-1.6741.tar.gz"),
                new File(kmerDir, "kmergenie-1.6741.tar.gz"));



        // SSPACE
        //TODO Can't do this because of users must fill in a form on the SSPACE website: SSPACE Basic v2.0

        System.out.println("Finished downloading.");
    }

    private void createDir(File dir) throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create directory: " +
                        dir.getAbsolutePath() + ";  Please check permissions.");
            }
        }
    }

    public void downloadFromUrl(URL url, File localFile) throws IOException {

        System.out.print("Downloading: " + url.toString() + " ... ");

        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(localFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        if (!localFile.exists()) {
            System.err.println("Could not download: " + url.toString() + "; URL may have changed.");
        }

        System.out.println("done.");
    }

    /**
     * The main entry point for RAMPART's dependency downloader.
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        // Process the command line
        try {
            new DependencyDownloader(args).execute();
        }
        catch (IllegalArgumentException | ParseException e) {
            System.err.println(e.getMessage());
            printHelp();
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(2);
        }
    }
}
