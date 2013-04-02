#!/usr/bin/perl

package AppStarter;

use strict;
use warnings;

# Add current directory to @INC
use File::Basename;
use lib basename ($0);

use Cwd;
use Cwd 'abs_path';
use File::Basename;

use Configuration;

sub getAppInitialiser {
	my ($app) = @_;
	
	my ( $RAMPART, $RAMPART_DIR ) = fileparse( abs_path($0) );
	my $cfg_path = $RAMPART_DIR . "grid_engine_app_startup.cfg";
	
	#print "Rampart config path: " . $cfg_path . "\n";
	#print "App requested: " . $app . "\n";
	
	my $config = new Configuration( $cfg_path );
	
	my $progs = $config->getSectionAt(0);
	my $cmd = $progs->{$app};
	
	#print "App command retrieved from config file: " . $cmd . "\n";
	
	return $cmd ? $cmd : "";
}

1;