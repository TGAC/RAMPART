#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';
use QsOptions;
use SubmitJob;


# Other constants
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $DEF_OUT = $PWD . "/mass_selector.rout";


# R script constants
my $MASS_SELECTOR_R = $RAMPART_DIR . "mass_selector.R";
my $FULL_PLOTTER_R = $RAMPART_DIR . "full_plotter.R";
my $R_SOURCE_CMD = "source R-2.15.0;";

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

my $merged_file = $qso->getOutput() . "/merged.tab";	# This file produced by the mass selector R scripts contains the stats for both datasets
my $plot_file = $qso->getOutput() . "/plots.pdf";

my @r_select_script_args = (
	$opt{raw_stats_file},
	$opt{qt_stats_file},
	$opt{approx_genome_size} ? $opt{approx_genome_size} : "0",
	$qso->getOutput()
);
my $r_select_args = join " ", @r_select_script_args;
my $r_select_cmd_line = "R CMD BATCH '--args " . $r_select_args  . "' " . $MASS_SELECTOR_R . " " .  $qso->getOutput() . "/select_log.rout";

my @r_plot_args = (
	$merged_file,
	$plot_file
);
my $r_plot_args = join " ", @r_plot_args;
my $r_plot_cmd_line = "R CMD BATCH '--args " . $r_plot_args  . "' " . $FULL_PLOTTER_R . " " .  $qso->getOutput() . "/plot_log.rout";

my $r_cmd_line = $R_SOURCE_CMD . " " . $r_select_cmd_line . "; " . $r_plot_cmd_line;

SubmitJob::submit($qso, $r_cmd_line);

print "Selected best assembly from input stats.\n" if $qso->isVerbose();


__END__

=pod

=head1 NAME

  mass_selector.pl


=head1 SYNOPSIS

  mass_selector.pl [options] <input_file>

  For full documentation type: "mass_selector.pl --man"


=head1 DESCRIPTION

  Simplifies the calling of an R script that selects the best assembly from pre-computed raw and quality trimmed assembly statistics.

=head1 OPTIONS

  raw_stats_file|raw          The file containing the statistics for multiple raw assemblies.
  qt_stats_file|qt            The file containing the statistics for multiple quality trimmed assemblies.
  approx_genome_size|ags|s    The approximate genome size (used to determine how close each assembly is to the expected genome size.
  output|o                    The output directory.
  verbose|v                   Print extra status information during run.
  help|usage|h|?              Print usage message and then exit.
  man                         Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


