#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use QsOptions;
use Configuration;
use SubmitJob;

# Gap closing constants
my $T_GAP_CLOSER = "gapcloser";
my $T_IMAGE      = "image";
my $T_GAP_FILLER = "gapfiller";
my $DEF_TOOL     = $T_GAP_CLOSER;

my $TP_GAP_CLOSER = "GapCloser";
my $TP_IMAGE      = "image";
my $TP_GAP_FILLER = "gapfiller";
my $DEF_TOOL_PATH = $TP_GAP_CLOSER;

# Command constants
my $GC_SOURCE_CMD = "source GapCloser-1.12;";

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->parseOptions();

# Parse tool specific options
my (%opt) = ( );

GetOptions( \%opt, 'read_length|readlen|rl|r=i', 'config|c=s', 'help|usage|h|?',
	'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

die "Error: No input scaffold file specified\n\n" unless $qst->getInput();
die "Error: No rampart config file specified\n\n" unless $opt{config};
die "Error: Read length not specified\n\n" unless $opt{read_length};

my $cmd_line = "";

# Display configuration settings if requested.
if ($qst->isVerbose()) {
	print 	"\n\n" .
			$qst->toString() .
			"Config: " . $opt{config} . "\n" .
			"Read Length: " . $opt{read_length} . "\n\n";
}

# Select the gap closer and build the command line
my $tool = $qst->getTool();
if ( $tool eq $T_GAP_CLOSER ) {

	my $rampart_cfg = new Configuration( $opt{config} );
	$rampart_cfg->validate();
	my $gc_cfg_file = $qst->getOutput() . "/gc.cfg";
	write_soap_cfg($rampart_cfg, $gc_cfg_file);	
	
	my $gc_scaffolds  = $qst->getOutput() . "/gc-scaffolds.fa";
	my $gc_other_args = "-p 61";

	$cmd_line =
	    $GC_SOURCE_CMD . " "
	  . $TP_GAP_CLOSER
	  . " -a \""
	  . $qst->getInput()
	  . "\" -b \""
	  . $gc_cfg_file
	  . "\" -o \""
	  . $gc_scaffolds
	  . "\" -l "
	  . $opt{read_length} . " "
	  . $gc_other_args;
}
elsif ( $tool eq $T_IMAGE ) {

	my $image_scaffolds = $qst->getOutput() . "/image-scaffolds.fa";
	$cmd_line = "";

	die "Error: IMAGE (Iterative Mapping and Assembly for Gap Elimination) tool not implemented in this script yet.\n\n";
}
elsif ( $tool eq $T_GAP_FILLER ) {

	my $gf_scaffolds = $opt{output} . "/gf-scaffolds.fa";
	$cmd_line = "";

	die "Error: Gap filler tool not implemented in this script yet.\n\n";
}
else {
	die "Error: Invalid gap closing tool requested.  Also, the script should not have got this far!!!.\n\n";
}

# Submit the scaffolding job
SubmitJob::submit($qst, $cmd_line);

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"Degap has successfully submitted the degapping job to the grid engine.  You will be notified by email when the job has completed.\n";
}



sub write_soap_cfg {
	
	my ( $config, $out_file ) = @_;
	
	
	open OUT, ">", $out_file;
	
	for( my $i = 1; $i <= $config->getNbSections(); $i++ ) {
		
		my $lib = $config->getSectionAt($i-1);
		
		my $ft = $lib->{q1};
		my $file1 = $lib->{q1} ? $lib->{q1} : $lib->{f1} ? $lib->{f1} : undef;
		my $file2 = $lib->{q2} ? $lib->{q2} : $lib->{f2} ? $lib->{f2} : undef;
		
		# We expect to have a valid configuration file here so don't bother throwing
		# any errors from this point... sspace will error anyway if there is a problem.
				
		my @soap_args = (
			"[LIB]",
			"max_rd_len=" . $lib->{max_rd_len},
			"avg_ins=" . $lib->{avg_ins},
			"reverse_seq=" . $lib->{reverse_seq},
			"asm_flags=3",
			"rank=1",
			($ft ? "q1=" : "f1=") . $file1,
			($ft ? "q2=" : "f2=") . $file2
		);
		
		my $line = join "\n", @soap_args;
		
		print OUT $line . "\n";
	}
	
	close OUT;
}



__END__

=pod

=head1 NAME

  degap.pl


=head1 SYNOPSIS

  degap.pl [options] -r <average_read_length> -c <rampart_config_file>  -i <input_scaffold_file>

  For full documentation type: "degap.pl --man"


=head1 DESCRIPTION

  This script is designed to execute degapping jobs on a grid engine.  Degapping the the process of filling gaps in scaffolds, 
  denoted by the nt (N), with real nucleotides (A,T,G,C), by trying to align assembled reads to the scaffolds.  Currently, one 
  degapping tool is support: SOAPdenovo GapCloser.


=head1 OPTIONS

  --read_length          --readlen           --rl                 -r
  			  REQUIRED: The average length of reads to use
  			  
  --config               --cfg               -c
  			  REQUIRED: The rampart config file that describs the reads to be used for this job.
  
  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.

  --tool                 -t
              Currently supported tools include: (gapcloser).  Default tool: gapcloser.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

  --project_name         --project           -p
              The project name for the job that will be placed on the grid engine.

  --job_name             --job               -j
              The job name for the job that will be placed on the grid engine.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --memory               --mem               -m
              The amount of memory to reserve for this job.

  --threads              -n
              The number of threads that this job is likely to use.  This is used to reserve cores from the grid engine.

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --input                --in                -i
              REQUIRED: The input scaffold file for this job.

  --output               --out               -o
              The output dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

