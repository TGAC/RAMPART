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


# Gather Command Line options and set defaults
my (%opt) = (	"assembler_path",	$DEF_ASSEMBLER_PATH,
		"scaffolder_path",	$DEF_SCAFFOLDER_PATH,
		"degap_path",		$DEF_DG_PATH,
		"output",		$PWD );

GetOptions (
	\%opt,
	'assembler|a=s',
	'assembler_path|ap=s',
	'scaffolder|s=s',
	'scaffolder_path|sp=s',
	'degap|dg=s',
	'degap_path|dgp=s',
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




#### Validation

die "Error: No output directory specified\n\n" unless $opt{output};me, $dir) = fileparse(abs_path($0));
die "Error: No raw library config file specified\n\n" unless $opt{raw_config};
die "Error: No quality trimmed library config file specified\n\n" unless $opt{qt_config};


# Interpret config files


#### Process (all steps to be controlled via cmd line options)
my $project_arg = "-P " . $opt{project};
my $ass_job_prefix = $JOB_PREFIX . $opt{assembler} . "-";
my $sg_job_prefix = $JOB_PREFIX . "stat_gatherer-";
my $scf_job_name = $JOB_PREFIX . $opt{scaffolder};
my $dg_job_name = $JOB_PREFIX . $opt{degap};
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

	my $raw_input = 1;	# Need to gather from config file
	my $qt_input = 1;	# Need to gather from config file
	my $raw_ass_job_prefix = $ass_job_prefix . "raw-";
	my $qt_ass_job_prefix = $ass_job_prefix . "qt-";

	# Run the assembler script for each dataset
	run_assembler($raw_input, $raw_ass_job_prefix, $raw_ass_dir);
	run_assembler($qt_input, $qt_ass_job_prefix, $qt_ass_dir);



	# Generate stats for each assembly
	my $raw_sg_job_name = $sg_job_prefix . "raw";
	my $qt_sg_job_name = $sq_job_prefix . "qt";

	run_stater($raw_ass_dir, $raw_sg_job_name);
	run_stater($qt_ass_dir, $qt_sg_job_name);

	# Run decider


}



## Improve best assembly

# Run scaffolding step
if ($opt{scaffolder}) {

	my $scf_dir = $opt{output} . "/scaffolds";
	mkdir $scf_dir;

	my $scf_path = $RAMPART_DIR . "/scaffolder.pl";
	my $scf_wait_arg = $opt{assembler} ? "--wait_job " . $ass_job_prefix . "*" : "";
	my $scf_job_arg = "--job_name " . $scf_job_name;
	my $scf_out_arg = "--output " . $scf_dir;
	my $scf_in_arg = "--input " . $best_ass; # Need to get the actual file here from either cmd option or assembly output
	my $scf_config_arg = "--config " . $scf_cfg_path; # Somehow need to have this available.

	my $scf_args = $scf_wait_arg . " " $scf_job_name . " " . $project_arg . " " . $scf_out_arg;

	system($scf_path, $scf_args, $scf_in_arg, $scf_config_arg);
}


# Run gap closing step
if ($opt{degap}) {

	my $dg_dir = $opt{output} . "/degapped";
	mkdir $dg_dir;

	my $dg_path = $RAMPART_DIR . "/degap.pl";
	my $dg_wait_arg = $opt{scaffolder} ? "--wait_job " . $scf_job_name : "";
	my $dg_job_arg = "--job_name " . $dg_job_name;
	my $dg_out_arg = "--output " . $dg_dir;
	my $dg_in_arg = "--input " . $scf_out; # Need to either get the input scaffolds from the user or the scaffolder tool
	my $dg_config_arg = "--config " . $scf_cfg_path; # Somehow need to make this available.

	my $dg_args = $dg_wait_arg . " " . $dg_job_name . " " . $project_arg . " " . $dg_out_arg;

	system($dg_path, $dg_args, $dg_in_arg, $dg_config_arg);
}



## Generate final stats (maybe!!)



## Remove contigs under 1KB



## Remove PhiX???






sub run_stater {

	my $stats_gatherer_path = $RAMPART_DIR . "/assembly_stats_gatherer.pl";
	my $sg_wait_arg = "-w \"done(" . $ass_job_prefix . "*)\"";
	my $sg_job_name = "-J " . $_[1];
	my $sg_cmd_line = $stats_gatherer_path . " " . $_[0];

	system($SUBMIT, $sg_job_name, $sg_wait_arg, $sg_cmd_line);
}



sub run_assembler {

	my $assembler_path = $RAMPART_DIR . "/assembler.pl";
	my $assembler_arg = "--assembler " . $opt{assembler};
	my $job_prefix_arg = "--job_prefix " . $_[1];
	my $kmin_arg = "--kmin " . $opt{kmin} if $opt{kmin};
	my $kmax_arg = "--kmax " . $opt{kmax} if $opt{kmax};
	my $out_dir = "--output " . $_[2];

	my $assembly_args = $job_prefix_arg . " " . $project_arg . " " . $assembler_arg . " " . $kmin_arg . " " $kmax_arg . " " . $out_dir;

	system($assembler_path, $assembly_args, $_[0]);
}


