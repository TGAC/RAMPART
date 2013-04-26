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
use AppStarter;

# Tool constants
my $T_SSPACE = "sspace";
my $T_GRASS = "grass";
my $DEF_TOOL = $T_SSPACE;

# Tool path constants
my $TP_SSPACE = "SSPACE_Basic_v2.0.pl";
my $TP_GRASS = "grass";
my $DEF_TOOL_PATH = $TP_SSPACE;

# Tool versions
my $T_SSPACE_VERSION = "2.0-Basic";
my $T_GRASS_VERSION = "x.x";

# Command constants
my $SSPACE_SOURCE_CMD = AppStarter::getAppInitialiser("SSPACE");
my $PERL_SOURCE_CMD = AppStarter::getAppInitialiser("PERL");

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;


# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->setMemory(30);
$qst->setThreads(8);
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
	
	my $in_file = $qst->getInput(); #convert_to_absolute($qst->getInput());
	my $out_file = $qst->getOutput(); #convert_to_absolute($qst->getOutput());	
	my $input_scaffolds = $out_file . "/input_scaffolds.fa";
	system("ln -s -f " . $in_file . " " . $input_scaffolds);
	
	my $sspace_output_prefix = "scaffolder";
	my $other_args = "-x 1";

	$cd = 1;

	my @sspace_args = (
		$PERL_SOURCE_CMD,
		$SSPACE_SOURCE_CMD,
		$TP_SSPACE,
		#"-l " . $sspace_cfg_file,
		"-l " . "sspace.cfg",
		"-s " . $input_scaffolds,
		$other_args,
		"-T " . $qst->getThreads(),
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
	
	for( my $i = 0; $i < $config->getNbSections(); $i++ ) {
		
		# Get info for this section
		my $lib = $config->getSectionAt($i);
		my $sect_name = $config->getSectionNameAt($i);
		
		# Only interested if this config section starts with "LIB"
		if ($sect_name =~ m/^LIB/) {
		
			if ($lib->{usage} eq "SCAFFOLDING" || $lib->{usage} eq "ASSEMBLY_AND_SCAFFOLDING") {
			
				my $file1 = $lib->{file_paired_1} ? $lib->{file_paired_1} : undef;
				my $file2 = $lib->{file_paired_2} ? $lib->{file_paired_2} : undef;
				
				# We expect to have a valid configuration file here so don't bother throwing
				# any errors from this point... sspace will error anyway if there is a problem.
						
				my @sspace_args = grep{$_} (
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
	}
	
	close OUT;
}

__END__

=pod

=head1 NAME

B<scaffolder.pl>


=head1 SYNOPSIS

B<scaffolder.pl> [options] B<--config> F<config.cfg> B<--input> F<assembly.fa>

For full documentation type: "scaffolder.pl --man"


=head1 DESCRIPTION

Runs a scaffolding tool to in an attempt to improve a given assembly.  This script is designed to execute jobs on a grid engine.


=head1 OPTIONS

=over

=item B<--config>

REQUIRED: The rampart library configuration file to use, which describes the paired end / mate pair reads which are to be used to improve the assembly.

=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported.  Default: LSF.

=item B<--tool>,B<-t>

Currently supported scaffolding tools: (sspace).  Default: sspace.

=item B<--tool_path>,B<--tp>

The path to the tool, or name of the tool's binary file if on the path.

=item B<--project_name>,B<--project>,B<-p>

The project name for the job that will be placed on the grid engine.

=item B<--job_name>,B<--job>,B<-j>

The job name for the job that will be placed on the grid engine.

=item B<--wait_condition>,B<--wait>,B<-w>

If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

=item B<--queue>,B<-q>

The queue to which this job should automatically be sent.

=item B<--memory>,B<--mem>,B<-m>

The amount of memory to reserve for this job.

=item B<--threads>,B<-n>

The number of threads that this job is likely to use.  This is used to reserve cores from the grid engine.

=item B<--extra_args>,B<--ea>

Any extra arguments that should be sent to the grid engine.

=item B<--input>,B<--in>,B<-i>

REQUIRED: The assembly to improve.

=item B<--output>,B<--out>,B<-o>

The output dir for this job. Default: Current working directory (".");

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut
