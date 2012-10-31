#!/usr/bin/perl

package QsOptions;

use strict;
use warnings;

use Getopt::Long;
use Pod::Usage;
use Cwd;
use LsfJobSubmitter;

# Now
my ( $sec, $min, $hr, $mday, $mon, $year, $wday, $yday, $isdst ) =
  localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;

my $DEFAULT_GRID_ENGINE = "LSF";
my $DEFAULT_QUEUE       = "production";

my $PWD = getcwd;

sub new {
	my $class = shift;
	my $self;

	# Extra attributes, which we'll set later
	$self->{_grid_engine} = $DEFAULT_GRID_ENGINE;
	$self->{_tool}            = undef;
	$self->{_tool_path}       = undef;
	$self->{_project_name}    = undef;
	$self->{_job_name}        = undef;
	$self->{_wait_condition}  = undef;
	$self->{_queue}           = $DEFAULT_QUEUE;
	$self->{_memory}          = undef;
	$self->{_threads}         = undef;
	$self->{_extra_args}      = undef;
	$self->{_input}           = undef;
	$self->{_output}          = $PWD;
	$self->{_verbose}         = 0;

	bless $self, $class;
	return $self;
}

sub parseOptions {
	my ($self) = @_;

	GetOptions(
		'grid_engine|ge=s'     	   => \$self->{_grid_engine},
		'tool|t=s'                 => \$self->{_tool},
		'tool_path|tp=s'           => \$self->{_tool_path},
		'project_name|project|p=s' => \$self->{_project_name},
		'job_name|job|j=s'         => \$self->{_job_name},
		'wait_condition|wait|w=s'  => \$self->{_wait_condition},
		'queue|q=s'                => \$self->{_queue},
		'memory|mem|m=i'           => \$self->{_memory},
		'threads|n=i'              => \$self->{_threads},
		'extra_args|ea=s'          => \$self->{_extra_args},
		'input|in|i=s'             => \$self->{_input},
		'output|out|o=s'           => \$self->{_output},
		'verbose|v'                => \$self->{_verbose}
	) or pod2usage("Try '$0 --help' for more information.");

	# Set defaults if not already set
	$self->{_project_name} = $self->{_tool} unless $self->{_project_name};
	my $tool_str = $self->{_tool} ? ( $self->{_tool} . "-" ) : "";
	$self->{_job_name} = $ENV{'USER'} . "-" . $tool_str . $NOW
	  unless $self->{_job_name};
}

sub toString {

	my ($self) = @_;

	my $string =
	    "Settings:\n"
	  . "Grid Engine: "
	  . $self->{_grid_engine} . "\n"
	  . "Tool: "
	  . $self->{_tool} . "\n"
	  . "Tool path: "
	  . $self->{_tool_path} . "\n"
	  . "Project Name: "
	  . $self->{_project_name} . "\n"
	  . "Job Name: "
	  . $self->{_job_name} . "\n"
	  . "Wait condition: "
	  . ( $self->{_wait_condition} ? $self->{_wait_condition} : "" ) . "\n"
	  . "Queue: "
	  . $self->{_queue} . "\n"
	  . "Memory: "
	  . ( $self->{_memory} ? $self->{_memory} : "" ) . "\n"
	  . "Threads: "
	  . ( $self->{_threads} ? $self->{_threads} : "" ) . "\n"
	  . "Extra Args: "
	  . ( $self->{_extra_args} ? $self->{_extra_args} : "" ) . "\n"
	  . "Input: "
	  . $self->{_input} . "\n"
	  . "Output: "
	  . $self->{_output} . "\n";

	return $string;
}

# **** Setters ****

sub setGridEngine {
	my ( $self, $grid_engine ) = @_;
	$self->{_grid_engine} = $grid_engine;
}

sub setTool {
	my ( $self, $tool ) = @_;
	$self->{_tool} = $tool;
}

sub setToolPath {
	my ( $self, $tool_path ) = @_;
	$self->{_tool_path} = $tool_path;
}

sub setProjectName {
	my ( $self, $project_name ) = @_;
	$self->{_project_name} = $project_name;
}

sub setJobName {
	my ( $self, $job_name ) = @_;
	$self->{_job_name} = $job_name;
}

sub setWaitCondition {
	my ( $self, $wait_condition ) = @_;
	$self->{_wait_condition} = $wait_condition;
}

sub setQueue {
	my ( $self, $queue ) = @_;
	$self->{_queue} = $queue;
}

sub setMemory {
	my ( $self, $memory ) = @_;
	$self->{_memory} = $memory;
}

