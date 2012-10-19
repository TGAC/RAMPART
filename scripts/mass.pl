#!/usr/bin/perl

use strict;
use warnings;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';
use QsOptions;


# Tool names
my $T_ABYSS = "abyss";
my $T_VELVET = "velvet";
my $T_SOAP = "soap";
my $DEF_TOOL = $T_ABYSS;

# Tool path constants
my $TP_ABYSS = "abyss-pe";
my $TP_VELVET = "velvet";
my $TP_SOAP = "soapdenovo";
my $DEF_TOOL_PATH = $TP_ABYSS;

# Kmer constants
my $KMER_MIN = 11;
my $KMER_MAX = 125;
my $DEF_KMER_MIN = 41;
my $DEF_KMER_MAX = 95;

# Threads constants
my $DEF_THREADS=8;

# Mem constants
my $DEF_MEM=60;
my $MIN_MEM=5;

# Source command constants
my $ABYSS_SOURCE_CMD = "source abyss_upTo127-1.3.4;";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));
my $MASS_GP_PATH = $RAMPART_DIR . "mass_gp.pl";


# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->setTool($DEF_TOOL);
$qst->setToolPath($DEF_TOOL_PATH);
$qst->setMemory(60);
$qst->setThreads(8);
$qst->parseOptions();


# Assign any command line options to variables
my (%opt) = (	"kmin", 		$DEF_KMER_MIN,
		"kmax", 		$DEF_KMER_MAX);

