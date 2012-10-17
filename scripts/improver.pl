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
use QsTool;


#### Constants

# Tool names
my $DEF_SCAFFOLDER = "sspace";
my $DEF_DEGAP = "gapcloser";
my $DEF_CLIPPER = "nizar";
my $DEDUP_BIN = "";


# Scaffold Improver constants
my $DEF_ITERATIONS = 3;

# Clipping constants
my $DEF_MIN_LEN = 1000;


# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;

# Script locations
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $SCAFFOLDER_PATH = $RAMPART_DIR . "scaffolder.pl";
my $DEGAPPER_PATH = $RAMPART_DIR . "degap.pl";
my $CLIPPER_PATH = $RAMPART_DIR . "clipper.pl";


# Parse generic queueing tool options
my $qst = new QsTool();
$qst->parseOptions();


# Gather Command Line options and set defaults
my (%opt) = (	"clip_args",	"--min_length " . $DEF_MIN_LEN	);

GetOptions (
	\%opt,
	'scaffold_args|s_args=s',
	'degap_args|dg_args=s',
	'clip|c',
	'clip_args',
	'iterations|i=i',
	'stats',
	'simulate|sim',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


# Validation
die "Error: No input file specified\n\n" unless $qst->getInput();
die "Error: No output directory specified\n\n" unless $qst->getOutput();


# Interpret config files






#### Process (all steps to be controlled via cmd line options)


# Build up static args which is to be used by all child jobs
my $queueing_system = $qst->getQueueingSystem() ? "--queueing_system " . $qst->getQueueingSystem() : "";
my $project_arg = $qst->getProjectName() ? "--project_name " . $qst->getProjectName() : "";
my $queue_arg = $qst->getQueue() ? "--queue " . $qst->getQueue : "";
my $extra_args = $qst->getExtraArgs() ? "--extra_args \"" . $qst->getExtraArgs . "\"" : "";
my $verbose_arg = $qst->isVerbose() ? "--verbose" : "";
my $static_args = $queueing_system . " " . $project_arg . " " . $extra_args . " " . $queue_arg . " " . $verbose_arg;

# Make all job name/prefix strings
my $job_prefix = $qst->getJobName();
my $sg_job_prefix = $job_prefix . "stat_gatherer-";
my $scf_job_prefix = $job_prefix . "scaffold-";
my $dg_job_prefix = $job_prefix . "degap-";
my $clip_job_name = $job_prefix . "clip";
my $stats_job_name = $job_prefix . "stats";


## Improve best assembly
my $current_scaffold = $qst->getInput();
my $last_job = $qst->getWaitCondition();

# Make output directories
my $output_dir = $qst->getOutput();
my $scf_dir = $output_dir . "/scaffolds";
my $dg_dir = $output_dir . "/degap";
mkdir $scf_dir if $opt{scaffold};
mkdir $dg_dir if $opt{degap};

for(my $i = 1; $i <= $opt{iterations}; $i++) {

	my $scf_job_name = $scf_job_prefix . $i;
	my $dg_job_name = $dg_job_prefix . $i;

	# Run scaffolding step

	my $scf_dir_i = $scf_dir . "/" . $i;
	mkdir $scf_dir_i;

	my $scf_wait_arg = $last_job ? ("--wait_condition ended(" . $last_job . ")") : "";
	my $scf_job_arg = "--job_name " . $scf_job_name;
	my $scf_out_arg = "--output " . $scf_dir_i;
	my $scf_in_arg = "--input " . $current_scaffold;
	my $scf_args = $opt{scaffolder_args} ? $opt{scaffolder_args} : "";

	my $scf_cmd_line = $SCAFFOLDER_PATH . " " . $static_args . " " . $scf_wait_arg . " " . $scf_job_arg . " " . $scf_args . " " . $scf_out_arg . " " . $scf_in_arg;

	system($scf_cmd_line) unless $opt{simulate};

	$current_scaffold = $scf_dir_i . "/scaffolder.final.scaffolds.fasta";
	$last_job = $scf_job_name;



	# Run gap closing step

	my $dg_dir_i = $dg_dir . "/" . $i;
	mkdir $dg_dir_i;

	my $dg_wait_arg = $last_job ? "--wait_condition done(" . $last_job . ")" : "";
	my $dg_job_arg = "--job_name " . $dg_job_name;
	my $dg_out_arg = "--output " . $dg_dir_i;
	my $dg_in_arg = "--input " . $current_scaffold;
	my $dg_args = $opt{degap_args} ? $opt{degap_args} : "";

	my $dg_cmd_line = $DEGAPPER_PATH . " " . $static_args . " " . $dg_wait_arg . " " . $dg_job_arg . " " . $dg_out_arg . " " . $dg_in_arg . " " . $dg_args;

	system($dg_cmd_line) unless $opt{simulate};

	$current_scaffold = $dg_dir_i . "/gc-scaffolds.fa";
	$last_job = $dg_job_name;
}




## Remove contigs under a user specified length

if ($opt{clip}) {

	my $clip_dir = $opt{output} . "/clipped";
	mkdir $clip_dir;

	my $clip_in_arg = "--input " . $current_scaffold;
	my $clip_out_arg = "--output " . $clip_dir . "/clipped-scaffolds.fa";

	my $clip_job_arg = "--job_name " . $clip_job_name;
	my $clip_wait_arg = "--wait_condition ended(" . $last_job . ")";
	my $clip_args = $opt{clip_args} ? $opt{clip_args} : "";

	my $clip_cmd_line = $CLIPPER_PATH . " " . $static_args . " " . $clip_job_arg . " " . $clip_wait_arg . " " . $clip_args . " " . $clip_in_arg . " " . $clip_out_arg;

	system($clip_cmd_line) unless $opt{simulate};

	$current_scaffold = $clip_out_arg;
	$last_job = $clip_job_name;
}


## Remove PhiX??? (This step shouldn't be required in a normal run as the data should be screened already)




## Remove duplicates???




## Generate final stats (maybe!!)

# Will need to make soft links to all scaffold files in same directory and then use stats_gatherer on this.
if ($opt{stats}) {
if (0) {
	my $stats_dir = $opt{output} . "/stats";
	mkdir $stats_dir;

	for(my $i=1; $i<=$opt{iterations}; $i++) {
		# Link to each scaffold file from scaffolding and gap closing
	}

	my $stats_path = $RAMPART_DIR . "/assembly_stats_gatherer.pl";
	my $stats_job_arg = "-J" . $stats_job_name;
	my $stats_wait_arg = "-wdone(" . $last_job . ")";
	my $stats_file = "stats.txt";
	my $stats_cmd_line = $stats_path . " " . $stats_dir . " > " . $stats_file;
	my $plotter_path = $RAMPART_DIR . "/assembly_stats_plotter.pl";
	my $plotter_cmd_line = "R CMD BATCH '--args " . $stats_file . "' " . $plotter_path . " " . $stats_file . "rout";
	my $cmd_line = $stats_cmd_line . "; " . $plotter_cmd_line;

#	system($SUBMIT, $opt{extra_queue_args}, $queue_project_arg, $stats_job_arg, $stats_wait_arg, $cmd_line) unless $opt{simulate};
}}




__END__

=pod

=head1 NAME

  improver.pl


=head1 SYNOPSIS

  improver.pl [options] <input_files>

  For full documentation type: "improver.pl --man"


=head1 DESCRIPTION

  Runs an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.


=head1 OPTIONS

  scaffold|s               Whether to scaffold the input contigs.
  scaffold_args|s_args     Additional arguments to the scaffolding tool.
  degap|d                  Whether to degap the input contigs.
  degap_args|dg_args       Additional arguments to the degapping tool.
  job_prefix|job|j         The prefix string for each job.
  project|p                The project name for marking the LSF jobs.
  extra_queue_args|eqa|q   Extra arguments to pass to the queueing system for each assembly job.
  min_len|min              Removes scaffolds shorter than this from the final assembly.
  iterations|i             The number of scaffolding and degapping iterations to run.
  simulate                 Runs the script without sending any jobs to the queue.
  stats                    Outputs statistics and plots for all scaffold files produced in the improvement process.
  output|out|o=s           The output directory.
  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


