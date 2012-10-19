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



# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $MASS_GATHERER_PATH = $RAMPART_DIR . "mass_gatherer.pl";
my $MASS_PLOTTER_PATH = $RAMPART_DIR . "mass_plotter.pl";


# Handle generic queueing system arguments here
my $qst = new QsOptions();
$qst->parseOptions();


# Assign any command line options to variables
my %opt;
GetOptions (
        \%opt,
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


die "Error: Input file not specified\n\n" unless $qst->getInput();



# Combine gatherer and plotter into a single command and submit
my $stat_file = $qst->getOutput() . "/stats.txt";
my $mg_cmd_line = $MASS_GATHERER_PATH . " " . $qst->getInput() . " > " . $stat_file;
my $mp_cmd_line = $MASS_PLOTTER_PATH . " --output " . $qst->getOutput() . " " . $stat_file;
my $cmd_line = $mg_cmd_line . "; " . $mp_cmd_line;

$qst->submit($cmd_line);


__END__

=pod

=head1 NAME

  mass_gp.pl


=head1 SYNOPSIS

  mass_gp.pl [options] -input <input_dir>

  For full documentation type: "mass_gp.pl --man"


=head1 DESCRIPTION

  Mass Assembly Statistics Gatherer and Plotter.  Runs mass_gatherer.pl and mass_plotter.pl automatically, producing statistics and graphs for a collection of assemblies.


=head1 OPTIONS

  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


