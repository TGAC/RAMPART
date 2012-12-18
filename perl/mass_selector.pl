#!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/modules";

# 3rd Part modules
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use Cwd;
use Cwd 'abs_path';
use File::Basename;

# RAMPART modules
use QsOptions;
use SubmitJob;
use AppStarter;

# Other constants
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $R_DIR = $RAMPART_DIR . "r_scripts/";
my $WEIGHTINGS_FILE = $RAMPART_DIR . "weightings.tab";
my $DEF_OUT = $PWD . "/mass_selector.rout";


# R script constants
my $MASS_SELECTOR_R = $R_DIR . "mass_selector.R";
my $FULL_PLOTTER_R = $R_DIR . "full_plotter.R";
my $R_SOURCE_CMD = AppStarter::getAppInitialiser("R");

# Parse generic queueing tool options
my $qso = new QsOptions();
$qso->setOutput($DEF_OUT);
$qso->parseOptions();


# Assign any command line options to variables
my %opt;
GetOptions (
        \%opt,
		'raw_stats_file|raw=s',
		'qt_stats_file|qt=s',
        'approx_genome_size|ags|s=i',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};



# Argument Validation

die "Error: No raw stats file was specified\n\n" unless $opt{raw_stats_file};
die "Error: No qt stats file was specified\n\n" unless $opt{qt_stats_file};
#die "Error: Approximate genome size was not specified\n\n" unless $opt{approx_genome_size};



# Produce stats and select best assembly from both datasets

my $score_file = $qso->getOutput() . "/score.tab";	# This file produced by the mass selector R scripts contains the stats for both datasets
my $plot_file = $qso->getOutput() . "/plots.pdf";

my @r_select_script_args = (
	$opt{raw_stats_file},
	$opt{qt_stats_file},
	$opt{approx_genome_size} ? $opt{approx_genome_size} : "0",
	$qso->getOutput(),
	$WEIGHTINGS_FILE
);
my $r_select_args = join " ", @r_select_script_args;
my $r_select_cmd_line = "R CMD BATCH '--args " . $r_select_args  . "' " . $MASS_SELECTOR_R . " " .  $qso->getOutput() . "/select_log.rout";

my @r_plot_args = (
	$score_file,
	$plot_file
);
my $r_plot_args = join " ", @r_plot_args;
my $r_plot_cmd_line = "R CMD BATCH '--args " . $r_plot_args  . "' " . $FULL_PLOTTER_R . " " .  $qso->getOutput() . "/plot_log.rout";

my $r_cmd_line = $R_SOURCE_CMD . $r_select_cmd_line . ";" . $r_plot_cmd_line;

SubmitJob::submit($qso, $r_cmd_line);

print "Selected best assembly from input stats.\n" if $qso->isVerbose();


__END__

=pod

=head1 NAME

B<mass_selector.pl>


=head1 SYNOPSIS

B<mass_selector.pl> [options] B<--raw_stats_file> F<raw_stats.txt> B<--qt_stats_file> F<qt_stats_file.txt>

For full documentation type: "mass_selector.pl --man"


=head1 DESCRIPTION

Simplifies the calling of an R script that selects the best assembly from pre-computed raw and quality trimmed assembly statistics.

=head1 OPTIONS

=over

=item B<--raw_stats_file>,B<--raw>

REQUIRED: The file containing the statistics for multiple raw assemblies.

=item B<--qt_stats_file>,B<--qt>

REQUIRED: The file containing the statistics for multiple quality trimmed assemblies.
  
=item B<--approx_genome_size>,B<--ags>,B<-s>

The approximate genome size (used to determine how close each assembly is to the expected genome size. Default: 0
  
=item B<--output>,B<-o>

The output directory. Default: Current working directory (".")
  
=item B<--verbose>,B<-v>

Print extra status information during run.

=item B<--help>,B<--usage>,B<-h>,B<-?>

Print usage message and then exit.

=item B<--man>

Display manual.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


