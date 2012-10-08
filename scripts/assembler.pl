#!/usr/bin/perl

use Getopt::Long;
use Pod::Usage;
use File::Basename;
use Cwd;

my %args;

# Now
($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$NOW = $year . $mon . $mday . "_" . $hr . $min . $sec;

# Assembler constants
$A_ABYSS = "abyss";
$DEF_ASSEMBLER = $A_ABYSS;

# Kmer constants
$KMER_MIN = 11;
$KMER_MAX = 95;
$DEF_KMER_MIN = 41;
$DEF_KMER_MAX = 95;

# Threads constants
$DEF_THREADS=8;

# Mem constants
$DEF_MEM=60;
$MIN_MEM=5;

# Project constants
$DEF_PROJECT_NAME = "AssemblerMultiKmer_" . $NOW;
$JOB_PREFIX = $ENV{'USER'} . "-assembler-";

# Queueing system constants
$SUBMIT = "bsub";

# Other constants
$QUOTE = "\"";
$PWD = getcwd;


# Assign any command line options to variables
my (%opt) = (	"assembler", 	$DEF_ASSEMBLER,
		"job_prefix",	$DEF_JOB_PREFIX,
		"project", 	$DEF_PROJECT_NAME,
		"kmin", 	$DEF_KMER_MIN,
		"kmax", 	$DEF_KMER_MAX,
		"threads", 	$DEF_THREADS,
		"memory",	$DEF_MEM,
		"output", 	$PWD);

GetOptions (
	\%opt,
	'assembler|a=s',
	'job_prefix|job|j=s',
	'project|p=s',
	'kmin=i',
	'kmax=i',
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
@in_files = @ARGV;
$input_files = join " ", @in_files;

print "\n";
print "Command line arguments gathered\n\n" if $opt{verbose};


# Argument Validation

die "Error: No input files specified\n\n" . $USAGE unless @in_files;
foreach(@in_files) {
        die "Error: Input file does not exist: " . $_ . "\n\n" . $USAGE unless (-e $_);
}

die "Error: No output directory specified\n\n" . $USAGE unless $opt{output};

die "Error: K-mer limits must be >= " . $KMER_MIN . "nt\n\n" . $USAGE unless ($opt{kmin} >= $KMER_MIN && $opt{kmax} >= $KMER_MIN);
die "Error: K-mer limits must be <= " . $KMER_MAX . "nt\n\n" . $USAGE unless ($opt{kmin} <= $KMER_MAX && $opt{kmax} <= $KMER_MAX);
die "Error: Min K-mer value must be <= Max K-mer value\n\n" . $USAGE unless ($opt{kmin} <= $opt{kmax});
die "Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.\n\n" . $USAGE unless (validKmer($opt{kmin}) && validKmer($opt{kmax}));

die "Error: Invalid assembler requested.  Known assemblers are: 'abyss'.\n\n" . $USAGE unless ($opt{assembler} eq "abyss");

die "Error: Invalid number of cores requested.  Must request at least 1 core per assembly.\n\n" .$USAGE unless ($opt{threads} >= 1);

die "Error: Invalid memory setting.  Must request at least " . $MIN_MEM . "GB.\n\n" . $USAGE unless ($opt{memory} >= $MIN_MEM);


print "Validated arguments\n\n" if $opt{verbose};
print "Input files: " . $in_files . "\n" if $opt{verbose};
if ($opt{verbose}) {
	print "Options:\n";
	foreach (keys %opt) {
		print "\t'$_' => " . $opt{$_} . "\n";
	}
	print "\n";
}



# Assembly job loop

$mem_mb = $opt{memory} * 1000;
$j = 0;
for($i=$opt{kmin}; $i<=$opt{kmax};) {

	$i_dir = $opt{output} . "/" . $i;
	$job_name = $opt{job_prefix} . $i;
	$job_arg = "-J " . $job_name;
	$project_arg = "-P " . $opt{project};
	$queue_arg = "-q production";
	$openmpi_arg = "-a openmpi";
	$rusage_arg = "-R rusage[mem=" . $mem_mb . "] space[ptile=" . $opt{threads} . "]";
	$threads_arg = "-n 8";
	$bsub_args= $job_arg . " " . $project_arg . " " . $queue_arg . " " . $openmpi_arg . " " . $rusage_arg . " " . $threads_arg;
	
	if ($opt{assembler} eq $A_ABYSS) {
		$abyss_bin = "abyss-pe";
		$abyss_core_args = "n=10 mpirun=mpirun.lsf";
		$abyss_threads = "np=" . $opt{threads};
		$abyss_kmer = "k=" . $i;
		$abyss_name = "name=Abyss-mpi-k" . $i;
		$abyss_in = "in='" . $input_files . "'";
		$cmd = $abyss_bin . " " . $abyss_threads . " " . $abyss_core_args . " " . $abyss_kmer . " " . $abyss_name . " " . $abyss_in;
	}
	else {
		die "Error: Invalid assembler requested.  Also, the script should not have got this far!!!.\n\n" . $USAGE;
	}

	system("mkdir", $i_dir) unless (-e $i_dir);
  	chdir $i_dir;

	print "Executing on cluster: " . $bsub . " " . $bsub_args . " " . $QUOTE . $cmd . $QUOTE . "\n\n" if ($opt{verbose});
	system($SUBMIT, $job_arg, $project_arg, $queue_arg, $openmpi_arg, $rusage_arg, $threads_arg, $cmd) unless ($opt{simulate});

	if ($j % 2) {
		$i += 6;
	}
	else {
		$i += 4;
	}
	$j++;
}


# Change back to the original directory before exiting.

chdir $pwd;


# Script finished successfully... but the jobs 

exit 0;



sub validKmer {
	$val_in = $_[0];

	$mod1 = ($val_in - 1) % 10;
	$mod2 = ($val_in - 5) % 10;

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

  assembler|a      The assembly program to use.
  job_prefix|job|j The prefix string for each job.
  project|p        The project name for marking the LSF jobs.
  kmin             The minimum k-mer value to run.
  kmax             The maximum k-mer value in run.
  threads|t        The number of threads each assembly job should use.
  memory|mem|m     The amount of memory each assembly job should use in GB.
  output|out|o=s   The output directory.
  simulate|sim|s   Runs the script as normal except that the assembly jobs are not submitted.
  verbose|v        Print extra status information during run.
  help|usage|h|?   Print usage message and then exit.
  man              Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

