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
use File::Basename;
use Cwd;

# RAMPART modules
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
	
	my $in_file = convert_to_absolute($qst->getInput());
	my $out_file = convert_to_absolute($qst->getOutput());	
	system("ln -s " . $in_file . " " . $out_file . "/input_scaffolds.fa;");
	
	my $sspace_output_prefix = "scaffolder";
	my $other_args = "-x 1 -T 2";

	$cd = 1;

	my @sspace_args = (
		$PERL_SOURCE_CMD,
		$SSPACE_SOURCE_CMD,
		$TP_SSPACE,
		#"-l " . $sspace_cfg_file,
		"-l " . "sspace.cfg",
		"-s " . "input_scaffolds.fa",
		$other_args,
		"-b " . $sspace_output_prefix
	);
	
	$cmd_line = join " ", @sspace_args;
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


sub convert_to_absolute {
	my ( $file ) = shift;
	
	system("case " . $file. " in
     /*) absolute=1 ;;
     *) absolute=0 ;;
	esac");
	
	my $abs_file;
	if ($? == 1) {
		$abs_file = $file;
	}
	else {
		$abs_file = $PWD . "/" . $file;
	}
		
	return $abs_file;
}

sub write_sspace_cfg {
	
	my ( $config, $out_file ) = @_;
	
	
	open OUT, ">", $out_file;
	
	for( my $i = 1; $i < $config->getNbSections(); $i++ ) {
		
		my $lib = $config->getSectionAt($i);
		
		if ($lib->{usage} eq "SCAFFOLDING" || $lib->{usage} eq "ASSEMBLY_AND_SCAFFOLDING") {
		
			my $file1 = $lib->{file_paired_1} ? $lib->{file_paired_1} : undef;
			my $file2 = $lib->{file_paired_2} ? $lib->{file_paired_2} : undef;
			
			# We expect to have a valid configuration file here so don't bother throwing
			# any errors from this point... sspace will error anyway if there is a problem.
					
			my @sspace_args = (
				"LIB" . $i,
				$file1,
				$file2,
				$lib->{avg_insert_size},
				$lib->{insert_err_tolerance},
				$lib->{seq_orientation} 
			);
			
			my $line = join " ", @sspace_args;
			
			print OUT $line . "\n";
		}
	}
	
	close OUT;
}

__END__

=pod

=head1 NAME

  scaffolder.pl


=head1 SYNOPSIS

  scaffolder.pl [options] --config <config_file> -i <input_assembly>

  For full documentation type: "scaffolder.pl --man"


=head1 DESCRIPTION

  Runs a scaffolding tool to in an attempt to improve a given assembly.  This script is designed to execute jobs on a grid engine.


=head1 OPTIONS

  --config
              REQUIRED: The rampart library configuration file to use, which describes the paired end / mate pair reads which are to be used to improve the assembly.

  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

  --tool                 -t
              Currently supported scaffolding tools: (sspace).  Default: sspace.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

  --project_name         --project           -p
              The project name for the job that will be placed on the grid engine.

  --job_name             --job               -j
              The job name for the job that will be placed on the grid engine.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --memory               --mem               -m
              The amount of memory to reserve for this job.

  --threads              -n
              The number of threads that this job is likely to use.  This is used to reserve cores from the grid engine.

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --input                --in                -i
              REQUIRED: The assembly to improve.

  --output               --out               -o
              The output dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

