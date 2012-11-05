#!/usr/bin/perl

package PbsJobSubmitter;

use strict;
use warnings;

# Add current directory to @INC
use File::Basename;
use lib basename ($0);

use JobSubmitter;
our @ISA = qw(JobSubmitter);

sub new {
	my ( $class, $qst ) = @_;

	my $CMD_SUBMIT  = "qsub";
	my $CMD_PROJECT = undef;    # No Pbs equivalent
	my $CMD_JOB = $qst->{_job_name} ? ( "-N" . $qst->{_job_name} ) : undef;
	my $CMD_WAIT = $qst->{_wait_condition} ? ( "-w" . $qst->{_wait_condition} ) : undef;
	my $CMD_QUEUE = $qst->{_queue} ? ( "-q" . $qst->{_queue} ) : undef;

	my $CMD_MEMORY  = undef;    # To implement
	my $CMD_THREADS = undef;    # To implement
	my $CMD_OPENMPI = undef;    # Not sure there's a Pbs option for this
	# Call base class constructor
	my $self = $class->SUPER::new(
		$CMD_SUBMIT,  $CMD_PROJECT, $CMD_JOB,
		$CMD_WAIT,    $CMD_QUEUE,   $CMD_MEMORY,
		$CMD_THREADS, $CMD_OPENMPI, $qst->{_extra_args},
		$qst->{_verbose}
	);
	bless $self, $class;
	return $self;
}

