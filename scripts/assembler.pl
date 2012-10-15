#!/usr/bin/perl

use strict;

use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';


# Now
my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;

# Assembler constants
my $A_ABYSS = "abyss";
my $DEF_ASSEMBLER = $A_ABYSS;

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

# Project constants
my $DEF_PROJECT_NAME = "AssemblerMultiKmer_" . $NOW;
my $DEF_JOB_PREFIX = $ENV{'USER'} . "-assembler-";

# Queueing system constants
my $SUBMIT = "bsub";
my $DEF_QUEUE_ARGS = "-q production";
my $ABYSS_SOURCE_CMD = "source abyss_upTo127-1.3.4;";

# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));



# Assign any command line options to variables
my (%opt) = (	"assembler", 		$DEF_ASSEMBLER,
		"job_prefix",		$DEF_JOB_PREFIX,
		"project", 		$DEF_PROJECT_NAME,
		"extra_queue_args",	$DEF_QUEUE_ARGS,
		"kmin", 		$DEF_KMER_MIN,
		"kmax", 		$DEF_KMER_MAX,
		"threads", 		$DEF_THREADS,
		"memory",		$DEF_MEM,
		"output", 		$PWD);

GetOptions (
	\%opt,
	'assembler|a=s',
	'job_prefix|job|j=s',
	'project|p=s',
	'extra_queue_args|eqa|q=s',
	'kmin=i',
	'kmax=i',
	'stats',
	'threads|t=i',
	'memory|mem|m=i',
	'output|out|o=s',
	'simulate|sim|s',
	'verbose|v',
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

die "Error: Invalid assembler requested.  Known assemblers are: 'abyss'.\n\n" unless ($opt{assembler} eq "abyss");

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



# Assembly job loop

my $project_arg = "-P" . $opt{project};
my $mem_mb = $opt{memory} * 1000;
my $j = 0;
for(my $i=$opt{kmin}; $i<=$opt{kmax};) {

	my $i_dir = $opt{output} . "/" . $i;
	my $job_name = $opt{job_prefix} . $i;
	my $job_arg = "-J" . $job_name;
	my $queue_arg = $opt{extra_queue_args};
	my $openmpi_arg = "-a openmpi";
	my $rusage_arg = "-R rusage[mem=" . $mem_mb . "] space[ptile=8]";
	my $threads_arg = "-n 8";
	my $bsub_args= $job_arg . " " . $project_arg . " " . $queue_arg . " " . $openmpi_arg . " " . $rusage_arg . " " . $threads_arg;
	my $cmd_line;
	
	if ($opt{assembler} eq $A_ABYSS) {
		my $abyss_bin = "abyss-pe";
		my $abyss_core_args = "n=10 mpirun=mpirun.lsf";
		my $abyss_threads = "np=" . $opt{threads};
		my $abyss_kmer = "k=" . $i;
		my $abyss_name = "name=Abyss-mpi-k" . $i;
		my $abyss_in = "in='" . $input_files . "'";
		$cmd_line = $ABYSS_SOURCE_CMD . " " . $abyss_bin . " " . $abyss_threads . " " . $abyss_core_args . " " . $abyss_kmer . " " . $abyss_name . " " . $abyss_in;
	}
	else {
		die "Error: Invalid assembler requested.  Also, the script should not have got this far!!!.\n\n";
	}

	system("mkdir", $i_dir) unless (-e $i_dir);
  	chdir $i_dir;

	print "Executing on cluster: " . $SUBMIT . " " . $bsub_args . " " . $QUOTE . $cmd_line . $QUOTE . "\n\n" if ($opt{verbose});
	system($SUBMIT, $job_arg, $project_arg, $queue_arg, $openmpi_arg, $rusage_arg, $threads_arg, $cmd_line) unless ($opt{simulate});

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
	my $sg_job_name = $opt{job_prefix} . "stat_gather";
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

