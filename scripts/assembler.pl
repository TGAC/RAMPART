#!/usr/bin/perl

use Getopt::Long;
use File::Basename;
use Cwd;

my %args;

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

# Other constants
$QUOTE = "\"";
$USAGE="\nassembler [-help] [-p project_name] [-kmin val] [-kmax val] [-a assembler] [-t threads] -i input_file -o output_dir -- script to run an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.  Requests 60GB RAM per assembly.\n\n" . 
"where:\n" .
"   -help  show this help text\n" .
"   -a     assembler.  Valid options: " . $A_ABYSS . "... actually it's just abyss so far! (default = " . $DEF_ASSEMBLER . ")\n" .
"   -p     project name for marking the LSF jobs\n" .
"   -kmin  minimum k-mer value in run.  Constraints: " . $KMER_MIN . " <= kmin <= " . $KMER_MAX . "; Last digit must be a '1' or a '5'; Must be <= -kmax. (default = " . $DEF_KMER_MIN . ")\n" .
"   -kmax  maximum k-mer value in run. Constraints: " . $KMER_MIN . " <= kmax <= " . $KMER_MAX . "; Last digit must be a '1' or a '5'; Must be >= -kmin. (default = " . $DEF_KMER_MAX . ")\n" .
"   -t     number of threads (default = " . $DEF_THREADS . ")\n" .
"   -m     memory usage in GB (default = " . $DEF_MEM . "GB)\n" .
"   -i     list of input files to assemble\n" .
"   -o     output directory\n\n";

# TODO consider using POD2USAGE to handle help information instead.



# Main script variables

$job_prefix = $ENV{'USER'} . "-assembler-";
$assembler = "abyss";
($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$project_name = "AssemblerMultiKmer_" . $year . $mon . $mday . "_" . $hr . $min . $sec;
$in_files;
$out_dir;
$help = 0;
$kmin = $DEF_KMER_MIN;
$kmax = $DEF_KMER_MAX;
$threads = $DEF_THREADS;
$mem = $DEF_MEM;
$verbose = 0;




# Assign any command line options to variables

$result = GetOptions (	"a=s"    => \$assembler,
			"p=s"    => \$project_name,
			"kmin=i" => \$kmin,
			"kmax=i" => \$kmax,
			"t=i"    => \$threads,
			"m=i"    => \$mem,
			"i=s"    => \$in_files,
			"o=s"    => \$out_dir,
			"v"      => \$verbose,
			"help"   => \$help );



# Print usage information if requested

if ($help) {
	print $USAGE;
	exit 0;
}


# Argument Validation

die "Error: No input files specified\n\n" . $USAGE unless $in_files;
die "Error: No output directory specified\n\n" . $USAGE unless $out_dir;

die "Error: K-mer limits must be >= " . $KMER_MIN . "nt\n\n" . $USAGE unless ($kmin >= $KMER_MIN && $kmax >= $KMER_MIN);
die "Error: K-mer limits must be <= " . $KMER_MAX . "nt\n\n" . $USAGE unless ($kmin <= $KMER_MAX && $kmax <= $KMER_MAX);
die "Error: Min K-mer value must be <= Max K-mer value\n\n" . $USAGE unless ($kmin <= $kmax);

die "Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.\n\n" . $USAGE unless (validKmer($kmin) && validKmer($kmax));

die "Error: Invalid assembler requested.  Known assemblers are: 'abyss'.\n\n" . $USAGE unless ($assembler eq "abyss");

die "Error: Invalid number of cores requested.  Must request at least 1 core per assembly.\n\n" .$USAGE unless ($threads >= 1);

die "Error: Invalid memory setting.  Must request at least " . $MIN_MEM . "GB.\n\n" .$USAGE unless ($mem >= $MIN_MEM);

# Record current working directory.  The cwd gets restored after the job loop has completed.

$pwd = getcwd;
$mem_mb = $mem * 1000;


# Assembly job loop

$j = 0;
for($i=$kmin; $i<=$kmax;) {

  	$i_dir = $out_dir . "/" . $i;
	$job_name = $job_prefix . $i;
	
	$bsub = "bsub";
	$job_arg = "-J " . $job_name;
	$project_arg = "-P " . $project_name;
	$queue_arg = "-q production";
	$openmpi_arg = "-a openmpi";
	$rusage_arg = "-R rusage[mem=" . $mem_mb . "] space[ptile=" . $abyss_threads . "]";
	$threads_arg = "-n 8";
	$bsub_args= $job_arg . " " . $project_arg . " " . $queue_arg . " " . $open_mpi_arg . " " . $rusage_arg . " " . $threads_arg;
	
	if ($assembler eq $A_ABYSS) {
		$abyss_bin = "abyss-pe";
		$abyss_core_args = "n=10 mpirun=mpirun.lsf";
		$abyss_threads = "np=" . $threads;
		$abyss_kmer = "k=" . $i;
		$abyss_name = "name=Abyss-mpi-k" . $i;
		$abyss_in = "in='" . $in_files . "'";
		$cmd = $abyss_bin . " " . $abyss_threads . " " . $abyss_core_args . " " . $abyss_kmer . " " . $abyss_name . " " . $abyss_in;
	}
	else {
		die "Error: Invalid assembler requested.  Also, the script should not have got this far!!!.\n\n" . $USAGE;
	}

	system("mkdir", $i_dir);
  	chdir $i_dir;

	if ($verbose) {
		print "Executing on cluster: " . $bsub . " " . $bsub_args . " " . $QUOTE . $cmd . $QUOTE . "\n\n";
	}

	system($bsub, $job_arg, $project_arg, $queue_arg, $openmpi_arg, $rusage_arg, $threads_arg, $cmd);

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