sub setThreads {
	my ( $self, $threads ) = @_;
	$self->{_threads} = $threads;
}

sub setExtraArgs {
	my ( $self, $ea ) = @_;
	$self->{_extra_args} = $ea;
}

sub setOutput {
	my ( $self, $output ) = @_;
	$self->{_output} = $output;
}

sub setVerbose {
	my ( $self, $verbose ) = @_;
	$self->{_verbose} = $verbose;
}

# **** Getters ****

sub getGridEngine {
	my ($self) = @_;
	return $self->{_grid_engine};
}

sub getTool {
	my ($self) = @_;
	return $self->{_tool};
}

sub getToolPath {
	my ($self) = @_;
	return $self->{_tool_path};
}

sub getProjectName {
	my ($self) = @_;
	return $self->{_project_name};
}

sub getJobName {
	my ($self) = @_;
	return $self->{_job_name};
}

sub getWaitCondition {
	my ($self) = @_;
	return $self->{_wait_condition};
}

sub getQueue {
	my ($self) = @_;
	return $self->{_queue};
}

sub getResources {
	my ($self) = @_;
	return $self->{_resources};
}

sub getMemoryGB {
	my ($self) = @_;
	return $self->{_memory};
}

sub getMemoryMB {
	my ($self) = @_;
	return $self->{_memory} * 1000;
}

sub getThreads {
	my ($self) = @_;
	return $self->{_threads};
}

sub getExtraArgs {
	my ($self) = @_;
	return $self->{_extra_args};
}

sub getInput {
	my ($self) = @_;
	return $self->{_input};
}

sub getOutput {
	my ($self) = @_;
	return $self->{_output};
}

sub isVerbose {
	my ($self) = @_;
	return $self->{_verbose};
}

# **** Param Getters ****

sub getGridEngineAsParam {
	my ($self) = @_;
	return $self->{_grid_engine}
	  ? ( "--grid_engine " . $self->{_grid_engine} )
	  : "";
}

sub getToolAsParam {
	my ($self) = @_;
	return $self->{_tool} ? ( "--tool " . $self->{_tool} ) : "";
}

sub getToolPathAsParam {
	my ($self) = @_;
	return $self->{_tool_path} ? ( "--tool_path " . $self->{_tool_path} ) : "";
}

sub getProjectNameAsParam {
	my ($self) = @_;
	return $self->{_project_name}
	  ? ( "--project_name " . $self->{_project_name} )
	  : "";
}

sub getJobNameAsParam {
	my ($self) = @_;
	return $self->{_job_name} ? ( "--job_name " . $self->{_job_name} ) : "";
}

sub getWaitConditionAsParam {
	my ($self) = @_;
	return $self->{_wait_condition}
	  ? ( "--wait_condition " . $self->{_wait_condition} )
	  : "";
}

sub getQueueAsParam {
	my ($self) = @_;
	return $self->{_queue} ? ( "--queue " . $self->{_queue} ) : "";
}

sub getResourcesAsParam {
	my ($self) = @_;
	return $self->{_resources} ? ( "--resources " . $self->{_resources} ) : "";
}

sub getMemoryAsParam {
	my ($self) = @_;
	return $self->{_memory} ? ( "--memory " . $self->{_memory} ) : "";
}

sub getThreadsAsParam {
	my ($self) = @_;
	return $self->{_threads} ? ( "--threads " . $self->{_threads} ) : "";
}

sub getExtraArgsAsParam {
	my ($self) = @_;
	return $self->{_extra_args}
	  ? ( "--extra_args " . $self->{_extra_args} )
	  : "";
}

sub getInputAsParam {
	my ($self) = @_;
	return $self->{_input} ? ( "--input " . $self->{_input} ) : "";
}

sub getOutputAsParam {
	my ($self) = @_;
	return $self->{_output} ? ( "--output " . $self->{_output} ) : "";
}

sub isVerboseAsParam {
	my ($self) = @_;
	return $self->{_verbose} ? "--verbose" : "";
}

1;

# Note that this pod is not intended to be used directly but rather as a template for tools that make use of QsTool.

__END__

=pod

=head1 NAME

  <script_name>.pl


=head1 SYNOPSIS

  <script_name>.pl [options] -i <input_file>

  For full documentation type: "<script_name>.pl --man"


=head1 DESCRIPTION

  <Script description>.  This script is designed to execute jobs on a grid engine.


=head1 OPTIONS

  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

  --tool                 -t
              If this script supports multiple tools to do the same job you can specify that tool using this parameter.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

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

