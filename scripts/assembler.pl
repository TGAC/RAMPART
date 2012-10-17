#!/usr/bin/perl

use strict;

use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';
use QsTool;


# Tool names
my $T_ABYSS = "abyss";
my $T_VELVET = "velvet";
my $T_SOAP = "soap";
my $DEF_TOOL = $A_ABYSS;

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


# Parse generic queueing tool options
my $qst = new QsTool();
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

die "Error: No output directory specified\n\n" unless $opt{output};

die "Error: K-mer limits must be >= " . $KMER_MIN . "nt\n\n" unless ($opt{kmin} >= $KMER_MIN && $opt{kmax} >= $KMER_MIN);
die "Error: K-mer limits must be <= " . $KMER_MAX . "nt\n\n" unless ($opt{kmin} <= $KMER_MAX && $opt{kmax} <= $KMER_MAX);
die "Error: Min K-mer value must be <= Max K-mer value\n\n" unless ($opt{kmin} <= $opt{kmax});
die "Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.\n\n" unless (validKmer($opt{kmin}) && validKmer($opt{kmax}));

die "Error: Invalid number of cores requested.  Must request at least 1 core per assembly.\n\n" unless ($opt{threads} >= 1);

die "Error: Invalid memory setting.  Must request at least " . $MIN_MEM . "GB.\n\n" unless ($opt{memory} >= $MIN_MEM);


print "Validated arguments\n\n" if $opt{verbose};
print "Input files: " . $input_files . "\n" if $opt{verbose};
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
	$qst->setOutput($i_dir);

	my $job_name = $job_prefix . $i;
	$qst->setJobName($job_name);

	my $cmd_line;
	
	if ($tool eq $A_ABYSS) {
		my $abyss_core_args = "n=10 mpirun=mpirun.lsf";
		my $abyss_threads = "np=" . $qst->getThreads();
		my $abyss_kmer = "k=" . $i;
		my $abyss_out_prefix = "name=Abyss-mpi-k" . $i;
		my $abyss_in = "in='" . $input_files . "'";
		$cmd_line = $ABYSS_SOURCE_CMD . " " . $TP_ABYSS . " " . $abyss_threads . " " . $abyss_core_args . " " . $abyss_kmer . " " . $abyss_out_prefix . " " . $abyss_in;
	}
	elsif ($tool eq $T_VELVET) {
		die "Error: Velvet not implemented yet\n\n";
	}
	else ($tool eq $T_SOAP) {
		die "Error: SOAP de novo not implemented yet\n\n";
	}
	else {
		die "Error: Invalid assembler requested.  Also, the script should not have got this far!!!.\n\n";
	}

	# Make the output directory for this child job and go into it
	system("mkdir", $i_dir) unless (-e $i_dir);
  	chdir $i_dir;

	# Submit the job
	$qst->submit($cmd_line) unless $opt{simulate};

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

	my $stats_gatherer_path = $RAMPART_DIR . "/assembly_stats_gatherer.pl";
        my $sg_wait_arg = "-w 'done(" . $opt{job_prefix} . "*)'";
	my $sg_job_name = $job_prefix . "stat_gather";
        my $sg_job_arg = "-J" . $sg_job_name;
	my $stat_file = $opt{output} . "/stats.txt";
        my $sg_cmd_line = $stats_gatherer_path . " " . $opt{output} . " > " . $stat_file;

	if ($opt{simulate}) {
		print "Executing stat gatherer on cluster immediately.\n\n" if ($opt{verbose});
		system($SUBMIT, $project_arg, $sg_job_arg, $opt{extra_queue_args}, $sg_cmd_line);
	}
	else {
		print "Will execute stat gatherer on cluster after assemblies have completed.\n\n" if ($opt{verbose});
		system($SUBMIT, $project_arg, $sg_job_arg, $sg_wait_arg, $opt{extra_queue_args}, $sg_cmd_line);
	}

	my $stats_plotter_path = $RAMPART_DIR . "/assembly_stats_plotter.pl";
	my $sp_job_name = $opt{job_prefix} . "stat_plot";
	my $sp_job_arg = "-J" . $sp_job_name;
	my $sp_wait_arg = "-w 'done(" . $sg_job_name . ")'";
	my $sp_cmd_line = $stats_plotter_path . " " . $stat_file . " > stat_plotter.rout";

	print "Will execute stat plotter after stat gatherer has completed.\n\n" if ($opt{verbose});
	system($SUBMIT, $project_arg, $sp_job_arg, $sp_wait_arg, $opt{extra_queue_args}, $sp_cmd_line);
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

  assembler.pl


=head1 SYNOPSIS

  assembler.pl [options] <input_files>

  For full documentation type: "assembler.pl --man"


=head1 DESCRIPTION

  Runs an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.


=head1 OPTIONS

  assembler|a              The assembly program to use.
  job_prefix|job|j         The prefix string for each job.
  project|p                The project name for marking the LSF jobs.
  extra_queue_args|eqa|q   Extra arguments to pass to the queueing system for each assembly job.
  kmin                     The minimum k-mer value to run.
  kmax                     The maximum k-mer value in run.
  stats                    Produces output statistics and graphs comparing each assembly job produced.
  threads|t                The number of threads each assembly job should use.
  memory|mem|m             The amount of memory each assembly job should use in GB.
  output|out|o=s           The output directory.
  simulate|sim|s           Runs the script as normal except that the assembly jobs are not submitted.
  verbose|v                Print extra status information during run.
  help|usage|h|?           Print usage message and then exit.
  man                      Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

