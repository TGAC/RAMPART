package uk.ac.tgac.rampart.tool.process.finalise;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.tgac.conan.process.asmIO.AbstractAssemblyIOArgs;
import uk.ac.tgac.conan.process.asmIO.AbstractAssemblyIOProcess;

/**
 * This is derived from Richard's FastA-to-AGP script in TGAC tools, which is in turn derived form Shaun Jackman's
 * FastA-to-AGP script in Abyss.
 */
public class FinaliseProcess extends AbstractConanProcess {

    public static final String NAME = "Finalise";

    public FinaliseProcess() {
        this(new FinaliseArgs());
    }

    public FinaliseProcess(FinaliseArgs args) {
        super("", args, new FinaliseParams());
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void doStuff() {
        /*open(INPUTFILE, $input_file) or die "Can't open reads file $input_file\n";
        open(CONTIGFILE, ">".$contig_file) or die "Can't open output file $contig_file\n";
        open(SCAFFOLDFILE, ">".$scaffold_file) or die "Can't open output file $scaffold_file\n";
        open(AGPFILE, ">".$agp_file) or die "Can't open output file $agp_file\n";
        open(TRANSLATIONFILE, ">".$translation_file) if defined $translation_file;

        my $current_id = "";
        my $current_contig = "";

        while (<INPUTFILE>) {
            chomp(my $line = $_);
            if ($_ =~ /^>(\S+)/) {
                if ($current_contig ne "") {
                    process_object($current_id, $current_contig);
                }
                $current_contig = "";
                $current_id = $1;
            } else {
                $current_contig .= $line;
            }
        }

        if ($current_contig ne "") {
            process_object($current_id, $current_contig);
        }

        close(AGPFILE);
        close(SCAFFOLDFILE);
        close(CONTIGFILE);
        close(INPUTFILE);
        close(TRANSLATIONFILE) if defined $translation_file;

        print "DONE.\n";

        sub process_object
        {
            my $id = $_[0];
            my $scaf_seq = $_[1];
            my $scaf_len = length($scaf_seq);
            my $scaf_id = $stem."_scaffold_".++$scaffold_n;

            print "Got ID $id\n";

            print SCAFFOLDFILE ">$scaf_id\n";
            print SCAFFOLDFILE $scaf_seq, "\n";

            print TRANSLATIONFILE "$scaf_id\t$id\n" if defined $translation_file;

            my @contig_seqs = split /([Nn]{$min_n,})/, $scaf_seq;
            my $line_number = 1;
            my $position = 1;
            for my $contig_seq (@contig_seqs) {
                my $len = length($contig_seq);
            # object object_beg object_end part_number
            print AGPFILE $scaf_id, "\t", $position, "\t", $position + $len - 1, "\t", $line_number, "\t";

            if ($contig_seq =~ /^[nN]/) {
            # component_type gap_length gap_type linkage
            print AGPFILE "N\t", $len, "\tscaffold\tyes\tpaired-ends\n";
        } else {
            my $contig_id = $stem."_contig_".++$contig_n;

            # component_type component_id component_beg component_end orientation
            print AGPFILE "W\t", $contig_id, "\t1\t", $len, "\t+\n";
            print CONTIGFILE '>', $contig_id, "\n", $contig_seq, "\n";
            print TRANSLATIONFILE $contig_id, "\t", $id, "\n" if defined $translation_file;
        }

            $line_number++;
            $position += $len;
            }
        }
                    */

    }
}
