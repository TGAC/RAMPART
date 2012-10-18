#!/usr/bin/perl

package QsTool;

use strict;
use warnings;

use Getopt::Long;
use Pod::Usage;
use Cwd;
use LsfJobSubmitter;


# Now
my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;

my $DEFAULT_QUEUEING_SYSTEM = "LSF";
my $DEFAULT_QUEUE = "production";

my $PWD = getcwd;


sub new {
        my $class = shift;
        my $self;

	# Extra attributes, which we'll set later
	$self->{_queueing_system}	= $DEFAULT_QUEUEING_SYSTEM;
	$self->{_tool}			= undef;
	$self->{_tool_path}		= undef;
	$self->{_project_name}		= undef;
	$self->{_job_name}		= undef;
        $self->{_wait_condition}	= undef;
        $self->{_queue}			= $DEFAULT_QUEUE;
	$self->{_memory}		= undef;
	$self->{_threads}		= undef;
	$self->{_extra_args} 		= undef;
	$self->{_input}			= undef;
	$self->{_output}		= $PWD;
        $self->{_verbose}		= 0;

        bless $self, $class;
        return $self;
}




sub parseOptions {
	my ( $self ) = @_;

	GetOptions (
		'queueing_system|qs=s'		=> \$self->{_queueing_system},
		'tool|t=s' 			=> \$self->{_tool},
		'tool_path|tp=s'		=> \$self->{_tool_path},
		'project_name|project|p=s'	=> \$self->{_project_name},
		'job_name|job|j=s'		=> \$self->{_job_name},
		'wait_condition|wait|w=s'	=> \$self->{_wait_condition},
		'queue|q=s'			=> \$self->{_queue},
		'memory|mem|m=i'		=> \$self->{_memory},
		'threads'			=> \$self->{_threads},
		'extra_args|ea|q=s'		=> \$self->{_extra_args},
		'input|in|i=s'			=> \$self->{_input},
		'output|out|o=s'		=> \$self->{_output},
		'verbose|v'			=> \$self->{_verbose}
	)
	or pod2usage( "Try '$0 --help' for more information." );


	# Set defaults if not already set
	$self->{_project_name} = $self->{_tool} unless $self->{_project_name};
	my $tool_str = $self->{_tool} ? ($self->{_tool} . "-") : "";
	$self->{_job_name} = $ENV{'USER'} . "-" . $tool_str . $NOW unless $self->{_job_name};
}




sub toString {

	my ( $self ) = @_;

	my $string = 	"Settings:\n" .
			"Tool: " . $self->{_tool} . "\n" .
		        "Tool path: " . $self->{_tool_path} . "\n" .
		        "Project Name: " . $self->{_project_name} . "\n" .
			"Job Name: " . $self->{_job_name} . "\n" .
			"Wait condition: " . ($self->{_wait_condition} ? $self->{_wait_condition} : "") . "\n".
			"Queue: " . $self->{_queue} . "\n" .
			"Memory: " . ($self->{_memory} ? $self->{_memory} : "") . "\n" .
			"Threads: " . ($self->{_threads} ? $self->{_threads} : "") . "\n" .
			"Extra Args: " . ($self->{_extra_args} ? $self->{_extra_args} : "") . "\n" .
			"Input: " . $self->{_input} . "\n" .
		        "Output: ". $self->{_output} . "\n";

	return $string;
}



sub submit {

	my ( $self, $cmd_line ) = @_;


	if ($self->{_queueing_system} eq "LSF") {
	#	my $lqs = new LsfJobSubmitter($self->{_verbose}, $self->{_project_name});
	#	$lqs->submit($self->{_job_name}, $self->{_wait_condition}, $self->{_queue}, $self->{_memory}, $self->{_threads), $self->{_extra_args}, $cmd_line);
		my $lqs = new LsfJobSubmitter($self);
		$lqs->submit($cmd_line);
	}
	elsif ($self->{_queueing_system} eq "PBS") {
		print "PBS not implemented yet.  No job submitted.\n";
	}
	else {
		print "Unknown queueing system requested.  No job submitted.\n";
	}
}





# **** Setters ****

sub setQueueingSystem {
	my ( $self, $queueing_system ) = @_;
        $self->{_queueing_system} = $queueing_system;
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

sub setOutput {
	my ( $self, $output ) = @_;
	$self->{_output} = $output;
}

sub setVerbose {
	my ( $self, $verbose ) = @_;
	$self->{_verbose} = $verbose;
}




# **** Getters ****


sub getQueueingSystem {
	my ( $self ) = @_;
	return $self->{_queueing_system};
}

sub getTool {
        my ( $self ) = @_;
        return $self->{_tool};
}

sub getToolPath {
        my ( $self ) = @_;
        return $self->{_tool_path};
}

sub getProjectName {
        my ( $self ) = @_;
        return $self->{_project_name};
}

sub getJobName {
        my ( $self ) = @_;
        return $self->{_job_name};
}

sub getWaitCondition {
        my ( $self ) = @_;
        return $self->{_wait_condition};
}

sub getQueue {
        my ( $self ) = @_;
        return $self->{_queue};
}

sub getResources {
        my ( $self ) = @_;
        return $self->{_resources};
}

sub getMemoryGB {
        my ( $self ) = @_;
        return $self->{_memory};
}

sub getMemoryMB {
	my ( $self ) = @_;
	return $self->{_memory} * 1000;
}

sub getThreads {
        my ( $self ) = @_;
        return $self->{_threads};
}

sub getExtraArgs {
        my ( $self ) = @_;
        return $self->{_extra_args};
}

sub getInput {
	my ( $self ) = @_;
        return $self->{_input};
}

sub getOutput {
        my ( $self ) = @_;
        return $self->{_output};
}

sub isVerbose {
        my ( $self ) = @_;
        return $self->{_verbose};
}




1;
