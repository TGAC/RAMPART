#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use LsfJobSubmitter;
use QsOptions;


# Gap closing constants
my $T_GAP_CLOSER = "gapcloser";
my $T_IMAGE = "image";
my $T_GAP_FILLER = "gapfiller";
my $DEF_TOOL = $T_GAP_CLOSER;

my $TP_GAP_CLOSER = "GapCloser";
my $TP_IMAGE = "image";
my $TP_GAP_FILLER = "gapfiller";
my $DEF_TOOL_PATH = $TP_GC_PATH;

# Read length constants
my $DEF_READ_LENGTH = 155;

# Command constants
my $GC_SOURCE_CMD = "source GapCloser-1.12;";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->parseOptions();


# Parse tool specific options
my (%opt) = (  	"read_length",	$DEF_READ_LENGTH );

GetOptions (
        \%opt,
	'read_length|readlen|rl|r=i',
        'config|c=s',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


die "Error: No config file specified\n\n" unless $opt{config};


my $cmd_line = "";



# Display configuration settings if requested.
print "\n\n" if $qst->isVerbose();
$qst->display() if $qst->isVerbose();
print "Config: " . $opt{config} . "\n" if $qst->isVerbose();
print "Read Length: " . $opt{read_length} . "\n\n" if $qst->isVerbose();




# Select the gap closer and build the command line
my $tool = $qst->getTool();
if ($tool eq $T_GAP_CLOSER) {

	my $gc_scaffolds = $opt{output} . "/gc-scaffolds.fa";
	my $gc_other_args = "-p 61";

	$cmd_line = $GC_SOURCE_CMD . " " . $TP_GAP_CLOSER . " -a \"" . $qst->getInput() . "\" -b \"" . $opt{config} . "\" -o \"" . $gc_scaffolds . "\" -l " . $opt{read_length} . " " . $gc_other_args;
}
elsif ($tool eq $T_IMAGE) {

	my $image_scaffolds = $opt{output} . "/image-scaffolds.fa";
	$cmd_line = "";

	die "Error: IMAGE (Iterative Mapping and Assembly for Gap Elimination) tool not implemented in this script yet.\n\n";
}
elsif ($tool eq $T_GAP_FILLER) {

	my $gf_scaffolds = $opt{output} . "/gf-scaffolds.fa";
	$cmd_line = "";

	die "Error: Gap filler tool not implemented in this script yet.\n\n";
}
else {
        die "Error: Invalid gap closing tool requested.  Also, the script should not have got this far!!!.\n\n";
}

# Submit the scaffolding job
$qst->submit($cmd_line);


__END__

=pod

=head1 NAME

  degap.pl


=head1 SYNOPSIS

  degap.pl [options] -i <input_file> -c <config_file>

  input|in|i       The path to the input contigs file.
  config|cfg|c     The degapping library configuration file.

  For full documentation type: "degap.pl --man"


=head1 DESCRIPTION

  Runs a gap closing program in an attempt to fill in gaps within and around scaffolds.


=head1 OPTIONS

  output|out|o=s           The output directory.
  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

