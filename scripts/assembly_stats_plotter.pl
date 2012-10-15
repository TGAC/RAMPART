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


# Assembly stats plotting constants
my $STATS_PLOTTER_SCRIPT = $RAMPART_DIR . "/assembly_stats_plotter.R";
my $R_SOURCE_CMD = "source R-2.15.0;";


# Assign any command line options to variables
my (%opt) = (	"output",       $DEF_OUT);


GetOptions (
        \%opt,
        'output|out|o=s',
        'verbose|v',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


# Get input files
my @in_files = @ARGV;
my $input_file = join " ", @in_files;


# Argument Validation

die "Error: No input files specified\n\n" unless @in_files;
die "Error: Was only expecting a single file to process\n\n" unless @in_files == 1;



# Get produce stats and graphs for raw dataset

system($R_SOURCE_CMD . " R CMD BATCH '--args " . $input_file . "' " . $STATS_PLOTTER_SCRIPT . " " .  $opt{output});

print "Plotted graphs from raw and qt datasets.\n" if $opt{verbose};



__END__

=pod

=head1 NAME

  assembly_stats_plotter.pl


=head1 SYNOPSIS

  assembly_stats_plotter.pl [options] <input_file>

  For full documentation type: "assembly_stats_plotter.pl --man"


=head1 DESCRIPTION

  Simplifies the calling of an R script that plots assembly changes in assembly statistics across multiple assemblies.

=head1 OPTIONS

  output|o         The directory to which output should be written
  verbose|v        Print extra status information during run.
  help|usage|h|?   Print usage message and then exit.
  man              Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


