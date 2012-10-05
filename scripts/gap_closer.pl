#!/usr/bin/perl

use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;


# Now
my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;

# Project constants
my $DEF_PROJECT_NAME = "gap_closer_" . $NOW;
my $JOB_NAME = $ENV{'USER'} . "-gap_closer-" . $NOW;

# Gap closing constants
my $GC_GAP_CLOSER = "gapcloser";
my $DEF_GC = $GC_GAP_CLOSER;
my $DEF_GC_PATH = "GapCloser";

# Read length constants
my $DEF_READ_LENGTH = 155;

# Queueing system constants
my $SUBMIT = "bsub";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


my (%opt) = (   "gap_closer",   $DEF_GC,
                "gc_path",      $DEF_GC_PATH,
		"readlen",	$DEF_READ_LENGTH,
                "output",       $PWD );

GetOptions (
        \%opt,
        'gap_closer|gc|g=s',
	'gap_closer_path|gcp|g=s',
        'project|p=s',
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


die "Error: No input file specified\n\n" unless ($opt{input} && -e $opt{input});
die "Error: No output directory specified\n\n" unless ($opt{output} && -e $opt{output});
die "Error: No library config file specified\n\n" unless ($opt{config} && -e $opt{config});


if ($opt{verbose}) {
        print "Output Directory: ". $opt{output} . "\n";
        print "Project Name: " . $opt{project} . "\n";
}

my $job_arg = "-J " . $JOB_NAME;
my $project_arg = "-P " . $opt{project};
my $queue_arg = "-q production";
my $cmd_line = "";
my $wait_arg = "-w 'ended(\"" . $opt{wait_job} . "\")'" if $opt{wait_job};


# Select the gap closer and build the command line

if ($opt{gap_closer} eq $GC_GAP_CLOSER) {

	my $gc_exe = $DEF_GC_PATH;
	my $gc_scaffolds = $opt{$output} . "/gc_scaffolds.fa";
	$cmd_line = $gc_exe . " -a " . $opt{output} . " -b " . $opt{config} . " -o " . $gc_scaffolds . " -l " . $opt{readlen} . " -p 61";

	if (verbose) {
		print "\n";
		print "Running Gap Closer (will start after SSPACE has completed):\n";
		print " - GC: Config file: " . $opt{config} . "\n";
		print " - GC: SSPACE scaffold file: " . $opt{input} . "\n";
		print " - GC: GC scaffold location: " . $gc_scaffolds . "\n";
		print " - GC: Command line: " . $cmd_line . "\n";
	}

}
else {
        die "Error: Invalid gap closing tool requested.  Also, the script should not have got this far!!!.\n\n";
}

system($SUBMIT, $job_arg, $project_arg, $queue_arg, $wait_arg, $cmd_line);




__END__

=pod

=head1 NAME

  gap_closer.pl


=head1 SYNOPSIS

  gap_closer.pl [options] -i contigs_file -c config_file

  input|in|i       The path to the input contigs file.
  config|cfg|c     The scaffolder library configuration file.

  For full documentation type: "gap_closer.pl --man"


=head1 DESCRIPTION

  Runs a gap closing program in an attempt to fill in gaps within and around scaffolds.


=head1 OPTIONS

  gap_closer|gc|g  The gap closing tool to use.
  project|p        The project name for marking the LSF jobs.
  readlen|rl|r     The length of the reads used to build the assembly.
  wait_job|wj|w    If specified, gap closing will not start until this job is completed.
  output|out|o=s   The output directory.
  verbose|v        Print extra status information during run.
  help|usage|h|?   Print usage message and then exit.
  man              Display manual.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

