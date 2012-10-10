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


# Tool paths
$DEF_ASSEMBLER_PATH = "abyss-pe";
$DEF_SCAFFOLDER_PATH = "sspace";
$DEF_DEGAP_PATH = "GapCloser";


# Queueing system constants
my $SUBMIT = "bsub";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));

# Assembly stats gathering constants
my $SELECT_BEST_ASSEMBLY_PATH = $RAMPART_DIR . "/select_best_assembly.pl";


# Gather Command Line options and set defaults
my (%opt) = (	"assembler_path",	$DEF_ASSEMBLER_PATH,
		"scaffolder_path",	$DEF_SCAFFOLDER_PATH,
		"degap_path",		$DEF_DG_PATH,
		"output",		$PWD );

GetOptions (
	\%opt,
	'assembler|a=s',
	'extra_assembler_args|ea_args|eaa=s',
	'approx_genome_size|ags=i',
	'improver|i',
	'extra_improver_args|ei_args|eia=s',
	'project|p=s',
	'job_prefix|job|j=s',
	'extra_queue_args|eqa=s',
	'raw_config|rc=s',
	'qt_config|qtc=s',
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

die "Error: No output directory specified\n\n" unless $opt{output};me, $dir) = fileparse(abs_path($0));
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
if ($opt{assembler}) {

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
	run_assembler($raw_input, $raw_ass_job_prefix, $raw_ass_dir);
	run_assembler($qt_input, $qt_ass_job_prefix, $qt_ass_dir);


	# Run best assembly selector to find "best" assembly (assembler will produce stats automatically for us to use here)
	my $raw_stats_file = $raw_ass_dir . "/stats.txt";
	my $qt_stats_file = $qt_ass_dir . "/stats.txt";
	my $asd_wait_arg = "-w 'done(" . $ass_job_prefix . "*)'";
	my $asd_job_arg = "-J" . $asd_job_name;

	my $raw_stats_arg = "--raw_stats_file " . $raw_stats_file;
	my $qt_stats_arg = "--qt_stats_file " . $qt_stats_file;
	my $gen_size_arg = "--approx_genome_size " . $opt{approx_genome_size};
	my $best_ass_file = "--output " . $ass_dir . "/best.path.txt";

	my $asd_cmd_line = $SELECT_BEST_ASSEMBLY_PATH .  . $raw_stats_arg . " " . $qt_stats_arg . " " . $gen_size_arg . " " . $best_ass_file;

	system($SUBMIT, $qs_project_arg, $asd_job_arg, $asd_wait_arg, $opt{extra_queue_args}, $asd_cmd_line;

	# Extract best assembly from file
	# This bit isn't going to work yet as we need to do this after the previous job has completed
	#open(BA_FILE, $best_ass_file) or die "Can't read " . $best_ass_file . "\n";
	#$best_ass = <BA_FILE>;
	#close (BA_FILE);
}



## Improve best assembly

if ($opt{improve} && $best_ass) {


	$improver_cmd = $improver_path . " " . $improver_args . " " . $best_assembly;

	system($SUBMIT, $qs_project_arg, $imp_wait_arg, $improver_cmd);
}





sub run_assembler {

	my $assembler_path = $RAMPART_DIR . "/assembler.pl";
	my $assembler_arg = "--assembler " . $opt{assembler};
	my $job_prefix_arg = "--job_prefix " . $_[1];
	my $out_dir = "--output " . $_[2];

	my $assembly_args = $job_prefix_arg . " " . $project_arg . " " . $assembler_arg . " " . $opt{extra_assembler_args} . " --stats " . $out_dir;

	system($assembler_path, $assembly_args, $_[0]);
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


