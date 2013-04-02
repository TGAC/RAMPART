#!/usr/bin/perl

package SubmitJob;

use strict;
use warnings;

# Static method that initiates a concrete instance of a JobSubmitter based on which grid engine is requested in the first parameter's QsOptions queueing system value.
sub submit {

	my ( $qso, $cmd_line ) = @_;
	
	if ( $qso->{_grid_engine} eq "LSF" ) {
		my $lqs = new LsfJobSubmitter($qso);
		$lqs->submit($cmd_line);
	}
	elsif ( $qso->{_grid_engine} eq "PBS" ) {
		print "PBS not implemented yet.  No job submitted.\n";
	}
	elsif ( $qso->{_grid_engine} eq "NONE" ) {

		# No grid engine requested.  Running job directly
		print("Executing command line: " . $cmd_line . "\n") if $qso->isVerbose();
		
		system($cmd_line);
	}
	else {
		# No grid engine requested.  Running job directly
		print("Grid engine undefined.  No job submitted");
	}
}

1;
