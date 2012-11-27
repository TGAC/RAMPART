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

B<mass_gp.pl>


=head1 SYNOPSIS

B<mass_gp.pl> [options] B<--input> F<input_dir>

For full documentation type: "mass_gp.pl --man"


=head1 DESCRIPTION

This script is designed to execute mass_gatherer and mass_plotter together as a job on a grid engine.


=head1 OPTIONS

=over

=item B<--index>

Whether to prefix each row in the statistics table with the index of the scaffold file being analysed.
  
=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

=item B<--project_name>,B<--project>,B<-p>

The project name for the job that will be placed on the grid engine.

=item B<--job_name>,B<--job>,B<-j>

The job name for the job that will be placed on the grid engine.

=item B<--wait_condition>,B<--wait>,B<-w>

If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

=item B<--queue>,B<-q>

The queue to which this job should automatically be sent.

=item B<--extra_args>,B<--ea>

Any extra arguments that should be sent to the grid engine.

=item B<--input>,B<--in>,B<-i>

REQUIRED: The input file for this job.

=item B<--output>,B<--out>,B<-o>

The output directory for this job.  Default: Current working directory (".")

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

