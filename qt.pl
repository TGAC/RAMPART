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


# Validation
die "Error: input 1 not specified\n\n" unless $opt{in1};
die "Error: input 2 not specified\n\n" unless $opt{in2};
die "Error: output 1 not specified\n\n" unless $opt{out1};
die "Error: output 2 not specified\n\n" unless $opt{out2};
die "Error: output singles not specified\n\n" unless $opt{sout};



my $cmd_line = "";
my $cd = 0;


# Display configuration settings if requested.
if($qst->isVerbose()) {
	print 	"\n\n" .
			$qst->toString() .
			"in1: " . $opt{in1} . "\n" .
			"in2: " . $opt{in2} . "\n" .
			"out1: " . $opt{out1} . "\n" .
			"out2: " . $opt{out2} . "\n" .
			"sout: " . $opt{sout} . "\n\n";
}

my $tool = $qst->getTool();

# Select the scaffolder and build the command line
if ($tool eq $T_SICKLE) {

	my @sickle_args = grep {$_} (
		$SICKLE_SOURCE_CMD,
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

B<qt.pl>


=head1 SYNOPSIS

B<qt.pl> [options] B<--in1> F<input_file_1.fq> B<--in2> F<input_file_2.fq> B<--out1> F<output_file_1.fq> B<--out2> F<output_file_2.fq> B<--sout> F<singles_output_file.fq>

For full documentation type: "qt.pl --man"


=head1 DESCRIPTION

Quality trimming tool that allows the user to execute a quality trimmer on a grid engine.  Assumes the user wants to quality trim paired end or mate pair data.


=head1 OPTIONS

=over

=item B<--in1>

REQUIRED: First input file.
              
=item B<--in2>

REQUIRED: Second input file.
  
=item B<--out1>

REQUIRED: First output file.
  
=item B<--out2>

REQUIRED: Second input file.
  
=item B<--sout>

REQUIRED: Singles output file.
  
=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

=item B<--tool>,B<-t>

Currently these quality trimming tools are supported: (sickle).  Default: sickle.

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

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

