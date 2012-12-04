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

# RAMPART modules
use QsOptions;
use Configuration;
use SubmitJob;
use AppStarter;

# Gap closing constants
my $T_GAP_CLOSER = "gapcloser";
my $T_IMAGE      = "image";
my $T_GAP_FILLER = "gapfiller";
my $DEF_TOOL     = $T_GAP_CLOSER;

# Tool path constants
my $TP_GAP_CLOSER = "GapCloser";
my $TP_IMAGE      = "image";
my $TP_GAP_FILLER = "gapfiller";
my $DEF_TOOL_PATH = $TP_GAP_CLOSER;

# Tool versions
my $T_GAP_CLOSER_VERSION = "1.12";
my $T_IMAGE_VERSION = "x.x";
my $T_GAP_FILLER_VERSION = "x.x";

# Command constants
my $GC_SOURCE_CMD = AppStarter::getAppInitialiser("GAP_CLOSER");

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->parseOptions();

# Parse tool specific options
my (%opt) = ();

GetOptions( \%opt, 'config|c=s', 'help|usage|h|?', 'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

die "Error: No input scaffold file specified\n\n" unless $qst->getInput();
die "Error: No rampart config file specified\n\n" unless $opt{config};

my $cmd_line = "";

# Display configuration settings if requested.
if ( $qst->isVerbose() ) {
	print "\n\n" . $qst->toString() . "Config: " . $opt{config} . "\n\n";
}

# Select the gap closer and build the command line
my $tool = $qst->getTool();
if ( $tool eq $T_GAP_CLOSER ) {

	my $rampart_cfg = new Configuration( $opt{config} );
	$rampart_cfg->validate();
	my $gc_cfg_file = $qst->getOutput() . "/gc.cfg";
	write_soap_cfg( $rampart_cfg, $gc_cfg_file );

	my $gc_scaffolds  = $qst->getOutput() . "/gc-scaffolds.fa";
	my $gc_other_args = "-p 61";
	my $read_length   = $rampart_cfg->getSectionAt(0)->{read_length};

	my @gc_args = (
		$GC_SOURCE_CMD,
		$TP_GAP_CLOSER,
		"-a \"" . $qst->getInput() . "\"",
		"-b \"" . $gc_cfg_file . "\"",
		"-o \"" . $gc_scaffolds . "\"",
		"-l " . $read_length . " ",
		$gc_other_args
	);

	$cmd_line = join " ", @gc_args;
}
elsif ( $tool eq $T_IMAGE ) {

	my $image_scaffolds = $qst->getOutput() . "/image-scaffolds.fa";
	$cmd_line = "";

	die
"Error: IMAGE (Iterative Mapping and Assembly for Gap Elimination) tool not implemented in this script yet.\n\n";
}
elsif ( $tool eq $T_GAP_FILLER ) {

	my $gf_scaffolds = $opt{output} . "/gf-scaffolds.fa";
	$cmd_line = "";

	die "Error: Gap filler tool not implemented in this script yet.\n\n";
}
else {
	die
"Error: Invalid gap closing tool requested.  Also, the script should not have got this far!!!.\n\n";
}

# Submit the scaffolding job
SubmitJob::submit( $qst, $cmd_line );

# Notify user of job submission
if ( $qst->isVerbose() ) {
	print "\n"
	  . "Degap has successfully submitted the degapping job to the grid engine.  You will be notified by email when the job has completed.\n";
}

sub write_soap_cfg {

	my ( $config, $out_file ) = @_;

	open OUT, ">", $out_file;

	for ( my $i = 1 ; $i < $config->getNbSections() ; $i++ ) {

		my $lib = $config->getSectionAt($i);

		my $ft = $lib
		  ->{file_paired_1}; # TODO: Need to fix this, doesn't distinguish between FASTQ and FASTA yet (assumes FASTQ)
		my $file1 = $lib->{file_paired_1} ? $lib->{file_paired_1} : undef;
		my $file2 = $lib->{file_paired_2} ? $lib->{file_paired_2} : undef;

 # We expect to have a valid configuration file here so don't bother throwing
 # any errors from this point... sspace will error anyway if there is a problem.

		my $rs = "";

		if ( $lib->{seq_orientation} eq "FR" ) {
			$rs = 0;
		}
		elsif ( $lib->{seq_orientation} eq "RF" ) {
			$rs = 1;
		}

		my $asm = "";

		if ( $lib->{usage} eq "ASSEMBLY_ONLY" ) {
			$asm = "1";
		}
		elsif ( $lib->{usage} eq "SCAFFOLDING_ONLY" ) {
			$asm = "2";
		}
		elsif ( $lib->{usage} eq "ASSEMBLY_AND_SCAFFOLDING" ) {
			$asm = "3";
		}
		else {
			$asm = "0";
		}

		my @soap_args = (
			"[LIB]",
			"max_rd_len=" . $lib->{read_length},
			"avg_ins=" . $lib->{avg_insert_size},
			"reverse_seq=" . $rs,
			"asm_flags=" . $asm,
			"rank=" . $i,
			( $ft ? "q1=" : "f1=" ) . $file1,
			( $ft ? "q2=" : "f2=" ) . $file2
		);

		my $line = join "\n", @soap_args;

		print OUT $line . "\n";
	}

	close OUT;
}

__END__

=pod

=head1 NAME

B<degap.pl>


=head1 SYNOPSIS

B<degap.pl> [options] B<--config> F<config.cfg> B<-i> F<input_scaffold_file.fa>

For full documentation type: "degap.pl --man"


=head1 DESCRIPTION

This script is designed to execute degapping jobs on a grid engine.  Degapping the the process of filling gaps in scaffolds, denoted by the nt (N), with real nucleotides (A,T,G,C), by trying to align assembled reads to the scaffolds.  Currently, one degapping tool is support: SOAPdenovo GapCloser.


=head1 OPTIONS

=over

=item B<--config>,B<--cfg>,B<-c>

REQUIRED: The rampart config file that describs the reads to be used for this job.
  
=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported.

=item B<--tool>,B<-t>

Currently supported tools include: (gapcloser).  Default tool: gapcloser.

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

The output dir for this job. Default: Current working directory ("."); 

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

