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

use QsOptions;
use SubmitJob;

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;
my ( $RAMPART, $RAMPART_DIR ) = fileparse( abs_path($0) );
my $MASS_GATHERER_PATH = $RAMPART_DIR . "mass_gatherer.pl";
my $MASS_PLOTTER_PATH  = $RAMPART_DIR . "mass_plotter.pl";

# Handle generic queueing system arguments here
my $qst = new QsOptions();
$qst->parseOptions();

# Assign any command line options to variables
my %opt;
GetOptions( 
	\%opt, 
	'index',
	'help|usage|h|?', 
	'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

die "Error: Input file not specified\n\n" unless $qst->getInput();

# Combine gatherer and plotter into a single command and submit
my $stat_file = $qst->getOutput() . "/stats.txt";
my $index_arg = $opt{index} ? "--index" : "";
my $mg_cmd_line = $MASS_GATHERER_PATH . " " . $index_arg . " " . $qst->getInput() . " > " . $stat_file;
my $mp_cmd_line = $MASS_PLOTTER_PATH . " --output " . $qst->getOutput() . " " . $stat_file;
my $cmd_line = $mg_cmd_line . "; " . $mp_cmd_line;

SubmitJob::submit( $qst, $cmd_line );

__END__

=pod

=head1 NAME

  mass_gp.pl


=head1 SYNOPSIS

  mass_gp.pl [options] -i <input_dir>

  For full documentation type: "mass_gp.pl --man"


=head1 DESCRIPTION

  This script is designed to execute mass_gatherer and mass_plotter together as a job on a grid engine.


=head1 OPTIONS

  --index
              Whether to prefix each row in the statistics table with the index of the scaffold file being analysed.
  
  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

  --tool                 -t
              If this script supports multiple tools to do the same job you can specify that tool using this parameter.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

  --project_name         --project           -p
              The project name for the job that will be placed on the grid engine.

  --job_name             --job               -j
              The job name for the job that will be placed on the grid engine.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --input                --in                -i
              REQUIRED: The input directory containing scaffold files for this job.

  --output               --out               -o
              The output file/dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


