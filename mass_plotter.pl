#!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/modules";

# 3rd Part modules
use Getopt::Long;
use Pod::Usage;
use Cwd;
use Cwd 'abs_path';
use File::Basename;


# Other constants
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $R_DIR = $RAMPART_DIR . "r_scripts/";
my $DEF_OUT = $PWD;


# Assembly stats plotting constants
my $MASS_PLOTTER_SCRIPT = $R_DIR . "mass_plotter.R";
my $R_SOURCE_CMD = "source R-2.12.2;";


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

my $output_log = $opt{output} . "/plotter.rout";
my $output_plot = $opt{output} . "/stats.pdf";
system($R_SOURCE_CMD . "R CMD BATCH '--args " . $input_file . " " . $output_plot . "' " . $MASS_PLOTTER_SCRIPT . " " .  $output_log);

print "Plotted graphs from raw and qt datasets.\n" if $opt{verbose};



__END__

=pod

=head1 NAME

B<mass_plotter.pl>


=head1 SYNOPSIS

B<mass_plotter.pl> [options] F<assembly_statistics_file>

For full documentation type: "mass_plotter.pl --man"


=head1 DESCRIPTION

Multiple Assembly Statistics Plotter.  Simplifies the calling of an R script that plots assembly changes in assembly statistics across multiple assemblies.

=head1 OPTIONS

=over

=item B<--output>,B<-o>

The directory to which output should be written

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



