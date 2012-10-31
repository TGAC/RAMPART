#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use QsOptions;
use SubmitJob;
use Configuration;

# Tool constants
my $T_SICKLE = "sickle";
my $DEF_TOOL = $T_SICKLE;

# Tool path constants
my $TP_SICKLE = "sickle";
my $DEF_TOOL_PATH = $TP_SICKLE;

# Command constants
my $SICKLE_SOURCE_CMD = "source sickle-1.1;";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->parseOptions();


# Parse tool specific options
my %opt;
GetOptions (
	\%opt,
	'in1=s',
	'in2=s',
	'out1=s',
	'out2=s',
	'sout=s',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


my $cmd_line = "";
my $cd = 0;


# Display configuration settings if requested.
if($qst->isVerbose()) {
	print "\n\n" .
	$qst->toString() .
	"Config: " . $opt{config} . "\n\n";
}

my $tool = $qst->getTool();

# Select the scaffolder and build the command line
if ($tool eq $T_SICKLE) {

	my @sickle_args = grep {$_} (
		$qst->getToolPath(),
		"pe",
		"-q 30 -l 75 -n -t sanger",
		"-f " . $opt{in1},
		"-r " . $opt{in2},
		"-o " . $opt{out1},
		"-p " . $opt{out2},
		"-s " . $opt{sout}
	);
	
	$cmd_line = join " ", @sickle_args;

}
else {
	die "Error: Invalid scaffolder requested.  Also, the script should not have got this far!!!.\n\n";
}


# Submit the scaffolding job
SubmitJob::submit($qst, $cmd_line);

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"~Quality Trimmer has successfully submitted the trimming job to the grid engine.  You will be notified by email when the job has completed.\n";
}



__END__

=pod

=head1 NAME

  qt.pl


=head1 SYNOPSIS

  qt.pl [options] --in1 <input_file1> --in2 <input_file2> --out1 <output_file1> --out2 <output_file2> --sout <singles_output_file>

  For full documentation type: "qt.pl --man"


=head1 DESCRIPTION

  Quality trimming tool that allows the user to execute a quality trimmer on a grid engine.  Assumes the user wants to quality trim paired end or mate pair data.


=head1 OPTIONS

  --in1
              REQUIRED: First input file.
              
  --in2
              REQUIRED: Second input file.
  
  --out1
              REQUIRED: First output file.
  
  --out2
              REQUIRED: Second input file.
  
  --sout
              REQUIRED: Singles output file.
  
  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

  --tool                 -t
              Currently these quality trimming tools are supported: (sickle).  Default: sickle.

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
  
  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

