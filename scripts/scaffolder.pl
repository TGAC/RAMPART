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
use Configuration;

# Tool constants
my $T_SSPACE = "sspace";
my $T_GRASS = "grass";
my $DEF_TOOL = $T_SSPACE;

# Tool path constants
my $TP_SSPACE = "/common/software/SSPACE-BASIC-2.0/x86_64/bin/SSPACE_Basic_v2.0.pl";
my $TP_GRASS = "grass";
my $DEF_TOOL_PATH = $TP_SSPACE;

# Command constants
my $SSPACE_SOURCE_CMD = "source SSPACE-BASIC-2.0;";
my $PERL_SOURCE_CMD = "source perl-5.16.1;";

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
	'config|c=s',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


my $cmd_line = "";
my $cd = 0;


# Validation
die "Error: Config file not specified.\n\n" unless $opt{config};


# Display configuration settings if requested.
if($qst->isVerbose()) {
	print "\n\n" .
	$qst->toString() .
	"Config: " . $opt{config} . "\n\n";
}

my $tool = $qst->getTool();

# Select the scaffolder and build the command line
if ($tool eq $T_SSPACE) {

	my $rampart_cfg = new Configuration( $opt{config} );
	$rampart_cfg->validate();
	my $sspace_cfg_file = $qst->getOutput() . "/sspace.cfg";
	write_sspace_cfg( $rampart_cfg, $sspace_cfg_file);	
	
	my $sspace_output_prefix = "scaffolder";
	my $other_args = "-x 1 -T 2";

	$cd = 1;

	$cmd_line = $PERL_SOURCE_CMD . " " . $SSPACE_SOURCE_CMD . " " . $TP_SSPACE . " -l " . $sspace_cfg_file . " -s " . $qst->getInput() . " " . $other_args . " -b " . $sspace_output_prefix;

}
elsif ($tool eq $T_GRASS) {

	die "Error: Grass not implemented yet.\n\n";
}
else {
	die "Error: Invalid scaffolder requested.  Also, the script should not have got this far!!!.\n\n";
}


# Change dir to output directory if required for the specific tool
chdir $qst->getOutput() if $cd;

# Submit the scaffolding job
SubmitJob::submit($qst, $cmd_line);

# Change dir to original dir
chdir $PWD if $cd;

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"Scaffolder has successfully submitted the scaffolding job to the grid engine.  You will be notified by email when the job has completed.\n";
}



sub write_sspace_cfg {
	
	my ( $config, $out_file ) = @_;
	
	
	open OUT, ">", $out_file;
	
	for( my $i = 1; $i <= $config->getNbSections(); $i++ ) {
		
		my $lib = $config->getSectionAt($i-1);
		
		my $file1 = $lib->{q1} ? $lib->{q1} : $lib->{f1} ? $lib->{f1} : undef;
		my $file2 = $lib->{q2} ? $lib->{q2} : $lib->{f2} ? $lib->{f2} : undef;
		
		# We expect to have a valid configuration file here so don't bother throwing
		# any errors from this point... sspace will error anyway if there is a problem.
				
		my @sspace_args = (
			"LIB" . $i,
			$file1,
			$file2,
			$lib->{avg_ins},
			$lib->{ins_err},
			$lib->{reverse_seq} ? "FR" : "RF" 
		);
		
		my $line = join " ", @sspace_args;
		
		print OUT $line . "\n";
	}
	
	close OUT;
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


