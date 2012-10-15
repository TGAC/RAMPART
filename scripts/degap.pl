#!/usr/bin/perl

use strict;

use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;


# Now
my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;

# Project constants
my $DEF_PROJECT_NAME = "de_gap_" . $NOW;
my $DEF_JOB_NAME = $ENV{'USER'} . "-de_gap-" . $NOW;

# Gap closing constants
my $DG_GAP_CLOSER = "gapcloser";
my $DG_IMAGE = "image";
my $DG_GAP_FILLER = "gapfiller";
my $DEF_TOOL = $DG_GAP_CLOSER;
my $DEF_GC_PATH = "GapCloser";
my $DEF_IMAGE_PATH = "image";
my $DEF_GF_PATH = "gapfiller";

# Read length constants
my $DEF_READ_LENGTH = 155;

# Queueing system constants
my $SUBMIT = "bsub";
my $DEF_QUEUE_ARGS = "-q production";
my $GC_SOURCE_CMD = "source GapCloser-1.12;";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


my (%opt) = (   "tool",		   	$DEF_TOOL,
                "gc_path",      	$DEF_GC_PATH,
		"project",              $DEF_PROJECT_NAME,
		"job_name",		$DEF_JOB_NAME,
		"extra_queue_args",     $DEF_QUEUE_ARGS,
		"readlen",		$DEF_READ_LENGTH,
                "output",       	$PWD );

GetOptions (
        \%opt,
        'tool|t=s',
	'tool_path|tp=s',
        'project|p=s',
	'job_name|job|j=s',
	'extra_queue_args|eqa|q=s',
	'readlen|rl|r=i',
	'wait_job|wj|w=s',
        'input|in|i=s',
        'config|c=s',
        'output|out|o=s',
        'verbose|v',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


die "Error: No input file specified\n\n" unless $opt{input};
die "Error: No output directory specified\n\n" unless $opt{output};
die "Error: No library config file specified\n\n" unless $opt{config};


my $job_arg = "-J" . $opt{job_name};
my $project_arg = "-P" . $opt{project};
my $queue_arg = $opt{extra_queue_args};
my $cmd_line = "";
my $wait_arg = "-wdone(" . $opt{wait_job} . ")" if $opt{wait_job};


if ($opt{verbose}) {
	print "Input Scaffold File: " . $opt{input} . "\n";
        print "Output Directory: ". $opt{output} . "\n";
        print "Project Arg: " . $project_arg . "\n";
	print "Queue Arg: " . $queue_arg . "\n";
	print "Wait Arg: " . $wait_arg . "\n";
}


# Select the gap closer and build the command line

if ($opt{tool} eq $DG_GAP_CLOSER) {

	my $gc_exe = $DEF_GC_PATH;
	my $gc_scaffolds = $opt{output} . "/gc-scaffolds.fa";
	$cmd_line = $GC_SOURCE_CMD . " " . $gc_exe . " -a \"" . $opt{input} . "\" -b \"" . $opt{config} . "\" -o \"" . $gc_scaffolds . "\" -l " . $opt{readlen} . " -p 61";

	if ($opt{verbose}) {
		print "\n";
		print "Initiating GapCloser job: " . $cmd_line . "\n";
	}

}
elsif ($opt{tool} eq $DG_IMAGE) {

	my $image_exe = $DEF_IMAGE_PATH;
	my $image_scaffolds = $opt{output} . "/image-scaffolds.fa";
	$cmd_line = $image_exe;

	die "Error: IMAGE (Iterative Mapping and Assembly for Gap Elimination) tool not implemented in this script yet.\n\n";
}
elsif ($opt{tool} eq $DG_GAP_FILLER) {

	my $gf_exe = $DEF_IMAGE_PATH;
	my $gf_scaffolds = $opt{output} . "/gf-scaffolds.fa";
	$cmd_line = $gf_exe;

	die "Error: Gap filler tool not implemented in this script yet.\n\n";
}
else {
        die "Error: Invalid gap closing tool requested.  Also, the script should not have got this far!!!.\n\n";
}

if ($opt{wait_job}) {
	system($SUBMIT, $job_arg, $project_arg, $queue_arg, $wait_arg, $cmd_line);
}
else {
	system($SUBMIT, $job_arg, $project_arg, $queue_arg, $cmd_line);
}




__END__

=pod

=head1 NAME

  degap.pl


=head1 SYNOPSIS

  degap.pl [options] -i contigs_file -c config_file

  input|in|i       The path to the input contigs file.
  config|cfg|c     The degapping library configuration file.

  For full documentation type: "degap.pl --man"


=head1 DESCRIPTION

  Runs a gap closing program in an attempt to fill in gaps within and around scaffolds.


=head1 OPTIONS

  tool|t                   The gap closing tool to use (gapcloser, image).
  tool_path|tp             The path to the gap closing tool to use.
  project|p                The project name for marking the LSF jobs.
  extra_queue_args|eqa|q   Extra arguments to pass to the queueing system for each assembly job.
  readlen|rl|r             The length of the reads used to build the assembly.
  wait_job|wj|w            If specified, gap closing will not start until this job is completed.
  output|out|o=s           The output directory.
  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

