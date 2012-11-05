#!/usr/bin/perl

use strict;
use warnings;

#### Packages
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';
use QsOptions;
use Configuration;

#### Constants

# Scaffold Improver constants
my $DEF_ITERATIONS = 1;

# Clipping constants
my $DEF_MIN_LEN = 1000;

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;

# Script locations
my ( $RAMPART, $RAMPART_DIR ) = fileparse( abs_path($0) );
my $SCAFFOLDER_PATH = $RAMPART_DIR . "scaffolder.pl";
my $DEGAPPER_PATH   = $RAMPART_DIR . "degap.pl";
my $CLIPPER_PATH    = $RAMPART_DIR . "clipper.pl";
my $MASS_GP_PATH    = $RAMPART_DIR . "mass_gp.pl";

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Gather Command Line options and set defaults
my (%opt) = ( 	"clip_args", 	"--min_length " . $DEF_MIN_LEN,
				"iterations", 	$DEF_ITERATIONS );

GetOptions(
	\%opt,
	'scaffolder_args|s_args=s',
	'degap_args|dg_args=s',
	'clip',
	'clip_args=s',
	'config|cfg=s',
	'iterations|i=i',
	'stats',
	'simulate|sim',
	'help|usage|h|?',
	'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

# Validation
die "Error: No input file specified\n\n"       	unless $qst->getInput();
die "Error: No output directory specified\n\n" 	unless $qst->getOutput();
die "Error: No config file specified\n\n" 	   	unless $opt{config};

# Interpret config files

#### Process (all steps to be controlled via cmd line options)

# Build up static args which is to be used by all child jobs
my @static_args = grep {$_} (
    $qst->getGridEngineAsParam(),
	$qst->getProjectNameAsParam(),
	$qst->getExtraArgsAsParam(),
	$qst->getQueueAsParam(),
	$qst->isVerboseAsParam() );

# Make all job name/prefix strings
my $job_prefix     = $qst->getJobName();
my $sg_job_prefix  = $job_prefix . "-stat_gatherer-";
my $scf_job_prefix = $job_prefix . "-scaffold-";
my $dg_job_prefix  = $job_prefix . "-degap-";
my $clip_job_name  = $job_prefix . "-clip";
my $stats_job_name = $job_prefix . "-stats";


## Improve best assembly
my $current_scaffold = $qst->getInput();
my $first_wait = $qst->getWaitCondition() ? 1 : 0;
my $last_job;


# Create an array which will store a record of all the assemblies created by this script.
# Initialise it with the input file. 
my @assemblies;
push @assemblies, $current_scaffold;

# Make output directories
my $output_dir = $qst->getOutput();
my $scf_dir    = $output_dir . "/scaffolds";
my $dg_dir     = $output_dir . "/degap";
mkdir $scf_dir;
mkdir $dg_dir;

for ( my $i = 1 ; $i <= $opt{iterations} ; $i++ ) {

	my $scf_job_name = $scf_job_prefix . $i;
	my $dg_job_name  = $dg_job_prefix . $i;

	# Run scaffolding step

	my $scf_dir_i = $scf_dir . "/" . $i;
	mkdir $scf_dir_i;

	my $scf_wait_arg;
	
	if ($first_wait) {
		$scf_wait_arg = "--wait_condition '" . $qst->getWaitCondition() . "'";
	}
	elsif ($last_job) {
		$scf_wait_arg = "--wait_condition 'ended(" . $last_job . ")'";
	}
	else {
		$scf_wait_arg = "";
	}
	

	my @scf_args = grep {$_} (
		$SCAFFOLDER_PATH,
		@static_args,
		$scf_wait_arg,
		"--job_name " . $scf_job_name,
		"--config " . $opt{config},
		$opt{scaffolder_args} ? $opt{scaffolder_args} : "",
		"--output " . $scf_dir_i,
		"--input " . $current_scaffold );

	system(join " ", @scf_args) unless $opt{simulate};

	$current_scaffold = $scf_dir_i . "/scaffolder.final.scaffolds.fasta";
	$last_job         = $scf_job_name;
	push @assemblies, $current_scaffold;

	# Run gap closing step

	my $dg_dir_i = $dg_dir . "/" . $i;
	mkdir $dg_dir_i;

	my $dg_wait_arg = $last_job ? "--wait_condition 'done(" . $last_job . ")'" : "";

	my @dg_args = grep {$_} (
		$DEGAPPER_PATH,
		@static_args,
		$dg_wait_arg,
		"--job_name " . $dg_job_name,
		"--config " . $opt{config},
		"--output " . $dg_dir_i,
		"--input " . $current_scaffold,
		$opt{degap_args} ? $opt{degap_args} : "" );

	system(join " ", @dg_args ) unless $opt{simulate};

	$current_scaffold = $dg_dir_i . "/gc-scaffolds.fa";
	$last_job         = $dg_job_name;
	push @assemblies, $current_scaffold;
}

## Remove contigs under a user specified length

if ( $opt{clip} ) {

	my $clip_dir = $qst->getOutput() . "/clipped";
	mkdir $clip_dir;

	my $clip_scf_file = $clip_dir . "/clipped-scaffolds.fa";
	my $clip_out_arg = "--output " . $clip_scf_file;
	my $clip_wait_arg = $last_job ? "--wait_condition 'ended(" . $last_job . ")'" : "";

	my @clip_args = grep {$_} (
		$CLIPPER_PATH,
		@static_args,
		"--job_name " . $clip_job_name,
		$clip_wait_arg,
		$opt{clip_args} ? $opt{clip_args} : "",
		"--input " . $current_scaffold,
		$clip_out_arg );

	system(join " ", @clip_args) unless $opt{simulate};

	$current_scaffold = $clip_scf_file;
	$last_job         = $clip_job_name;
	push @assemblies, $current_scaffold;
}

## Remove PhiX??? (This step shouldn't be required in a normal run as the data should be screened already)

## Remove duplicates???

## Generate final stats 
if ( $opt{stats} ) {

	my $stats_dir = $qst->getOutput() . "/stats";
	mkdir $stats_dir;

	# Link to each scaffold file from each stage of this process
	my $j = 1;
	foreach ( @assemblies ) {
		system("ln -s -f " . $_ . " " . $stats_dir . "/" . $j . "-scaffolds.fa");
		$j++;
	}

	my $mgp_wait_arg = $last_job ? "--wait_condition 'ended(" . $last_job . ")'" : "";

	my @mgp_args = grep {$_} (
		$MASS_GP_PATH,
		$qst->getGridEngineAsParam(),
		$qst->getProjectNameAsParam(),
		$qst->getQueueAsParam(),
		"--job_name " . $stats_job_name,
		$mgp_wait_arg,
		$qst->isVerboseAsParam(),
		"--output " . $stats_dir,
		"--input " . $stats_dir,
		"--index"	);

	system(join " ", @mgp_args);
	
	system("ln -s -f " . $assemblies[-1] . " " . $qst->getOutput() . "/final-scaffolds.fa" );

	$last_job = $clip_job_name;
}


__END__

=pod

=head1 NAME

  improver.pl


=head1 SYNOPSIS

  improver.pl [options] -i <input_file>

  For full documentation type: "improver.pl --man"


=head1 DESCRIPTION

  Runs an sequence of programs that attempt to improve the scaffold quality of the input file.  It does this by doing a 
  user defined number of scaffolding and gap fillings steps.  This script uses additional reads specified by the user in 
  the configuration file to do this. After enhancing the user can optionally discard shorter scaffolds from file.  Finally, 
  the user can optionally request assembly statistics to be produced at each step in the process.



=head1 OPTIONS

  --iterations           -i
              The number of scaffolding and degapping iterations to run.  Default: 1.
  
  --simulate
              Runs the script without sending any jobs to the queue.  Used for testing purposes only.
              
  --stats
              Outputs statistics and plots for all scaffold files produced in the improvement process.

  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

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
              The input file(s) for this job.

  --output               --out               -o
              The output file/dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


