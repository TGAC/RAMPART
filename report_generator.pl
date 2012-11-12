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
use Configuration;
use SubmitJob;


#### Constants

# Project constants
my $JOB_PREFIX = $ENV{'USER'} . "-report-";


# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));

# Tool constants
my $REPORT_GEN_TOOL_CMD = "java -jar " . $RAMPART_DIR . "ReportGenerator-0.1.jar";
my $REPORT_TEMPLATE_DIR = $RAMPART_DIR . "/data/report_template/";

# Gather Command Line options and set defaults
my (%opt) = ( "output", $PWD );
	

GetOptions (
	\%opt,
	'input|in|i=s',
	'output|out|o=s',
	'verbose|v',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




#### Validation

die "Error: No input directory specified\n\n" unless $opt{input};



# Build file structure vars
my $READS_DIR 		= $opt{input} . "reads/";
my $MASS_DIR 		= $opt{input} . "mass/";
my $IMPROVER_DIR 	= $opt{input} . "improver/";
my $REPORT_DIR 		= $opt{output} . "report/";


# Build context
my $context_path = $REPORT_DIR . "report.ctx";
my $report_images_dir = $REPORT_DIR . "images";
mkdir $report_images_dir;

# Copy known resources
system("cp " . $REPORT_TEMPLATE_DIR . "header.png " .$report_images_dir); 

# Gather statistics for template context
gather_context($opt{input}, $report_images_dir, $context_path);


# Run merging tool
my $report_file = $REPORT_DIR . "report.tex";
my @rg_args = grep {$_} (
	$REPORT_GEN_TOOL_CMD,
	"--template " . $REPORT_TEMPLATE_DIR . "template.tex",
	"--context " . $context_path,
	"--output " . $report_file,
	$opt{verbose} ? "--verbose" : ""
);
system(join " ", @rg_args);

# Compile LaTeX project
system("pdflatex -output-directory=" . $REPORT_DIR . " " . $report_file);


# Make a symbolic link somewhere?
#system ("ln -s -f ")



sub gather_context {
	my $input_dir = shift;
	my $images_dir = shift;
	my $context_out = shift;
	
	
}


__END__

=pod

=head1 NAME

  report_generator.pl


=head1 SYNOPSIS

  report_generator.pl [options] --input <rampart_job_dir>

  For full documentation type: "report_generator.pl --man"


=head1 DESCRIPTION

  This script is designed to automatically generate a report containing details of all aspects of a given rampart job


=head1 OPTIONS

  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

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

  --output               --out               -o
              The output file/dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut



