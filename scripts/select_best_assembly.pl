#!/usr/bin/perl

use strict;

use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';


# Other constants
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $DEF_OUT = $PWD . "/plotter.rout";


# R script constants
my $SELECT_BEST_ASSEMBLY_SCRIPT = $RAMPART_DIR . "/select_best_assembly.R";


# Assign any command line options to variables
my (%opt) = (	"output",       $DEF_OUT);


GetOptions (
        \%opt,
	'raw_stats_file|raw=s',
	'qt_stats_file|qt=s',
        'approx_genome_size|ags|s=i',
	'output|out|o=s',
        'verbose|v',
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
die "Error: Approximate genome size was not specified\n\n" unless $opt{approx_genome_size};

die "Error: raw stats file does not exist\n\n" unless -e $opt{raw_stats_file};
die "Error: qt stats file does not exist\n\n" unless -e $opt{qt_stats_file};



# Get produce stats and graphs for raw dataset

my $r_script_args = $opt{raw_stats_file} . " " . $opt{qt_stats_file} . " " . $opt{approx_genome_size} . " " . $opt{output};

system("R CMD BATCH '--args " . $r_script_args  . "' " . $SELECT_BEST_ASSEMBLY_SCRIPT . " " .  $opt{output} . "/log.rout");

print "Selected best assembly from input stats.\n" if $opt{verbose};



__END__

=pod

=head1 NAME

  select_best_assembly.pl


=head1 SYNOPSIS

  select_best_assembly.pl [options] <input_file>

  For full documentation type: "select_best_assembly.pl --man"


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


