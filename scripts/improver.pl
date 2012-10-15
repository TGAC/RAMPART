#!/usr/bin/perl

use strict;

#### Packages
use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';


#### Constants

# Now
my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;


# Project constants
my $DEF_PROJECT_NAME = "AssemblyImprover_" . $NOW;
my $JOB_PREFIX = $ENV{'USER'} . "-ai-" . $NOW . "-";


# Tool names
my $DEF_SCAFFOLDER = "sspace";
my $DEF_DEGAP = "gapcloser";
my $CLIPPER_BIN = "fastx_clipper";
my $DEDUP_BIN = "";


# Scaffold Improver constants
my $DEF_ITERATIONS = 3;

# Clipping constants
my $DEF_MIN_LEN = 1000;


# Queueing system constants
my $SUBMIT = "bsub";
my $FASTX_SOURCE_CMD = "source fastx_toolkit-0.0.13;";
my $DEF_QUEUE_ARGS = "-q production";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));


# Gather Command Line options and set defaults
my (%opt) = (	"output",		$PWD,
		"min_len",		$DEF_MIN_LEN,
		"extra_queue_args",	$DEF_QUEUE_ARGS );

GetOptions (
	\%opt,
	'scaffold|s',
	'scaffold_args|s_args=s',
	'degap|dg',
	'degap_args|dg_args=s',
	'clip|c',
	'project|p=s',
	'extra_queue_args|eqa=s',
	'wait_job|wj|w=s',
	's_config|scfg=s',
	'g_config|gcfg=s',
	'min_len|min|m=i',
	'iterations|i=i',
	'stats',
	'simulate|sim',
	'input|in=s',
	'output|out|o=s',
	'verbose|v',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




#### Validation

die "Error: No input files specified\n\n" unless $opt{input};
die "Error: No output directory specified\n\n" unless $opt{output};
die "Error: No scaffolder config file specified\n\n" unless $opt{s_config};
die "Error: No degapper config file specified\n\n" unless $opt{g_config};


# Interpret config files






#### Process (all steps to be controlled via cmd line options)
my $queue_project_arg = "-P " . $opt{project};
my $script_project_arg = "--project " . $opt{project};
my $sg_job_prefix = $JOB_PREFIX . "stat_gatherer-";
my $scf_job_prefix = $JOB_PREFIX . "scaffold-";
my $dg_job_prefix = $JOB_PREFIX . "degap-";
my $clip_job_name = $JOB_PREFIX . "clip";
my $stats_job_name = $JOB_PREFIX . "stats";
my $queue_args = "--extra_queue_args \"" . $opt{extra_queue_args} . "\"";
my $verbose_arg = "--verbose" if $opt{verbose};



## Improve best assembly

my $current_scaffold = $opt{input};
my $last_job = $opt{wait_job};
my $scf_dir = $opt{output} . "/scaffolds";
my $dg_dir = $opt{output} . "/degap";

mkdir $scf_dir if $opt{scaffold};
mkdir $dg_dir if $opt{degap};

for(my $i = 1; $i <= $opt{iterations}; $i++) {

	my $scf_job_name = $scf_job_prefix . $i;
	my $dg_job_name = $dg_job_prefix . $i;

	# Run scaffolding step
	if ($opt{scaffold}) {

		my $scf_dir_i = $scf_dir . "/" . $i;
		mkdir $scf_dir_i;

		my $scf_path = $RAMPART_DIR . "scaffolder.pl";
		my $scf_wait_arg = $last_job ? ("--wait_job " . $last_job) : "";
		my $scf_job_arg = "--job_name " . $scf_job_name;
		my $scf_out_arg = "--output " . $scf_dir_i;
		my $scf_in_arg = "--input " . $current_scaffold;
		my $scf_config_arg = "--config " . $opt{s_config};

		my $scf_args = $scf_path . " " . $verbose_arg . " " . $scf_wait_arg . " " . $scf_job_arg . " " . $script_project_arg . " " . $queue_args . " " . $scf_out_arg . " " . $scf_in_arg . " " . $scf_config_arg;

		print "\nScaffolding args: " . $scf_args . "\n" if $opt{verbose};

#		system($scf_path, $scf_wait_arg, $scf_job_arg, $script_project_arg, $queue_args, $scf_out_arg, $scf_in_arg, $scf_config_arg);
		system($scf_args) unless $opt{simulate};

		$current_scaffold = $scf_dir_i . "/scaffolder.final.scaffolds.fasta";
		$last_job = $scf_job_name;
	}



	# Run gap closing step
	if ($opt{degap}) {

		my $dg_dir_i = $dg_dir . "/" . $i;
		mkdir $dg_dir_i;

		my $dg_path = $RAMPART_DIR . "degap.pl";
		my $dg_wait_arg = $last_job ? "--wait_job " . $last_job : "";
		my $dg_job_arg = "--job_name " . $dg_job_name;
		my $dg_out_arg = "--output " . $dg_dir_i;
		my $dg_in_arg = "--input " . $current_scaffold;
		my $dg_config_arg = "--config " . $opt{g_config};

		my $dg_args = $dg_path . " " . $verbose_arg . " " . $dg_wait_arg . " " . $dg_job_arg . " " . $script_project_arg . " " . $queue_args . " " . $dg_out_arg . " " . $dg_in_arg . " " . $dg_config_arg;

		print "\nDegapping args: " . $dg_args . "\n" if $opt{verbose};

		system($dg_args) unless $opt{simulate};

		$current_scaffold = $dg_dir_i . "/gc-scaffolds.fa";
		$last_job = $dg_job_name;
	}
}




## Remove contigs under 1KB (use fastx_clipper)

if ($opt{clip}) {

	my $clip_dir = $opt{output} . "/clipped";
	mkdir $clip_dir;

	my $clip_path = "~droun/bin/length_extract_fasta";
	my $clip_in_arg = $current_scaffold;
	my $clip_out_arg = $clip_dir . "/scaffolds_over_" . $opt{min_len} . ".fa";
	my $min_seq_len_arg = $opt{min_len};

	my $clip_cmd_line = $clip_path . " " . $min_seq_len_arg . " " . $clip_in_arg . " > " . $clip_out_arg;
	my $clip_wait_arg = "-wended(" . $last_job . ")";
	my $clip_job_arg = "-J" . $clip_job_name;

	system($SUBMIT, $opt{extra_queue_args}, $queue_project_arg, $clip_job_arg, $clip_wait_arg, $clip_cmd_line) unless $opt{simulate};

	$current_scaffold = $clip_out_arg;
	$last_job = $clip_job_name;
}


## Remove PhiX??? (This step shouldn't be required in a normal run as the data should be screened already)




## Remove duplicates???




## Generate final stats (maybe!!)

# Will need to make soft links to all scaffold files in same directory and then use stats_gatherer on this.
if ($opt{stats}) {

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

	system($SUBMIT, $opt{extra_queue_args}, $queue_project_arg, $stats_job_arg, $stats_wait_arg, $cmd_line) unless $opt{simulate};
}




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


