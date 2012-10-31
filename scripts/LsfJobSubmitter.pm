#!/usr/bin/perl

package LsfJobSubmitter;

use strict;
use warnings;

use JobSubmitter;
our @ISA = qw(JobSubmitter);

sub new {
        my ($class, $qst) = @_;

        my $CMD_SUBMIT = "bsub";
        my $CMD_PROJECT = $qst->{_project_name} ? ("-P" . $qst->{_project_name}) : undef;
        my $CMD_JOB = $qst->{_job_name} ? ("-J" . $qst->{_job_name}) : undef;
        my $CMD_WAIT = $qst->{_wait_condition} ? ("-w" . $qst->{_wait_condition}) : undef;
        my $CMD_QUEUE = $qst->{_queue} ? ("-q" . $qst->{_queue}) : undef;

	my $CMD_MEMORY;
	if ($qst->{_memory}) {
		my $mem_mb = $qst->{_memory} * 1000;
		$CMD_MEMORY = "-Rrusage[mem=" . $mem_mb . "]span[ptile=8]";
	}
	else {
		$CMD_MEMORY = undef;
	}

	my $CMD_THREADS = ($qst->{_threads} && $qst->{_threads} > 1) ? ("-n" . $qst->{_threads}) : undef;
	my $CMD_OPENMPI = $qst->{_threads} ? "-aopenmpi" : undef;

        # Call base class constructor
        my $self = $class->SUPER::new(
                                $CMD_SUBMIT,
                                $CMD_PROJECT,
                                $CMD_JOB,
                                $CMD_WAIT,
                                $CMD_QUEUE,
				$CMD_MEMORY,
				$CMD_THREADS,
				$CMD_OPENMPI,
				$qst->{_extra_args},
                                $qst->{_verbose});
        bless $self, $class;
        return $self;
}

