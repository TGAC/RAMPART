#!/usr/bin/perl

package SubmitJob;

use strict;
use warnings;

# Static method that initiates a concrete instance of a JobSubmitter based on which queuing system is requested in the first parameter's QsOptions queueing system value.
sub submit {

        my ( $qso, $cmd_line ) = @_;


        if ($qso->{_queueing_system} eq "LSF") {
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

1;