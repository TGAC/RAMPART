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
