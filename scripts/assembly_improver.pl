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
my $DEF_PROJECT_NAME = "AssemblyImprover_" . $NOW;
my $JOB_PREFIX = $ENV{'USER'} . "-ai-";


# Tool names
my $DEF_SCAFFOLDER = "sspace";
my $DEF_DEGAP = "gapcloser";
my $CLIPPER_BIN = "fastx_clipper";
my $DEDUP_BIN = "";


# Queueing system constants
my $SUBMIT = "bsub";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));


# Gather Command Line options and set defaults
my (%opt) = (	"output",		$PWD );

GetOptions (
	\%opt,
	'scaffolder|s=s',
	'degap|dg=s',
	'clip|c',
	'project|p=s',
	'wait_job|wj|w=s',
	'config|cfg|c=s',
	'output|out|o=s',
	'verbose|v',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};



# Get input files
my @in_files = @ARGV;
my $input_file = join " ", @in_files;



#### Validation

die "Error: No input files specified\n\n" unless @in_files;
die "Error: Was only expecting a single file to process\n\n" unless @in_files == 1;
die "Error: No output directory specified\n\n" unless $opt{output};me, $dir) = fileparse(abs_path($0));
die "Error: No library config file specified\n\n" unless $opt{raw_config};


# Interpret config files






#### Process (all steps to be controlled via cmd line options)
my $queue_project_arg = "-P " . $opt{project};
my $script_project_arg = "--project " . $opt{project};
my $sg_job_prefix = $JOB_PREFIX . "stat_gatherer-";
my $scf_job_name = $JOB_PREFIX . $opt{scaffolder};
my $dg_job_name = $JOB_PREFIX . $opt{degap};
my $fxc_job_name = $JOB_PREFIX . $opt
my $best_ass;




## Improve best assembly

# Run scaffolding step
if ($opt{scaffolder}) {


	my $scf_dir = $opt{output} . "/scaffolds";
	mkdir $scf_dir;

	my $scf_path = $RAMPART_DIR . "/scaffolder.pl";
	my $scf_wait_arg = $opt{wait_job} ? "--wait_job " . $opt{wait_job} : "";
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






## Remove contigs under 1KB (use fastx_clipper)

if ($opt{clip}) {
	my $fxc_path = "fastx_clipper";
	my $fxc_in_arg = "-i ";
	my $fxc_out_arg = "-o ";
	my $min_seq_len_arg = "-l 1000";

	my $fxc_cmd_line = $fxc_path . " " . $min_seq_len_arg . " " . $fxc_in_arg . " " . $fxc_out_arg;
	my $fxc_wait_arg = "-w \"done(" . . ")\"";
	my $

	system($SUBMIT, $fxc_cmd_line);
}


## Remove PhiX??? (This step shouldn't be required in a normal run as the data should be screened already)




## Remove duplicates???




## Generate final stats (maybe!!)






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


