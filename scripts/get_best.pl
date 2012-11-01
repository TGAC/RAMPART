#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
use Pod::Usage;

# Gather Command Line options and set defaults
my (%opt) = ( );
	

GetOptions (
	\%opt,
	'best_assembly_in|in_ba=s',
	'best_dataset_in|in_bd=s',
	'raw_config|raw=s',
	'qt_config|qt=s',
	'best_assembly_out|out_ba=s',
	'best_config_out|out_cfg=s',
	'verbose|v',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );

open BP, "<", $opt{best_assembly_in} or die "Error: Couldn't parse input file.\n\n";
my @bplines = <BP>;
die "Error: Was only expecting a single line.\n\n" unless (@bplines == 1);
my $best_ass = $bplines[0];
close(BP);

open BD, "<", $opt{best_dataset_in} or die "Error: Couldn't parse input file.\n\n";
my @bdlines = <BD>;
die "Error: Was only expecting a single line.\n\n" unless (@bdlines == 1);
my $best_dataset = $bdlines[0];
close(BD);

$best_ass =~ s/\s+$//;
$best_dataset =~ s/\s+$//;
my $best_config = (($best_dataset eq "raw") ? $opt{raw_config} : $opt{qt_config});

system("ln -s -f " . $best_ass . " " . $opt{best_assembly_out});
system("ln -s -f " . $best_config . " " . $opt{best_config_out});

if ($opt{verbose}) {
	print "Best Assembly:" . $best_ass . "\n";
	print "Best Dataset:" . $best_dataset . "\n";
	print "Best Config:" . $best_config . "\n";
}

