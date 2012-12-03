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
use Cwd;
use Cwd 'abs_path';
use File::Basename;

# RAMPART modules
use QsOptions;
use SubmitJob;
use Configuration;
use AppStarter;

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

# Tool versions
my $T_ABYSS_VERSION = "1.3.4";
my $T_VELVET_VERSION = "x.x";
my $T_SOAP_VERSION = "x.x";

# Names
my $ASM_NAME_PREFIX = "MASS-k";

# Kmer constants
my $KMER_MIN = 11;
my $KMER_MAX = 125;
my $DEF_KMER_MIN = 41;
my $DEF_KMER_MAX = 125;

# Threads constants
my $DEF_THREADS=8;

# Mem constants
my $DEF_MEM=60;
my $MIN_MEM=5;

# Source command constants
my $ABYSS_SOURCE_CMD = AppStarter::getAppInitialiser("ABYSS");

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
	'config|cfg=s',
	'kmin=i',
	'kmax=i',
	'stats',
	'simulate|sim|s',
	'log',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


print "\nCommand line arguments gathered\n\n" if $qst->isVerbose();


# Argument Validation

die "Error: No config file specified\n\n" unless $opt{config};
die "Error: K-mer limits must be >= " . $KMER_MIN . "nt\n\n" unless ($opt{kmin} >= $KMER_MIN && $opt{kmax} >= $KMER_MIN);
die "Error: K-mer limits must be <= " . $KMER_MAX . "nt\n\n" unless ($opt{kmin} <= $KMER_MAX && $opt{kmax} <= $KMER_MAX);
die "Error: Min K-mer value must be <= Max K-mer value\n\n" unless ($opt{kmin} <= $opt{kmax});
die "Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.\n\n" unless (validKmer($opt{kmin}) && validKmer($opt{kmax}));

die "Error: Invalid number of cores requested.  Must request at least 1 core per assembly.\n\n" unless ($qst->getThreads() >= 1);

die "Error: Invalid memory setting.  Must request at least " . $MIN_MEM . "GB.\n\n" unless ($qst->getMemoryGB() >= $MIN_MEM);

print "Validated arguments\n\n" if $qst->isVerbose();



# We shouldn't have to do this but abyss



# These variables get varied for each run.
my $job_prefix = $qst->getJobName();
my $output_dir = $qst->getOutput();

my $cfg = new Configuration( $opt{config} );


# Assembly job loop
my $tool = $qst->getTool();
my $tool_version = "x.x";
my $j = 0;

# Create directory for links to assembled contigs
my $contigs_dir = $output_dir . "/contigs";
system("mkdir", $contigs_dir) unless (-e $contigs_dir);

# Abyss automatically creates scaffolds so create a directory for links to assembled scaffolds
my $scaffolds_dir = $output_dir . "/scaffolds";
my $stat_scaffolds = 0;
if ($tool eq $T_ABYSS) {
	$stat_scaffolds = 1;
	$tool_version = $T_ABYSS_VERSION;
	system("mkdir", $scaffolds_dir) unless (-e $scaffolds_dir);	
}


