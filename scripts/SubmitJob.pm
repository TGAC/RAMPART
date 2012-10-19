#!/usr/bin/perl

use strict;
use warnings;

use QsTool;
use LSFJobSubmitter;
#use PBSJobSubmitter;


sub submit {

        my ( $qso, $cmd_line ) = @_;


        if ($qso->{_queueing_system} eq "LSF") {
        #       my $lqs = new LsfJobSubmitter($self->{_verbose}, $self->{_project_name});
        #       $lqs->submit($self->{_job_name}, $self->{_wait_condition}, $self->{_queue}, $self->{_memory}, $self->{_threads), $self->{_extra_args}, $cmd_line);
                my $lqs = new LsfJobSubmitter($qso);
                $lqs->submit($cmd_line);
        }
        elsif ($qso->{_queueing_system} eq "PBS") {
                print "PBS not implemented yet.  No job submitted.\n";
        }
        else {
                print "Unknown queueing system requested.  No job submitted.\n";
        }
}






