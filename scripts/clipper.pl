#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use QsTool;

# Tool constants
my $T_FASTX = "fastx";
my $T_NIZAR = "nizar";
my $DEF_TOOL = $T_NIZAR;

# Tool path constants
my $TP_FASTX = "fastx_clipper";
my $TP_NIZAR = "~droun/bin/length_extract_fasta";
my $DEF_TOOL_PATH = $TP_NIZAR;

# Sourceing constants
my $SOURCE_FASTX = "source fastx_toolkit-0.0.13;";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


# Parse generic queueing tool options
my $qst = new QsTool($DEF_TOOL, $DEF_TOOL_PATH);
$qst->parseOptions();


# Parse tool specific options
my %opt;
GetOptions (
        \%opt,
        'min_length|minlen=i',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


my $cmd_line = "";


# Validation
die "Error: Minimum length not specified.\n\n" unless $opt{config};


# Display configuration settings if requested.
print "\n\n" if $qst->isVerbose();
$qst->display() if $qst->isVerbose();
print "Minimum Length: " . $opt{minimum_length} . "\n\n" if $qst->isVerbose();


# Select the scaffolder and build the command line
my $tool = $qst->getTool();
if ($tool eq $T_FASTX) {
	$cmd_line = $FASTX_SOURCE_CMD . " " . $TP_FASTX . " -l " . $opt{min_length} . " -i " . $qst->getInput() . " -o " . $qst->getOutput();
	die "Error: FastX clipper does not work correctly yet!\n\n";
}
elsif ($tool eq $T_NIZAR) {
	$cmd_line = $TP_NIZAR . " " . $opt{min_length} . " " . $qst->getInput() . " > " . $qst->getOutput();
}
else {
	die "Error: Invalid tool requested.  Also, the script should not have got this far!!!.\n\n";
}


# Submit the scaffolding job
$qst->submit($cmd_line);



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

