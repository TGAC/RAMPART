#!/usr/bin/perl

use strict;

#### Packages
use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;
use QsOptions;


#### Constants

# Project constants
#my $DEF_PROJECT_NAME = "Rampart_" . $NOW;
my $JOB_PREFIX = $ENV{'USER'} . "-rampart-";


# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));

# Assembly stats gathering constants
my $MASS_PATH = $RAMPART_DIR . "mass.pl";
my $MASS_SELECTOR_PATH = $RAMPART_DIR . "mass_selector.pl";
my $IMPROVER_PATH = $RAMPART_DIR . "improver.pl";

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Gather Command Line options and set defaults
my %opt;

GetOptions (
	\%opt,
	'assembler|a=s',
	'extra_assembler_args|ea_args|eaa=s',
	'approx_genome_size|ags=i',
	'improver|i',
	'extra_improver_args|ei_args|eia=s',
	'raw_config|rc=s',
	'qt_config|qtc=s',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




#### Validation

die "Error: No raw library config file specified\n\n" unless $opt{raw_config};
die "Error: No quality trimmed library config file specified\n\n" unless $opt{qt_config};
die "Error: Approximate genome size not specified\n\n" unless $opt{approx_genome_size};


# Interpret config files


#### Process (all steps to be controlled via cmd line options)
my $qs_project_arg = "-P" . $opt{project};
my $script_project_arg = "--project " . $opt{project};
my $ass_job_prefix = $opt{job_prefix} . $opt{assembler} . "-";
my $asd_job_name = $opt{job_prefix} . "assembly_selector";
my $best_ass;


## Run assemblies for both raw and qt datasets
if ($opt{mass}) {

	# Make assemblies output directories
	my $ass_dir = $opt{output} . "/assemblies";
	mkdir $ass_dir;

	my $raw_ass_dir = $ass_dir . "/raw";
	mkdir $raw_ass_dir;

	my $qt_ass_dir = $ass_dir . "/qt";
	mkdir $qt_ass_dir;

	my $raw_input = 1;	# Need to gather raw_dir from config file
	my $qt_input = 1;	# Need to gather qt_dir from config file
	my $raw_ass_job_prefix = $ass_job_prefix . "raw-";
	my $qt_ass_job_prefix = $ass_job_prefix . "qt-";

	# Run the assembler script for each dataset
	run_mass($raw_input, $raw_ass_job_prefix, $raw_ass_dir);
	run_mass($qt_input, $qt_ass_job_prefix, $qt_ass_dir);


	# Run best assembly selector to find "best" assembly (mass will produce stats automatically for us to use here)
	my $raw_stats_file = $raw_ass_dir . "/stats.txt";
	my $qt_stats_file = $qt_ass_dir . "/stats.txt";

	my @ms_args = (	$qst->getQueueingSystemAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $asd_job_name,
			"--wait_condition done(" . $ass_job_prefix . "*)",
			$qst->getQueueAsParam(),
			"--output " . $ass_dir . "/best.path.txt",
			$qst->isVerboseAsParam(),
			"--raw_stats_file " . $raw_stats_file,
			"--qt_stats_file " . $qt_stats_file,
			"--approx_genome_size " . $opt{approx_genome_size} );

	system($MASS_SELECTOR_PATH, join(" ", @ms_args));

	# Extract best assembly from file
	# This bit isn't going to work yet as we need to do this after the previous job has completed
	#open(BA_FILE, $best_ass_file) or die "Can't read " . $best_ass_file . "\n";
	#$best_ass = <BA_FILE>;
	#close (BA_FILE);
}



## Improve best assembly

if ($opt{improve} && $best_ass) {

	my $improver_cmd = $IMPROVER_PATH . " " ;#. $improver_args . " " . $best_ass;
	my $imp_wait_arg = "";

	system($IMPROVER_PATH, $script_project_arg, $imp_wait_arg, $improver_cmd);
}





sub run_mass {

	my @mass_args = (	$qst->getQueueingSystemAsParam(),
						$qst->getProjectNameAsParam(),
						"--job_name " . $_[1],
						$qst->getQueueAsParam(),
						"--output " . $_[2],
						$qst->isVerboseAsParam(),
						$opt{extra_mass_args},
						"--stats" );

	system($MASS_PATH, join(" ", @mass_args), $_[0]);
}

__END__

=pod

=head1 NAME

  rampart.pl


=head1 SYNOPSIS

  rampart.pl [options] --raw_config <file> --qt_config <file>

  For full documentation type: "rampart.pl --man"


=head1 DESCRIPTION

  Runs an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.


=head1 OPTIONS

  job_prefix|job|j                    The prefix string for all rampart child jobs.
  project|p                           The project name for marking the job.
  extra_queue_args|eqa|q              Extra arguments to pass to the queueing system for each child job.  E.g. "-q normal" to move jobs from the production (default) queue to the normal queue.
  assembler|a                         The assembly program to use.
  extra_assembler_args|ea_args|eaa    Any additional arguments to pass to the assembler script.  Type assembler.pl --man for more information.  This script will automatically invoke the assembler script with the project, job_prefix, threads, memory, stats, in_dir, and out_dir settings.  Assembler arguments such as --kmin and --kmax should be set via this argument for example.
  approx_genome_size|ags              The approximate genome size for the organism that is being sequenced.  Used for determining best assembly.
  improver|i
  output|out|o=s                      The output directory.
  verbose|v                           Print extra status information during run.
  help|usage|h|?                      Print usage message and then exit.
  man                                 Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