GetOptions (
	\%opt,
	'kmin=i',
	'kmax=i',
	'stats',
	'simulate|sim|s',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


# Get input files
my @in_files = @ARGV;
my $input_files = join " ", @in_files;

print "\n";
print "Command line arguments gathered\n\n" if $opt{verbose};


# Argument Validation

die "Error: No input files specified\n\n" unless @in_files;
foreach(@in_files) {
        die "Error: Input file does not exist: " . $_ . "\n\n" unless (-e $_);
}

die "Error: K-mer limits must be >= " . $KMER_MIN . "nt\n\n" unless ($opt{kmin} >= $KMER_MIN && $opt{kmax} >= $KMER_MIN);
die "Error: K-mer limits must be <= " . $KMER_MAX . "nt\n\n" unless ($opt{kmin} <= $KMER_MAX && $opt{kmax} <= $KMER_MAX);
die "Error: Min K-mer value must be <= Max K-mer value\n\n" unless ($opt{kmin} <= $opt{kmax});
die "Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.\n\n" unless (validKmer($opt{kmin}) && validKmer($opt{kmax}));

die "Error: Invalid number of cores requested.  Must request at least 1 core per assembly.\n\n" unless ($qst->getThreads() >= 1);

die "Error: Invalid memory setting.  Must request at least " . $MIN_MEM . "GB.\n\n" unless ($qst->getMemoryGB() >= $MIN_MEM);


print "Validated arguments\n\n" if $qst->isVerbose();
print "Input files: " . $input_files . "\n" if $qst->isVerbose();
if ($opt{verbose}) {
	print "Options:\n";
	foreach (keys %opt) {
		print "\t'$_' => " . $opt{$_} . "\n";
	}
	print "\n";
}



# Build up static args which is to be used by all child jobs
my $queueing_system = $qst->getQueueingSystem() ? "--queueing_system " . $qst->getQueueingSystem() : "";
my $project_arg = $qst->getProjectName() ? "--project_name " . $qst->getProjectName() : "";
my $queue_arg = $qst->getQueue() ? "--queue " . $qst->getQueue : "";
my $extra_args = $qst->getExtraArgs() ? "--extra_args \"" . $qst->getExtraArgs . "\"" : "";
my $verbose_arg = $qst->isVerbose() ? "--verbose" : "";
my $static_args = $queueing_system . " " . $project_arg . " " . $extra_args . " " . $queue_arg . " " . $verbose_arg;


# These variables get varied for each run.
my $job_prefix = $qst->getJobName();
my $output_dir = $qst->getOutput();


# Assembly job loop

my $tool = $qst->getTool();
my $j = 0;


for(my $i=$opt{kmin}; $i<=$opt{kmax};) {

	my $i_dir = $output_dir . "/" . $i;
	my $job_name = $job_prefix . "k" . $i;

	my $qst_ass = new QsTool();
	$qst_ass->setQueueingSystem($qst->getQueueingSystem());
	$qst_ass->setTool($qst->getTool());
	$qst_ass->setToolPath($qst->getToolPath());
	$qst_ass->setProjectName($qst->getProjectName());
	$qst_ass->setQueue($qst->getQueue());
	$qst_ass->setMemory($qst->getMemoryGB());
	$qst_ass->setThreads($qst->getThreads());
	$qst_ass->setVerbose($qst->isVerbose());

	$qst_ass->setOutput($i_dir);
	$qst_ass->setJobName($job_name);


	my $cmd_line;
	
	if ($tool eq $T_ABYSS) {
		my $abyss_core_args = "n=10 mpirun=mpirun.lsf";
		my $abyss_threads = "np=" . $qst_ass->getThreads();
		my $abyss_kmer = "k=" . $i;
		my $abyss_out_prefix = "name=Abyss-mpi-k" . $i;
		my $abyss_in = "in='" . $input_files . "'";
		$cmd_line = $ABYSS_SOURCE_CMD . " " . $TP_ABYSS . " " . $abyss_threads . " " . $abyss_core_args . " " . $abyss_kmer . " " . $abyss_out_prefix . " " . $abyss_in;
	}
	elsif ($tool eq $T_VELVET) {
		die "Error: Velvet not implemented yet\n\n";
	}
	elsif ($tool eq $T_SOAP) {
		die "Error: SOAP de novo not implemented yet\n\n";
	}
	else {
		die "Error: Invalid assembler requested.  Also, the script should not have got this far!!!.\n\n";
	}

	# Make the output directory for this child job and go into it
	system("mkdir", $i_dir) unless (-e $i_dir);
  	chdir $i_dir;

	# Submit the job
	$qst_ass->submit($cmd_line) unless $opt{simulate};

	if ($j % 2) {
		$i += 6;
	}
	else {
		$i += 4;
	}
	$j++;
}


# Change back to the original directory before exiting.

chdir $PWD;


# If requested, produce statistics and graphs for this run
if ($opt{stats}) {

	my $gp_qs_arg = "--queueing_system " . $qst->getQueueingSystem();
	my $gp_tool_arg = "--tool mass_gp";
	my $gp_project_arg = "--project " . $qst->getProjectName();
	my $gp_queue_arg = "--queue " . $qst->getQueue();
	my $gp_wc_arg = "--wait_condition " . "'done(" . $job_prefix . "k*)'"; # This presumes an LSF wait condition.
	my $gp_job_arg = "--job_name " . $job_prefix . "stats";
	my $gp_verbose_arg = $qst->isVerbose() ? "--verbose" : "";
	my $gp_input_arg = "--input " .  $qst->getOutput();
	my $gp_output_arg = "--output " . $qst->getOutput();

	my $mgp_cmd_line = $MASS_GP_PATH . " " . $gp_qs_arg . " " . $gp_tool_arg . " " . $gp_project_arg . " " . $gp_queue_arg . " " . $gp_wc_arg . " " . $gp_job_arg . " " . $gp_verbose_arg . " " . $gp_input_arg . " " . $gp_output_arg;

	system($mgp_cmd_line);
}



# Script finished successfully... but the jobs will still be running

exit 0;



sub validKmer {
	my $val_in = $_[0];

	my $mod1 = ($val_in - 1) % 10;
	my $mod2 = ($val_in - 5) % 10;

	if ($mod1 == 0 || $mod2 == 0 ) {
		return 1;
	}
	else {
		return 0;
	}
}

__END__

=pod

=head1 NAME

  mass.pl


=head1 SYNOPSIS

  mass.pl [options] <input_files>

  For full documentation type: "mass.pl --man"


=head1 DESCRIPTION

  Runs an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.


=head1 OPTIONS

  --queueing_system      --qs
              The queueing system to use.  Currently "LSF" and "PBS" are supported.

  --tool                 -t
              If this script supports multiple tools to do the same job you can specify that tool using this parameter.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

  --project_name         --project           -p
              The project name for the job that will be placed on the queueing system.

  --job_name             --job               -j
              The job name for the job that will be placed on the queueing system.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --memory               --mem               -m
              The amount of memory to reserve for this job.

  --threads              -n
              The number of threads that this job is likely to use.  This is used to reserve cores from the queueing system.

  --extra_args           --ea
              Any extra arguments that should be sent to the queueing system.

  --input                --in                -i
              The input file(s) for this job.

  --output               --out               -o
              The output file/dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.

  --kmin
              The minimum k-mer value to run.

  --kmax
              The maximum k-mer value in run.

  --stats
              Produces output statistics and graphs comparing each assembly job produced.

  --simulate             --sim              -s
              Runs the script as normal except that the assembly jobs are not submitted.

  --help                 --usage            -h             -?
              Print usage message and then exit.

  --man
              Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

