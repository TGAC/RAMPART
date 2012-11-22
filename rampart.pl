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
use Cwd 'abs_path';
use File::Basename;

# RAMPART modules
use QsOptions;
use Configuration;
use SubmitJob;


#### Constants

# Project constants
my $JOB_PREFIX = $ENV{'USER'} . "-rampart-";


# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));

# Assembly stats gathering constants
my $QT_PATH = $RAMPART_DIR . "qt.pl";
my $MASS_PATH = $RAMPART_DIR . "mass.pl";
my $MASS_SELECTOR_PATH = $RAMPART_DIR . "mass_selector.pl";
my $GETBEST_PATH = $RAMPART_DIR . "get_best.pl";
my $IMPROVER_PATH = $RAMPART_DIR . "improver.pl";
my $HELPER_PATH = $RAMPART_DIR . "tools/RampartHelper/target/RampartHelper-0.1-jar-with-dependencies.jar";

my $SOURCE_JAVA = "source jre-6.0.25;";
my $SOURCE_LATEX = "source texlive-2012";


# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Gather Command Line options and set defaults
my (%opt) = (	"qt",				1,
				"mass", 			1,
				"improver",			1,
				"mass_selector", 	1,
				"persist",			1,
				"report",			1 );
	

GetOptions (
	\%opt,
	'qt!',
	'mass!',
	'mass_args|ma=s',
	'mass_selector!',
	'improver!',
	'improver_args|ia=s',
	'persist!',
	'report!',
	'config|cfg=s',
	'simulate|sim',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




#### Validation

die "Error: No rampart library config file specified\n\n" unless $opt{config};
#die "Error: Approximate genome size not specified\n\n" unless $opt{approx_genome_size};


# Interpret config files
my $raw_cfg;
my $qt_cfg;



#### Process (all steps to be controlled via cmd line options)
my $qt_job_prefix = $qst->getJobName() . "-qt";
my $mass_job_prefix = $qst->getJobName() . "-mass";
my $ms_job_name = $qst->getJobName() . "-ms";
my $get_best_job_name = $mass_job_prefix . "-getbest";
my $improver_job_prefix = $qst->getJobName() . "-improver";
my $helper_job_name = $qst->getJobName() . "-helper";


# Set locations of read library files and directories
my $reads_dir = $qst->getOutput() . "/reads";
my $raw_config_file = $reads_dir . "/raw.cfg";
my $qt_config_file = $reads_dir . "/qt.cfg";

# Set locations of mass files and directories
my $mass_dir = $qst->getOutput() . "/mass";
my $mass_raw_dir = $mass_dir . "/raw";
my $mass_qt_dir = $mass_dir . "/qt";
my $mass_stats_dir = $mass_dir . "/stats";
my $mass_best_dir = $mass_dir . "/best";
my $raw_stats_file = $mass_raw_dir . "/stats.txt";
my $qt_stats_file = $mass_qt_dir . "/stats.txt";
my $best_path_file = $mass_stats_dir . "/best.path.txt";
my $best_dataset_file = $mass_stats_dir . "/best.dataset.txt";
my $best_assembly_sl_path = $mass_best_dir . "/best_assembly.fa";
my $best_config_sl_path = $mass_best_dir . "/best.cfg";
	

# TODO - wrap all stages in subs and avoid global vars where possible.


# Optionally do quality trimming on the input data
if ($opt{qt}) {

	mkdir $reads_dir;
	
	system("cp " . $opt{config} . " " . $raw_config_file );
	system("cp " . $opt{config} . " " . $qt_config_file );
	
	$raw_cfg = new Configuration( $raw_config_file );
	$qt_cfg = new Configuration( $qt_config_file );
	
	for(my $i = 1; $i < $raw_cfg->getNbSections(); $i++) {
		
		# Get info for this section
		my $cfg_sect = $raw_cfg->getSectionAt($i);
		my $sect_name = $raw_cfg->getSectionNameAt($i);
		
		# Get the file paths pointed to by the original config file.
		my $file1 = $cfg_sect->{file_paired_1};
		my $file2 = $cfg_sect->{file_paired_2};
		
		# Configure file paths for reads dir
		my $read_file_prefix = $reads_dir . "/" . $cfg_sect->{name};
		my $in_file1 = $read_file_prefix . "_1.fastq";
		my $in_file2 = $read_file_prefix . "_2.fastq";
		my $out_file1 = $read_file_prefix . "_1.qt.fastq";
		my $out_file2 = $read_file_prefix . "_2.qt.fastq";
		my $sout_file = $read_file_prefix . "_se.qt.fastq";
				
		# Link the original files to the reads dir
		system("ln -s -f " . $file1 . " " . $in_file1);
		system("ln -s -f " . $file2 . " " . $in_file2);
		
		
		my $qt_job_name = $qt_job_prefix . "-" . $i;
		
		my @qt_args = grep {$_} (
			$QT_PATH,score_tab
			$qst->getGridEngineAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $qt_job_name,
			$qst->getQueueAsParam(),
			$qst->getExtraArgs(),
			$qst->isVerboseAsParam(),
			"--in1 " . $in_file1,
			"--in2 " . $in_file2,
			"--out1 " . $out_file1,
			"--out2 " . $out_file2,
			"--sout " . $sout_file
		);
				
		my $qt_cmd_line = join " ", @qt_args;
	
		system($qt_cmd_line);
		
		# Also we must change the new configuration files for consisitency
		$raw_cfg->getSectionAt($i)->{file_paired_1} = $in_file1;
		$raw_cfg->getSectionAt($i)->{file_paired_2} = $in_file2;
		$qt_cfg->getSectionAt($i)->{file_paired_1} = $out_file1;
		$qt_cfg->getSectionAt($i)->{file_paired_2} = $out_file2;
		$qt_cfg->getRawStructure()->newval($sect_name, "file_se", $sout_file );
	}
	
	# Save the new configuration files
	$raw_cfg->save( $raw_config_file );
	$qt_cfg->save( $qt_config_file );
}


## Run assemblies for both raw and qt datasets
if ($opt{mass}) {

	# Create directories to hold the assemblies.
	mkdir $mass_dir;	
	mkdir $mass_raw_dir;	
	mkdir $mass_qt_dir;

	# Set job prefixes
	my $raw_mass_job_prefix = $mass_job_prefix . "-raw";
	my $qt_mass_job_prefix = $mass_job_prefix . "-qt";
	
	# If we have run qt, then make sure those jobs have finished before starting the assemblies.
	my $qt_wait_job = $qt_job_prefix . "*" if $opt{qt};

	# Run the assembler script for each dataset
	run_mass($raw_config_file, $raw_mass_job_prefix, $mass_raw_dir, $qt_wait_job);
	run_mass($qt_config_file, $qt_mass_job_prefix, $mass_qt_dir, $qt_wait_job);
}

## Run mass selector to find the best assembly
if ($opt{mass_selector}) {
	
	mkdir $mass_stats_dir;
	
	# Build the command line args.
	# If the MASS step was run previously make sure this step doesn't run until after all MASS jobs have finished. 
	my @ms_args = grep {$_} (
			$MASS_SELECTOR_PATH,
			$qst->getGridEngineAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $ms_job_name,
			$opt{mass} ? "--wait_condition 'ended(" . $mass_job_prefix . "*)'" : "",
			$qst->getQueueAsParam(),
			$qst->getExtraArgs(),
			"--output " . $mass_stats_dir,
			$qst->isVerboseAsParam(),
			"--raw_stats_file " . $raw_stats_file,
			"--qt_stats_file " . $qt_stats_file,
			$opt{approx_genome_size} ? "--approx_genome_size " . $opt{approx_genome_size} : "" );
			
	my $ms_cmd_line = join " ", @ms_args;

	system($ms_cmd_line);
	
	# Put the best assembly and config in a known location
	mkdir $mass_best_dir;	
	
	my @gb_args = grep {$_} (
		$GETBEST_PATH,
		"--best_assembly_in " . $best_path_file,
		"--best_dataset_in " . $best_dataset_file,
		"--raw_config " . $raw_config_file,
		"--qt_config " . $qt_config_file,
		"--best_assembly_out " . $best_assembly_sl_path,
		"--best_config_out " . $best_config_sl_path,
		$qst->isVerboseAsParam()
	);
	
	my $get_best_job = new QsOptions();
	$get_best_job->setGridEngine($qst->getGridEngine());
	$get_best_job->setProjectName($qst->getProjectName());
	$get_best_job->setJobName($get_best_job_name);
	$get_best_job->setWaitCondition("ended(" . $ms_job_name . ")");
	$get_best_job->setVerbose($qst->isVerbose());
	SubmitJob::submit($get_best_job, join " ", @gb_args);
}

## Improve best assembly
if ($opt{improver}) {
	
	# Make a directory for all improver results
	my $imp_dir = $qst->getOutput() . "/improver";
	mkdir $imp_dir;
	
	# Change into that dir
	chdir $imp_dir;


	# Run improver on best assembly using best config
	# Wait for mass selector to finish before starting if mass selector is running.
	my @imp_args = grep {$_} (
			$IMPROVER_PATH,
			$qst->getGridEngineAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $improver_job_prefix,
			$opt{mass_selector} ? "--wait_condition 'done(" . $get_best_job_name . ")'" : "",
			$qst->getQueueAsParam(),
			$qst->getExtraArgs(),
			"--output " . $imp_dir,
			"--input " . $best_assembly_sl_path,
			"--config " . $best_config_sl_path,
			"--stats",
			$opt{simulate} ? "--simulate" : "",
			$opt{improver_args},
			$qst->isVerboseAsParam());
	
	my $imp_job = new QsOptions();
	$imp_job->setGridEngine($qst->getGridEngine());
	$imp_job->setProjectName($qst->getProjectName());
	$imp_job->setJobName($improver_job_prefix);
	$imp_job->setWaitCondition("ended(" . $get_best_job_name . ")") if $opt{mass_selector};
	SubmitJob::submit($imp_job, join " ", @imp_args);

	chdir $PWD;
}

if ($opt{report} || $opt{persist}) {
	
	my @helper_args = grep {$_} (
		$SOURCE_JAVA,
		$SOURCE_LATEX,
		"java -jar " . $HELPER_PATH,
		"--job_dir " . $qst->getOutput(),
		"--project_dir " . $RAMPART_DIR,
		$opt{report} ? "--report" : undef,
		$opt{persist} ? "--persist" : undef,
		$qst->isVerboseAsParam()
	);
	
	my $helper_job = new QsOptions();
	$helper_job->setGridEngine($qst->getGridEngine());
	$helper_job->setProjectName($qst->getProjectName());
	$helper_job->setJobName($helper_job_name);
	
	if ($opt{improver}) {
		$helper_job->setWaitCondition("ended(" . $improver_job_prefix . "*)");
	}
	elsif ($opt{mass_selector}) {
		$helper_job->setWaitCondition("ended(" . $get_best_job_name . ")");
	}
	
	SubmitJob::submit($helper_job, join " ", @helper_args);
}

# Notify user of job submission
if (1){#$qst->isVerbose()) {
	print 	"\n" .
			"RAMPART has successfully submitted all child jobs to the grid engine.  " .
			"You will be notified by email when the jobs have completed.  " .
			"In case you need to kill all grid engine jobs produced by this run the job prefix is: " . $qst->getJobName() . ".  " .
			"So, for example, if you are using LSF type \"bkill -J " . $qst->getJobName() . "*\" to kill all jobs from this run.\n";
}


sub run_mass {

	my $mass_config = shift;
	my $mass_job_name = shift;
	my $mass_output_dir = shift;
	my $qt_job_name = shift;
	
	my @mass_args = grep {$_} (	
		$MASS_PATH,
		$qst->getGridEngineAsParam(),
		$qst->getProjectNameAsParam(),
		"--job_name " . $mass_job_name,
		$qst->getQueueAsParam(),
		$qt_job_name ? "--wait_condition 'done(" . $qt_job_name . ")'" : "",
		$qst->getExtraArgs(),
		"--output " . $mass_output_dir,
		$qst->isVerboseAsParam(),
		$opt{mass_args},
		"--stats",
		$opt{simulate} ? "--simulate" : "",
		"--config " . $mass_config
	);

	system(join " ", @mass_args);
}


__END__

=pod

=head1 NAME

  rampart.pl


=head1 SYNOPSIS

  rampart.pl [options] --raw_config <raw_config_file> --qt_config <qt_config_file>

  For full documentation type: "rampart.pl --man"


=head1 DESCRIPTION

  This script is designed to run mass on raw and quality trimmed datasets and gather the resulting statistics.  
  It then selects the best assembly and then attempts to improve that assembly by doing additional scaffolding, gap closing and cleaning.


=head1 OPTIONS

  --qt
              Whether or not to run the quality trimming step.  Use --noqt to disable.  Default: on.
  
  --mass
	          Whether or not to do the MASS step.  Use --nomass to disable.  Default: on.
	          
  --mass_args            --ma
              Any additional arguments to pass to the MASS tool (e.g. --kmin and --kmax).
	
  --mass_selector
              Whether to attempt to select the best assembly from a set of assemblies already created by rampart.  Use --nomass_selector to disable.  Default: on.
	
  --improver
              Whether or not to run the assembly improver step.  Use --noimprover to disable.  Default: on.
	
  --improver_args        --ia
              Any additional arguments to pass to the improver tool (e.g. --iterations)
	
  --config               --cfg
              REQUIRED: The path to the rampart library configuration file.
	
  --simulate             --sim
              If set then the script is run but no mass or improver jobs are submitted to the grid engine. Default: off.
	
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

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --output               --out               -o
              The output file/dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut



