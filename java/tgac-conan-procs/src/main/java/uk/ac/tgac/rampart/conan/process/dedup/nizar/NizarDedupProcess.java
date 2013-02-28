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
package uk.ac.tgac.rampart.conan.process.dedup.nizar;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.tgac.rampart.conan.process.dedup.Deduplicator;

import java.io.File;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 17:24
 */
public class NizarDedupProcess extends AbstractConanProcess implements Deduplicator {

    @Override
    public void setInputFile(File inputFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getOutputFile() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCommand() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /*

    #!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/modules";

# 3rd Part modules
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use Cwd;
use Cwd 'abs_path';
use File::Basename;

# RAMPART modules
use QsOptions;
use Configuration;
use SubmitJob;
use AppStarter;

# Script locations
my ( $RAMPART, $RAMPART_DIR ) = fileparse( abs_path($0) );
my $NIZAR_SCRIPT_DIR = $RAMPART_DIR . "tools/nizar/";
my $LEF_PATH 				= $NIZAR_SCRIPT_DIR . "length_extract_fasta";
my $LEFLTL_PATH   			= $NIZAR_SCRIPT_DIR . "length_extract_fasta_less_than_limit";
my $EXONERATE_PATH  		= $NIZAR_SCRIPT_DIR . "exonerate_cmd";
my $SEARCH_EXON_RYO_PATH    = $NIZAR_SCRIPT_DIR . "search_exonerate_ryo";
my $EX_SEQID_PATH    		= $NIZAR_SCRIPT_DIR . "extract_seq_ids_from_exonerate_ryo";
my $EX_FASTA_ID_PATH    	= $NIZAR_SCRIPT_DIR . "extract_fasta_records_on_ids";
my $FASTA_FORMATTER			= "fasta_formatter";

my $SOURCE_FASTX			= AppStarter::getAppInitialiser("FASTX");
my $SOURCE_EXONERATE		= AppStarter::getAppInitialiser("EXONERATE");

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Parse tool specific options
my (%opt) = ( );

GetOptions(
	\%opt,
	'help|usage|h|?',
	'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

die "Error: No input scaffold file specified\n\n" unless $qst->getInput();

my $cmd_line = "";

# Display configuration settings if requested.
if ($qst->isVerbose()) {
	print 	"\n\n" .
			$qst->toString() . "\n\n";
}


# First clean output directory and format input file to single line
#1) perl ~droun/bin/length_extract_fasta 1000 $INPUT_FILE > over_1kb.fasta
#2) perl ~droun/bin/length_extract_fasta_less_than_limit $INPUT_FILE > less_1kb.fasta
#3) ~droun/bin/exonerate_cmd less_1kb.fasta over_1kb.fasta sub_1kb_vs_over_1kb.exonerate
#4) perl ~droun/bin/search_exonerate_ryo sub_1kb_vs_over_1kb.exonerate 95 95 duplication 50 0_both > filtered.sub_1kb_vs_over_1kb.exonerate
#5) perl ~droun/bin/extract_seq_ids_from_exonerate_ryo.pl filtered.sub_1kb_vs_over_1kb.exonerate > ids.list
#6) sort -u ids.list > ids.list.sorted
#7) perl ~droun/bin/extract_fasta_records_on_ids -i ids.list.sorted -s $INPUT_FILE -c cleaned.$INPUT_FILE.fasta -r removed.$INPUT_FILE.fasta

# Setup files
my $basename_input = basename($qst->getInput());
my $sl_file = $qst->getOutput() . "/in-sl.fasta";
my $o1k_file = $qst->getOutput() . "/over_1kb.fasta";
my $l1k_file = $qst->getOutput() . "/less_1kb.fasta";
my $exon_file = $qst->getOutput() . "/less_1kb_vs_over_1kb.exonerate";
my $filtered_file = $qst->getOutput() . "/filtered.sub_1kb_vs_over_1kb.exonerate";
my $ids_file = $qst->getOutput() . "/ids.list";
my $sorted_ids_file = $qst->getOutput() . "/ids.list.sorted";
my $output_cleaned = $qst->getOutput() . "/cleaned.fasta";
my $output_removed = $qst->getOutput() . "/removed.fasta";


# Clean directory (to ensure we overwrite all files)
my @rm_args = grep{$_} (
	"rm -f " . $sl_file,
	"rm -f " . $o1k_file,
	"rm -f " . $l1k_file,
	"rm -f " . $exon_file,
	"rm -f " . $filtered_file,
	"rm -f " . $ids_file,
	"rm -f " . $sorted_ids_file,
	"rm -f " . $output_cleaned,
	"rm -f " . $output_removed
);
my $rm_cmd = join "; ", @rm_args;

# Convert input to single line seq format using FASTX
my @sl_args = grep{$_} (
	$SOURCE_FASTX,
	$FASTA_FORMATTER,
	"-i " . $qst->getInput(),
	"-o " . $sl_file
);
my $sl_cmd = join " ", @sl_args;


# Step 1 - Get large scaffolds ( >= 1kb )
my @o1k_args = grep{$_} (
	$LEF_PATH,
	"1000",
	$sl_file,
	">",
	$o1k_file
);
my $o1k_cmd = join " ", @o1k_args;

# Step 2 - Get small scaffolds ( < 1kb )
my @l1k_args = grep{$_} (
	$LEFLTL_PATH,
	"1000",
	$sl_file,
	">",
	$l1k_file
);
my $l1k_cmd = join " ", @l1k_args;

# Step 3 - Exonerate small scaffolds against large scaffolds
my @exon_args = grep{$_} (
	$SOURCE_EXONERATE,
	$EXONERATE_PATH,
	$l1k_file,
	$o1k_file,
	$exon_file
);
my $exon_cmd = join " ", @exon_args;

# Step 4 - Filter exonerate results
my @filter_args = grep{$_} (
	$SEARCH_EXON_RYO_PATH,
	$exon_file,
	"95",
	"95",
	"duplication",
	"50",
	"0_both",
	">",
	$filtered_file
);
my $filter_cmd = join " ", @filter_args;


# Step 5 - Get seq IDs
my @get_ids_args = grep{$_} (
	$EX_SEQID_PATH,
	$filtered_file,
	">",
	$ids_file
);
my $get_ids_cmd = join " ", @get_ids_args;


#Step 6 - Sort IDs
my @sort_args = grep{$_} (
	"sort",
	"-u",
	$ids_file,
	">",
	$sorted_ids_file
);
my $sort_cmd = join " ", @sort_args;


# Step 7 - Extract FASTA Records based on the sorted IDs
my @extract_args = grep{$_} (
	$EX_FASTA_ID_PATH,
	"-i " . $sorted_ids_file,
	"-s " . $sl_file,
	"-c " . $output_cleaned,
	"-r " . $output_removed
);
my $extract_cmd = join " ", @extract_args;


# Combine all the commands and submit to the Grid Engine
my @cmd_args = grep{$_} (
	$rm_cmd,
	$sl_cmd,
	$o1k_cmd,
	$l1k_cmd,
	$exon_cmd,
	$filter_cmd,
	$get_ids_cmd,
	$sort_cmd,
	$extract_cmd
);
my $full_cmd = join "; ", @cmd_args;


# Submit the deduplication job
SubmitJob::submit($qst, $full_cmd);



__END__

=pod

=head1 NAME

B<dedup.pl>


=head1 SYNOPSIS

B<dedup.pl> [options] B<--input> F<input_scaffold_file.fa>

For full documentation type: "dedup.pl --man"


=head1 DESCRIPTION

This script is designed to execute a fasta deduplication jobs on a grid engine.  The deduplication process involves separating the input scaffold file into 2 groups, those scaffolds that are under 1k and those that are over 1k in length.  The smaller scaffolds are exonerated against the larger scaffolds and the output is filtered and sorted to indentify those smaller scaffolds for which there is strong evidence of redundancy.  Two files are produced, one which has the redundant scaffolds removed, and another that contains the redundant scaffolds.


=head1 OPTIONS

=over

=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

=item B<--tool>,B<-t>

If this script supports multiple tools to do the same job you can specify that tool using this param.

=item B<--tool_path>,B<--tp>

The path to the tool, or name of the tool's binary file if on the path.

=item B<--project_name>,B<--project>,B<-p>

The project name for the job that will be placed on the grid engine.

=item B<--job_name>,B<--job>,B<-j>

The job name for the job that will be placed on the grid engine.

=item B<--wait_condition>,B<--wait>,B<-w>

If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

=item B<--queue>,B<-q>

The queue to which this job should automatically be sent.

=item B<--memory>,B<--mem>,B<-m>

The amount of memory to reserve for this job.

=item B<--threads>,B<-n>

The number of threads that this job is likely to use.  This is used to reserve cores from the grid engine.

=item B<--extra_args>,B<--ea>

Any extra arguments that should be sent to the grid engine.

=item B<--input>,B<--in>,B<-i>

REQUIRED: The input scaffold file for this job.

=item B<--output>,B<--out>,B<-o>

The output directory for this job.  Recommended that you select a clean directory for this.  Default: Current working directory (".")

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=back


=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut



     */

}
