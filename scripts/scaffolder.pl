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
my $DEF_PROJECT_NAME = "Scaffolder_" . $NOW;
my $JOB_NAME = $ENV{'USER'} . "-scaffolder-" . $NOW;

# Scaffolder constants
my $S_SSPACE = "sspace";
my $S_GRASS = "grass";
my $DEF_SCAFFOLDER = $S_SSPACE;
my $DEF_SSPACE_PATH = "/common/software/SSPACE-BASIC-2.0/x86_64/bin/SSPACE_Basic_v2.0.pl";
my $DEF_GRASS_PATH = "grass";

# Queueing system constants
my $SUBMIT = "bsub";
my $DEF_QUEUE_ARGS = "-q production";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


my (%opt) = (	"scaffolder",		$DEF_SCAFFOLDER,
		"scaffolder_path",	$DEF_SSPACE_PATH,
		"extra_queue_args",     $DEF_QUEUE_ARGS,
		"output",		$PWD );

GetOptions (
	\%opt,
	'scaffolder|s=s',
	'scaffolder_path|sp|p=s',
	'project|p=s',
	'extra_queue_args|eqa|q=s',
	'wait_job|wj=s',
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


if ($opt{verbose}) {
	print "Output Directory: ". $opt{output} . "\n";
	print "Project Name: " . $opt{project} . "\n";
}

my $job_arg = "-J" . $JOB_NAME;
my $project_arg = "-P" . $opt{project};
my $queue_arg = $opt{extra_queue_args};
my $wait_arg = "-w \"done(" . $opt{wait_job} . ")\"";
my $cmd_line = "";


# Select the scaffolder and build the command line

if ($opt{scaffolder} eq $S_SSPACE) {

	my $sspace_exe = $DEF_SSPACE_PATH;
	my $sspace_scaffolds = "scaffolder";

	if ($opt{verbose}) {
		print "\n";
		print "Running SSPACE:\n";
		print " - SSPACE: Script location: " . $sspace_exe . "\n";
		print " - SSPACE: Library file: " . $opt{config} . "\n";
		print " - SSPACE: Input scaffold: " . $opt{input} . "\n";
		print " - SSPACE: Scaffold location: " . $sspace_scaffolds . "\n";
	}

	$cmd_line = $sspace_exe . " -l " . $opt{config} . " -s " . $opt{input} . " -x 1 -T 2 -b " . $sspace_scaffolds;

}
elsif ($opt{scaffolder) eq $S_GRASS) {
	my $grass_exe = $DEF_GRASS_PATH;

	die "Error: Grass not implemented yet.\n\n";
}
else {
	die "Error: Invalid scaffolder requested.  Also, the script should not have got this far!!!.\n\n";
}


# Submit the scaffolding job
if ($opt{wait_job}) {
	system($SUBMIT, $job_arg, $project_arg, $queue_arg, $wait_arg, $cmd_line);
else {
	system($SUBMIT, $job_arg, $project_arg, $queue_arg, $cmd_line);
}



__END__

=pod

=head1 NAME

  scaffolder.pl


=head1 SYNOPSIS

  scaffolder.pl [options] -i contigs_file -c config_file

  input|in|i       The path to the input contigs file.
  config|cfg|c     The scaffolder library configuration file.

  For full documentation type: "scaffolder.pl --man"


=head1 DESCRIPTION

  Runs a scaffolding program.


=head1 OPTIONS

  scaffolder|s             The scaffolding tool to use (sspace, grass).
  scaffolder_path|sp|p     The path to the scaffolding tool (in case this script does not know where to find it)
  project|p                The project name for marking the LSF jobs.
  extra_queue_args|eqa|q   Extra arguments to pass to the queueing system for the scaffolding job.
  wait_job|wj              If specified, the scaffolder will not run until this job has finished.
  output|out|o             The output directory.
  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