for(my $i=$opt{kmin}; $i<=$opt{kmax};) {

	my $i_dir = $output_dir . "/" . $i;
	my $job_name = $job_prefix . "-k" . $i;
	my $qst_ass = createAssemblyJobOptions($i_dir, $job_name);
	my $cmd_line;
	
	if ($tool eq $T_ABYSS) {
		
		# TGAC Cluster specific command (Abyss doesn't like Intel nodes, so avoid those)
		$qst_ass->setExtraArgs("-Rselect[hname!='n57142.tgaccluster']");
		
		# Build up the command line for abyss
		$cmd_line = buildAbyssCmdLine($i);
		
		# Make links to assembled contigs at a predicatable location
		my $abyss_config_file = "../" . $i . "/" . $ASM_NAME_PREFIX . $i . "-contigs.fa"; #Expected abyss contig file
		my $contig_sl_file = $contigs_dir . "/k" . $i . "-contigs.fa";	#Output file				
		system("ln -s -f " . $abyss_config_file . " " . $contig_sl_file);
		
		# Make links to assembled scaffolds at a predicatable location
		my $abyss_scaffold_file = "../" . $i . "/" . $ASM_NAME_PREFIX . $i . "-scaffolds.fa"; #Expected abyss scaffold file
		my $scaffold_sl_file = $scaffolds_dir . "/k" . $i . "-scaffolds.fa";	#Output file				
		system("ln -s -f " . $abyss_scaffold_file . " " . $scaffold_sl_file);
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

	# Make the output directory for this child job (clean the directory if it exists)
	if (-e $i_dir) {
		system("rm -R -f " . $i_dir . "/*");	
	}
	else {
		system("mkdir", $i_dir) unless ();
	}
	
	# Go into the output dir for this job
  	chdir $i_dir;

	# Submit the job
	SubmitJob::submit($qst_ass, $cmd_line) unless $opt{simulate};

	# Go back to the working directory
	chdir $PWD;
	
	# Increment the kmer
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
	
	# Create the job options	
	my $qst_stats = createStatJobOptions();
	
	# Create the command line
	my @stat_args;
	push @stat_args, buildStatCmdLine($contigs_dir);
	push @stat_args, buildStatCmdLine($scaffolds_dir) if $stat_scaffolds;
	
	my $stat_cmd_line = join "; ", @stat_args;
	
	# Submit the stat job
	SubmitJob::submit($qst_stats, $stat_cmd_line);
}


if ($opt{log}) {
	open (LOGFILE, ">", $output_dir . "/mass.log");
	print LOGFILE "[MASS]\n";
	print LOGFILE "tool=" . $tool . "\n";
	print LOGFILE "version=" . $tool_version . "\n";
	print LOGFILE "kmin=" . $opt{kmin} . "\n";
	print LOGFILE "kmax=" . $opt{kmax} . "\n";
	close(LOGFILE);
}


# Script finished successfully... but the jobs will still be running...

exit 0;


sub buildStatCmdLine {
	my ($stat_dir) = @_;
	
	my @mgp_args = grep {$_} (
		$MASS_GP_PATH,
		"--grid_engine NONE",
		$qst->isVerboseAsParam(),
		"--input " .  $stat_dir,
		"--output " . $stat_dir
	);
	
	my $mgp_cmd_line = join " ", @mgp_args;
	
	return $mgp_cmd_line;
}


sub buildAbyssCmdLine {
	
	my ($kmer) = @_;
	
	# Create argument list
	my $abyss_core_args = "n=10 mpirun=mpirun.lsf";
	my $abyss_threads = "np=" . $qst->getThreads();
	my $abyss_kmer = "k=" . $kmer;
	my $abyss_out_prefix = "name=" . $ASM_NAME_PREFIX . $kmer;
	
	# Process configuration file to build list of libraries to assemble
	my @libs = ();
	my @lib_args = ();
	my @single_ends = ();
	
	for(my $j = 1; $j < $cfg->getNbSections(); $j++) {
		my $sect = $cfg->getSectionAt($j);
		if ($sect->{usage} eq "ASSEMBLY" || $sect->{usage} eq "ASSEMBLY_AND_SCAFFOLDING") {			
			my $sect_name = $cfg->getSectionNameAt($j);
			my $se = $sect->{file_se};
			push(@libs, $sect_name);
			push(@lib_args, ($sect_name . "='" . $sect->{file_paired_1} . " " . $sect->{file_paired_2} . "'"));
			push(@single_ends, $se) if $se;
		}
	}
	
	my $abyss_lib = "lib='" . (join " ", @libs) . "'";
	my $abyss_lib_args = (join " ", @lib_args);
	my $abyss_ses = @single_ends > 0 ? "se='" . (join " ", @single_ends) . "'" : "";
	my $abyss_libs = "-j" . ($cfg->getNbSections() - 1);
	
	# Put together all the arguments
	my @abyss_args = grep {$_} (
		$ABYSS_SOURCE_CMD,
		$TP_ABYSS,
		$abyss_threads,
		$abyss_core_args,
		$abyss_kmer,
		$abyss_out_prefix,
		$abyss_libs,
		$abyss_lib,
		$abyss_lib_args,
		$abyss_ses
	);
	
	my $cmd_line = join " ", @abyss_args;
	
	return $cmd_line;
}


sub createAssemblyJobOptions {
	
	my ($i_dir, $job_name) = @_;
	
	my $qst_ass = new QsOptions();
	$qst_ass->setGridEngine($qst->getGridEngine());
	$qst_ass->setTool($qst->getTool());
	$qst_ass->setToolPath($qst->getToolPath());
	$qst_ass->setProjectName($qst->getProjectName());
	$qst_ass->setWaitCondition($qst->getWaitCondition());
	$qst_ass->setQueue($qst->getQueue());
	$qst_ass->setMemory($qst->getMemoryGB());
	$qst_ass->setThreads($qst->getThreads());
	$qst_ass->setVerbose($qst->isVerbose());

	$qst_ass->setOutput($i_dir);
	$qst_ass->setJobName($job_name);
	
	return $qst_ass;
}

sub createStatJobOptions {
	
	my $gp_wc_arg = "ended(" . $job_prefix . "-k*)"; # This presumes an LSF wait condition, modify to handle this better in the future.
	my $gp_job_arg = $job_prefix . "-stats";
	
	my $qst_stats = new QsOptions();
	$qst_stats->setGridEngine($qst->getGridEngine());
	$qst_stats->setProjectName($qst->getProjectName());
	$qst_stats->setWaitCondition($gp_wc_arg);
	$qst_stats->setQueue($qst->getQueue());
	$qst_stats->setVerbose($qst->isVerbose());
	$qst_stats->setJobName($gp_job_arg);
	
	return $qst_stats;
}

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

B<mass.pl>


=head1 SYNOPSIS

B<mass.pl> [options] B<--config> F<config.cfg> 

For full documentation type: "mass.pl --man"


=head1 DESCRIPTION

Runs an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.


=head1 OPTIONS

=over

=item B<--config>,B<--cfg>

REQUIRED: The rampart library configuration file to use, which describes the paired end / mate pair reads which are to be used to improve the assembly.

=item B<--grid_engine>,B<--ge>

The grid engine to use.  Currently "LSF" and "PBS" are supported. Default: LSF.

=item B<--tool>,B<-t>

Currently these assemblers are supported (abyss).  Default: abyss.

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

=item B<--output>,B<--out>,B<-o>

The output dir for this job. Default: Current working directory (".")

=item B<--verbose>,B<-v>

Whether detailed debug information should be printed to STDOUT.

=item B<--kmin>

The minimum k-mer value to run. Default: 41.

=item B<--kmax>

The maximum k-mer value in run. Default: 125.

=item B<--stats>

Produces output statistics and graphs comparing each assembly job produced.

=item B<--simulate>,B<--sim>,B<-s>

Runs the script as normal except that the assembly jobs are not submitted.

=item B<--log>

Whether to log the mass scripts settings in a file called F<mass.log> in the directory specified with the B<--output> argument. 

=item B<--help>,B<--usage>,B<-h>,B<-?>

Print usage message and then exit.

=item B<--man>

Display manual.

=back

=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

