#!/usr/bin/perl

use Getopt::Long;
use Pod::Usage;
use File::Find;
use Cwd;

my %args;


# Assembler constants
$A_ABYSS = "abyss";
$DEF_ASSEMBLER = $A_ABYSS;

# Assembly stats gathering constants
$STATS_GATHERER = "./assembly_stats_gatherer.pl";
$STATS_PLOTTER = "./assembly_stats_plotter.R";


# Other constants
$PWD = getcwd;

# Assign any command line options to variables
my (%opt) = (   "assembler",    $DEF_ASSEMBLER,
                "output",       $PWD);

$opt{seq_info} = $SEQ_INFO_PATH if ($SEQ_INFO_PATH);

GetOptions (
        \%opt,
        'assembler|a=s',
	'raw|r=s',
	'qt|r=s',
        'output|out|o=s',
        'verbose|v',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


die "Error: The raw assemblies directory was not specified\n\n" . $USAGE unless $opt{raw};
die "Error: The quality trimmed assemblies directory was not specified\n\n" . $USAGE unless $opt{qt};

die "Error: The raw assemblies directory does not exist: " . $opt{raw} . "\n\n" . $USAGE unless (-e $opt{raw});
die "Error: The quality trimmed assemblies directory does not exist: " . $opt{qt} . "\n\n" . $USAGE unless (-e $opt{qt});



# Get produce stats and graphs for raw dataset

$raw_stats_out = $opt{output} . "/raw.stats";
$qt_stats_out = $opt{output} . "/qt.stats";

system($STATS_GATHERER . " " . $opt{raw} . " > " . $raw_stats_out);
system($STATS_GATHERER . " " . $opt{qt} . " > " . $qt_stats_out);

print "Gathered statistics for raw and qt datasets.\n" if $opt{verbose};

system("R CMD BATCH '--args " . $raw_stats_out . "' " . $STATS_PLOTTER . " " .  $raw_stats_out . ".rout");
system("R CMD BATCH '--args " . $qt_stats_out . "' " . $STATS_PLOTTER . " " . $qt_stats_out . ".rout");

print "Plotted graphs from raw and qt datasets.\n" if $opt{verbose};


# Now call an R script which will apply weightings to each statistic and determine which assembly is 'best'
$decider_out = $opt{output} . "/decision.txt";
system("R CMD BATCH '--args " . $raw_stats_out . " " . $qt_stats_out . "' " . $STATS_DECIDER . " " . $decider_out);







__END__

=pod

=head1 NAME

  assembly_analyser.pl


=head1 SYNOPSIS

  assembly_analyser.pl [options] <input_directory>

  For full documentation type: "assembly_analyser.pl --man"


=head1 DESCRIPTION

  Compares assemblies created from raw and quality trimmed sequence data.  Produces statistics and graphs for assemblies in each dataset and makes an assesment as to which assembly is the 'best' based on the given metrics and weighting system.


=head1 OPTIONS

  assembler|a      The assembly program to use.
  raw|r            The directory containing the assemblies created from raw sequence data
  qt|q             The directory containing the assemblies created from quality trimmed sequence data
  output|o         The directory to which output should be written
  verbose|v        Print extra status information during run.
  help|usage|h|?   Print usage message and then exit.
  man              Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


