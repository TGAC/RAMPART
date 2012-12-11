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

use QsOptions;
use Configuration;
use SubmitJob;


#### Constants

# Script locations
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $SCAFFOLDER_PATH = $RAMPART_DIR . "scaffolder.pl";
my $DEGAPPER_PATH   = $RAMPART_DIR . "degap.pl";
my $CLIPPER_PATH    = $RAMPART_DIR . "clipper.pl";
my $MASS_GP_PATH    = $RAMPART_DIR . "mass_gp.pl";
my $DEDUP_PATH    	= $RAMPART_DIR . "dedup.pl";

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setMemory(30);
$qst->setThreads(8);
$qst->parseOptions();

# Gather Command Line options and set defaults
my (%opt) = ();

GetOptions(
	\%opt,
	'scaffolder_args|s_args=s',
	'degap_args|dg_args=s',
	'clip_args=s',
	'config|cfg=s',
	'stats',
	'log',
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
    "--grid_engine NONE",
	$qst->getProjectNameAsParam(),
	$qst->getExtraArgsAsParam(),
	$qst->getQueueAsParam(),
	$qst->isVerboseAsParam() );

# Make all job name/prefix strings
my $job_prefix     = $qst->getJobName();

## Improve best assembly
my $first_wait = $qst->getWaitCondition() ? 1 : 0;

# Make output directories
my $assembly_dir = $qst->getOutput() . "/assemblies";
my $stages_dir = $qst->getOutput() . "/stages";
my $logs_dir = $qst->getOutput() . "/logs";
mkdir $assembly_dir;
mkdir $stages_dir;
mkdir $logs_dir;

# Want some code here to interpret improver pipeline from configuration file
my @enhancer_stages = load_stages($opt{config});

# Generate the execution command list
my $input_file = abs_path($qst->getInput());
my @commands = process_stages(\@enhancer_stages, $input_file);

# Generate final stats if requests 
stats($assembly_dir) if $opt{stats};

# Log if requested
log_settings() if $opt{log};

# Everything gets submitted as one big job.
my $job_command = join("; ", @commands);
SubmitJob::submit($qst, $job_command);


# Loads improver stage configuration from file and returns an array of stages to execute
sub load_stages {	
	my $cfg_file = shift;
	
	my $cfg = new Configuration($cfg_file);
	my $cfg_mass_sect = $cfg->getSectionByName("IMPROVER");
	
	my @enhancer_stages;
	my $i = 1;
	while($cfg_mass_sect->{$i}) {
		push @enhancer_stages, $cfg_mass_sect->{$i};
		$i++;
	}
	
	# Checks the requestes enhancement stages are valid
	validateStages(\@enhancer_stages);
	
	return @enhancer_stages;
}

# Builds up a command line containing all programs to execute
sub process_stages {
	my $ref_stages = shift;
	my $input_assembly = shift;
	
	my @stages = @{$ref_stages};
	
	my $current_assembly = $assembly_dir . "/0.fa";
	
	my @commands;
	
	# Create link from input file to assemblies dir
	push @commands, ("ln -s -f " . $input_assembly . " " . $current_assembly );
	
	
	my $i = 1;
	my $last_job;
	foreach(@stages) {
		my $stage = $_;
		my $assembly;
		
		my $stage_dir = $stages_dir . "/" . $i;
		mkdir $stage_dir;
		
		if ($stage eq "SCAFFOLD") {
			$assembly = scaffold(\@commands, $current_assembly, $stage_dir, $logs_dir, $i);
		}
		elsif ($stage eq "DEGAP") {
			$assembly = degap(\@commands, $current_assembly, $stage_dir, $logs_dir, $i);
		}
		elsif ($stage eq "DEDUP") {
			$assembly =	dedup(\@commands, $current_assembly, $stage_dir, $logs_dir, $i);
		}
		elsif ($stage eq "CLIP") {
			$assembly = clip(\@commands, $current_assembly, $stage_dir, $logs_dir, $i);
		}
		
		# Make link for output of this stage to the assemblies directory
		$current_assembly = $assembly_dir . "/" . $i . "-scaffolds.fa";
		push @commands, ("ln -s -f " . $assembly . " " . $current_assembly);
		$i++;			
	}
	
	push @commands, ("ln -s -f " . $current_assembly . " " . $qst->getOutput() . "/final.fa");
	
	return @commands;
}


sub scaffold {
	my ($ref_commands, $input_assembly, $stage_dir, $logs_dir, $i) = @_;
	
	my @scf_args = grep {$_} (
		$SCAFFOLDER_PATH,
		@static_args,
		"--config " . $opt{config},
		$opt{scaffolder_args} ? $opt{scaffolder_args} : "",
		$qst->getMemoryAsParam(),
		$qst->getThreadsAsParam(),
		"--output " . $stage_dir,
		"--input " . $input_assembly,
		">",
		$logs_dir . "/" . $i . ".log");

	push @$ref_commands, (join " ", @scf_args) unless $opt{simulate};
	
	my $current_scaffold = $stage_dir . "/scaffolder.final.scaffolds.fasta";
	
	return $current_scaffold;
}

sub degap {	
	my ($ref_commands, $input_assembly, $stage_dir, $logs_dir, $i) = @_;
	
	my @dg_args = grep {$_} (
		$DEGAPPER_PATH,
		@static_args,
		"--config " . $opt{config},
		"--output " . $stage_dir,
		"--input " . $input_assembly,
		$qst->getMemoryAsParam(),
		$qst->getThreadsAsParam(),
		$opt{degap_args} ? $opt{degap_args} : "",
		">",
		$logs_dir . "/" . $i . ".log" );

	push @$ref_commands, (join " ", @dg_args ) unless $opt{simulate};

	my $current_scaffold = $stage_dir . "/gc-scaffolds.fa";
	
	return $current_scaffold;
}

