#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use QsOptions;
use SubmitJob;

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
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
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
die "Error: Minimum length not specified.\n\n" unless $opt{min_length};


# Display configuration settings if requested.
if ($qst->isVerbose()) {
	print	"\n\n" .
			$qst->toString() .
			"Minimum Length: " . $opt{min_length} . "\n\n";
}


# Select the scaffolder and build the command line
my $tool = $qst->getTool();
if ($tool eq $T_FASTX) {
	$cmd_line = $SOURCE_FASTX . " " . $TP_FASTX . " -l " . $opt{min_length} . " -i " . $qst->getInput() . " -o " . $qst->getOutput();
	die "Error: FastX clipper does not work correctly yet!\n\n";
}
elsif ($tool eq $T_NIZAR) {
	$cmd_line = $TP_NIZAR . " " . $opt{min_length} . " " . $qst->getInput() . " > " . $qst->getOutput();
}
else {
	die "Error: Invalid tool requested.  Also, the script should not have got this far!!!.\n\n";
}


# Submit the scaffolding job
SubmitJob::submit($qst, $cmd_line);

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"Clipper has successfully submitted the clip job to the grid engine.  You will be notified by email when the clip job has completed.\n";
}

__END__

=pod

=head1 NAME

  clipper.pl


=head1 SYNOPSIS

  clipper.pl [options] --minlen <minimum_length> -i <input_file>

  min_length|minlen    The minimum length for scaffolds to be kept in the output file.
  input|in|i           The path to the input scaffolds file.

  For full documentation type: "clipper.pl --man"


=head1 DESCRIPTION

  Runs a clipping program that removes scaffolds less than a user specified length.


=head1 OPTIONS

  output|out|o             The output directory.
  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

