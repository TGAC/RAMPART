#!/usr/bin/perl

use strict;

#### Packages
use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;


#### Constants

# Now
my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;


# Project constants
$DEF_PROJECT_NAME = "Rampart_" . $NOW;
$JOB_PREFIX = $ENV{'USER'} . "-rampart-";

# Queueing system constants
my $SUBMIT = "bsub";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


# Gather Command Line options and set defaults
my (%opt) = (	"assembler_path",	$DEF_ASSEMBLER_PATH,
		"scaffolder_path",	$DEF_SSPACE_PATH,
		"gap_closer_path",	$DEF_GC_PATH,
		"output",		$PWD );

GetOptions (
	\%opt,
	'assembler|a=s',
	'assembler_path|ap=s',
	'scaffolder|s=s',
	'scaffolder_path|sp=s',
	'gap_closer|gc=s',
	'gap_closer_path|gcp=s',
	'project|p=s',
	'raw_config|rc=s',
	'qt_config|qtc=s',
	'kmin=i',
	'kmax=i',
	'output|out|o=s',
	'verbose|v',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




# Validation

die "Error: No output directory specified\n\n" unless $opt{output};
die "Error: No raw library config file specified\n\n" unless $opt{raw_config};
die "Error: No quality trimmed library config file specified\n\n" unless $opt{qt_config};


# Interpret config files


# Process (all steps to be controlled via cmd line options)

my $project_arg = "-P " . $opt{project};


# Run assemblies with appropriate kmer settings (for both raw and qt datasets)
if ($opt{assembler}) {

	my $raw_input = 1;	# Need to gather from config file
	my $qt_input = 1;	# Need to gather from config file

	run_assembler($raw_input);
	run_assembler($qt_input);
}

# Generate stats for each assembly

# Determine best assembly


# Improve best assembly

# Run scaffolding step
if ($opt{scaffolder}) {


}

# Run gap closing step
if ($opt{gap_closer}) {

}

# Generate final stats






sub run_assembler {

	my $assembler_arg = "--assembler " . $opt{assembler};
	my $job_prefix_arg = "--job_prefix " . $JOB_PREFIX . $opt{assembler};
	my $kmin_arg = "--kmin " . $opt{kmin} if $opt{kmin};
	my $kmax_arg = "--kmax " . $opt{kmax} if $opt{kmax};

	my $assembly_args = $job_prefix_arg . " " . $project_arg . " " . $assembler_arg . " " . $kmin_arg . " " $kmax_arg;

	system("assembler.pl", $assembly_args, $_[0]);
}


