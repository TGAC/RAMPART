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

# Gap closing constants
my $T_GAP_CLOSER = "gapcloser";
my $T_IMAGE      = "image";
my $T_GAP_FILLER = "gapfiller";
my $DEF_TOOL     = $T_GAP_CLOSER;

my $TP_GAP_CLOSER = "GapCloser";
my $TP_IMAGE      = "image";
my $TP_GAP_FILLER = "gapfiller";
my $DEF_TOOL_PATH = $TP_GAP_CLOSER;

# Read length constants
my $DEF_READ_LENGTH = 155;

# Command constants
my $GC_SOURCE_CMD = "source GapCloser-1.12;";

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->parseOptions();

# Parse tool specific options
my (%opt) = ( "read_length", $DEF_READ_LENGTH );

GetOptions( \%opt, 'read_length|readlen|rl|r=i', 'config|c=s', 'help|usage|h|?',
	'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

die "Error: No config file specified\n\n" unless $opt{config};

my $cmd_line = "";

# Display configuration settings if requested.
if ($qst->isVerbose()) {
	print 	"\n\n" .
			$qst->toString() .
			"Config: " . $opt{config} . "\n" .
			"Read Length: " . $opt{read_length} . "\n\n";
}

# Select the gap closer and build the command line
my $tool = $qst->getTool();
if ( $tool eq $T_GAP_CLOSER ) {

	my $rampart_cfg = new Configuration( $opt{config} );
	$rampart_cfg->validate();
	my $gc_cfg_file = "gc.cfg";
	write_sspace_cfg( $rampart_cfg, $gc_cfg_file);	
	
	my $gc_scaffolds  = $qst->getOutput() . "/gc-scaffolds.fa";
	my $gc_other_args = "-p 61";

	$cmd_line =
	    $GC_SOURCE_CMD . " "
	  . $TP_GAP_CLOSER
	  . " -a \""
	  . $qst->getInput()
	  . "\" -b \""
	  . $gc_cfg_file
	  . "\" -o \""
	  . $gc_scaffolds
	  . "\" -l "
	  . $opt{read_length} . " "
	  . $gc_other_args;
}
elsif ( $tool eq $T_IMAGE ) {

	my $image_scaffolds = $qst->getOutput() . "/image-scaffolds.fa";
	$cmd_line = "";

	die "Error: IMAGE (Iterative Mapping and Assembly for Gap Elimination) tool not implemented in this script yet.\n\n";
}
elsif ( $tool eq $T_GAP_FILLER ) {

	my $gf_scaffolds = $opt{output} . "/gf-scaffolds.fa";
	$cmd_line = "";

	die "Error: Gap filler tool not implemented in this script yet.\n\n";
}
else {
	die "Error: Invalid gap closing tool requested.  Also, the script should not have got this far!!!.\n\n";
}

# Submit the scaffolding job
SubmitJob::submit($qst, $cmd_line);

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"Degap has successfully submitted the degapping job to the grid engine.  You will be notified by email when the job has completed.\n";
}



sub write_soap_cfg {
	
	my ( $config, $out_file ) = @_;
	
	
	open OUT, ">", $out_file;
	
	for( my $i = 1; $i <= $config->getNbSections(); $i++ ) {
		
		my $lib = $config->getSectionAt($i-1);
		
		my $ft = $lib->{q1};
		my $file1 = $lib->{q1} ? $lib->{q1} : $lib->{f1} ? $lib->{f1} : undef;
		my $file2 = $lib->{q2} ? $lib->{q2} : $lib->{f2} ? $lib->{f2} : undef;
		
		# We expect to have a valid configuration file here so don't bother throwing
		# any errors from this point... sspace will error anyway if there is a problem.
				
		my @soap_args = (
			"[LIB]",
			"max_rd_len=" . $lib->{max_rd_len},
			"avg_ins=" . $lib->{avg_ins},
			"reverse_seq=" . $lib->{reverse_seq},
			"asm_flags=3",
			"rank=1",
			($ft ? "q1=" : "f1=") . $file1,
			($ft ? "q2=" : "f2=") . $file2
		);
		
		my $line = join "\n", @soap_args;
		
		print OUT $line . "\n";
	}
	
	close OUT;
}



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

