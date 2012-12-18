#!/usr/bin/perl

package JobSubmitter;

use strict;
use warnings;


#use Exception::Class;

sub new {
	my $class = shift;
	my $self = {
		_cmd_submit => shift,
		_cmd_project_name => shift,
		_cmd_job_name => shift,
		_cmd_wait_condition => shift,
		_cmd_queue => shift,
		_cmd_memory => shift,
		_cmd_threads => shift,
		_cmd_openmpi => shift,
		_extra_args => shift,
		_verbose => shift
	};
	bless $self, $class;
	return $self;
}


# Submission method
sub submit {

	my ( $self, $cmd_line ) = @_;

	my $project_arg = $self->{_cmd_project_name} ? $self->{_cmd_project_name} : "";
	my $job_arg = $self->{_cmd_job_name} ? $self->{_cmd_job_name} : "";
	my $wait_arg = $self->{_cmd_wait_condition} ? $self->{_cmd_wait_condition} : "";
	my $queue_arg = $self->{_cmd_queue} ? $self->{_cmd_queue} : "";
	my $memory_arg = $self->{_cmd_memory} ? $self->{_cmd_memory} : "";
	my $threads_arg = $self->{_cmd_threads} ? $self->{_cmd_threads} : "";
	my $openmpi_arg = $self->{_cmd_openmpi} ? $self->{_cmd_openmpi} : "";
	my $extra_args = $self->{_extra_args} ? $self->{_extra_args} : "";
	my $qs_args = $project_arg . " " . $job_arg . " " . $wait_arg . " " . $queue_arg . " " . $memory_arg . " " . $openmpi_arg . " " . $threads_arg . " " . $extra_args;

	if ($self->{_verbose}) {
		print 	"\nJob Submission:\n" .
				"Submitting with: " . $self->{_cmd_submit} . "\n" .
				"Grid Engine Args: " . $qs_args . "\n" .
				"Command to execute: " . $cmd_line . "\n\n";
	}

	my @args;
	push @args, $self->{_cmd_submit};
	push @args, $project_arg if $self->{_cmd_project_name};
	push @args, $job_arg if $self->{_cmd_job_name};
	push @args, $wait_arg if $self->{_cmd_wait_condition};
	push @args, $queue_arg if $self->{_cmd_queue};
	push @args, $memory_arg if $self->{_cmd_memory};
	push @args, $threads_arg if $self->{_cmd_threads};
	push @args, $openmpi_arg if $self->{_cmd_openmpi};
	push @args, $extra_args if $self->{_extra_args};
	push @args, $cmd_line;

	system(@args);
}


# **** Getters ****

sub setProjectName {
	my ( $self, $cmd_project_name ) = @_;
	$self->{_cmd_project_name} = $cmd_project_name;
}

sub getProjectName {
	my ( $self ) = @_;
	return $self->{_cmd_project_name};
}

sub isVerbose {
	my ( $self ) = @_;
	return $self->{_verbose};
}


1;