sub dedup {
	my ($ref_commands, $input_assembly, $stage_dir, $logs_dir, $i) = @_;
	
	my @dedup_args = grep {$_} (
		$DEDUP_PATH,
		@static_args,
		"--input " . $input_assembly,
		"--output " . $stage_dir,
		">",
		$logs_dir . "/" . $i . ".log" 
	);

	push @$ref_commands, (join " ", @dedup_args) unless $opt{simulate};
	
	my $current_scaffold = $stage_dir . "/cleaned.fasta";
	
	return $current_scaffold;
}

sub clip {
	my ($ref_commands, $input_assembly, $stage_dir, $logs_dir, $i) = @_;
	
	my @clip_args = grep {$_} (
		$CLIPPER_PATH,
		@static_args,
		$opt{clip_args} ? $opt{clip_args} : "",
		"--input " . $input_assembly,
		"--output " . $stage_dir,
		">",
		$logs_dir . "/" . $i . ".log" 
	);

	push @$ref_commands, (join " ", @clip_args) unless $opt{simulate};
	
	my $current_scaffold = $stage_dir . "/clipped-scaffolds.fa";
	
	return $current_scaffold;
}

sub stats {
	my $stats_dir = shift;

	my @mgp_args = grep {$_} (
		$MASS_GP_PATH,
		@static_args,
		$qst->isVerboseAsParam(),
		"--output " . $stats_dir,
		"--input " . $stats_dir,
		"--index"
	);

	push @commands, (join " ", @mgp_args);
}


sub log_settings {
	open (LOGFILE, ">", $qst->getOutput() . "/improver.log");
	print LOGFILE "[IMPROVER]\n";
	print LOGFILE "scaffolding.tool=" . "scfx" . "\n";
	print LOGFILE "scaffolding.version=" . "x.x" . "\n";
	print LOGFILE "scaffolding.memory=" . "0" . "\n";
	print LOGFILE "degap.tool=" . "degapx" . "\n";
	print LOGFILE "degap.version=" . "x.x" . "\n";
	print LOGFILE "degap.memory=" . "0" . "\n";
	print LOGFILE "clip.minlen=" . "1" . "\n";
	close(LOGFILE);
}


sub validateStages {
	my $ref_stages = shift;
	my @stages = @{$ref_stages};
	
	my $i = 1;
	foreach(@stages) {
		my $stage = $_;
		die "Stage " . $i . " is not valid: " . $stage . "; Valid stage names are: SCAFFOLD, DEGAP, DEDUP, CLIP" unless validStage($stage);
		$i++;
	}
	
	return 1;
}

sub validStage {
	my $stage = shift;
	
	print "Validating stage: " . $stage . "\n" if $qst->isVerbose();
	
	if ($stage eq "SCAFFOLD" || $stage eq "DEGAP" || $stage eq "DEDUP" || $stage eq "CLIP") {
		return 1;
	}
	else {
		return 0;
	}
}


__END__

=pod

=head1 NAME

B<improver.pl>


=head1 SYNOPSIS

B<improver.pl> [options] B<--input> F<assembly.fa> B<--config> F<config.cfg>

For full documentation type: "improver.pl --man"


=head1 DESCRIPTION

Runs an sequence of programs that attempt to improve the scaffold quality of the input file.  It does this by doing a user defined number of scaffolding and gap fillings steps.  This script uses additional reads specified by the user in the configuration file to do this. After enhancing the user can optionally discard shorter scaffolds from file.  Finally, the user can optionally request assembly statistics to be produced at each step in the process.


=head1 OPTIONS

=over

=item B<--scaffolder_args>,B<--s_args>

Any additional arguments to send to the scaffolding tool.
	
=item B<--degap_args>,B<--dg_args>

Any additional arguments to send to the degapping tool. 
	
=item B<--clip_args>

Any additional arguments to send to the clipping tool (e.g. --min_length 500)

=item B<--config>,B<--cfg>

REQUIRED: The rampart configuration file describing the read libraries which are used to enhance the input scaffolds file.
  
=item B<--simulate>

Runs the script without sending any jobs to the queue.  Used for testing purposes only.
              
=item B<--stats>

Outputs statistics and plots for all scaffold files produced in the improvement process.

=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

=item B<--project_name>,B<--project>,B<-p>

The project name for the job that will be placed on the grid engine.

=item B<--job_name>,B<--job>,B<-j>

The job name for the job that will be placed on the grid engine.

=item B<--wait_condition>,B<--wait>,B<-w>

If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

=item B<--queue>,B<-q>

The queue to which this job should automatically be sent.

=item B<--memory>,B<--mem>,B<-m>

The amount of memory to reserve for this job.

=item B<--threads>,B<-n>

The number of threads that this job is likely to use.  This is used to reserve cores from the grid engine.

=item B<--extra_args>,B<--ea>

Any extra arguments that should be sent to the grid engine.

=item B<--input>,B<--in>,B<-i>

REQUIRED: The input scaffold file to improve.

=item B<--output>,B<--out>,B<-o>

The output dir for this job. Default: Current working directory (".")

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=item B<--log>

Whether to log the mass scripts settings in a file called F<mass.log> in the directory specified with the B<--output> argument. 

=item B<--help>,B<--usage>,B<-h>,B<-?>

Print usage message and then exit.

=item B<--man>

Display manual.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


