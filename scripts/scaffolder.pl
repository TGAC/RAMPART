#!/usr/bin/perl

use Getopt::Long;
use File::Basename;
use Cwd;


$usage="\nscaffolder [-help] [-v] [-p project_name] [-r read_length] -i input_scaffold_file -sc sspace_config_file -gcc gc_config_file -o output_dir -- script to run abyss with multiple k-mer settings (41-95 with alternate 4 and 6 step increments).  Requests 60GB RAM and 8 cores per assembly\n\n" . 
"where:\n" .
"   -help  show this help text\n" .
"   -v     verbose output (default false)\n" .
"   -p     project name for marking the LSF jobs\n" .
"   -r     length of reads (default: 155)\n" .
"   -sc    SSPACE library config file\n" .
"   -gc    GC library config file\n" .
"   -i     input scaffold file to enhance\n" .
"   -o     output directory\n\n";

$job_prefix = $ENV{'USER'} . "-scaffolder-";
($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$project_name = "Scaffolder_" . $year . $mon . $mday . "_" . $hr . $min . $sec;
$in_scaffolds_file;
$sspace_config_file;
$gc_config_file;
$out_dir;
$help = 0;
$verbose = 0;
$read_length = "155";

$result = GetOptions (  "p=s"   => \$project_name,
                        "v"     => \$verbose,
			"r=i"   => \$read_length,
                        "i=s"   => \$in_scaffolds_file,
			"sc=s"  => \$sspace_config_file,
			"gcc=s" => \$gc_config_file,
                        "o=s"   => \$out_dir,
                        "help"  => \$help );


if ($help) {
        print $usage;
        exit 0;
}



die "Error: No input file specified\n\n" . $usage unless $in_scaffolds_file;
die "Error: No output directory specified\n\n" . $usage unless $out_dir;
die "Error: No SSPACE library config file specified\n\n" . $usage unless $sspace_config_file;
die "Error: No GapCloser library config file specified\n\n" . $usage unless $gc_config_file;

die "Error: Input file doesn't exist\n\n" . $usage unless (-e $in_scaffolds_file);
die "Error: Output directory doesn't exist\n\n" . $usage unless (-d $out_dir);
die "Error: SSPACE library config file doesn't exist\n\n" . $usage unless (-e $sspace_config_file);
die "Error: GapCloser library config file doesn't exist\n\n" . $usage unless (-e $gc_config_file);


$pwd = getcwd;

if (verbose) {
	print "Output Directory: ". $out_dir . "\n";
	print "Project Name: " . $project_name . "\n";
	print "Read Length: " . $read_length . "\n";
}



$bsub = "bsub";
$project_arg = "-P " . $project_name;
$queue_arg = "-q production";



# Run SSPACE (needs library config file, abyss scaffolds, and output location)

$sspace_exe = "perl /common/software/SSPACE-BASIC-2.0/x86_64/bin/SSPACE_Basic_v2.0.pl";
$sspace_job_name = $job_prefix . "sspace";
$sspace_scaffolds = "sspace";

if (verbose) {
	print "\n";
	print "Running SSPACE:\n";
	print " - SSPACE: Script location: " . $sspace_exe . "\n";
	print " - SSPACE: Job name: " . $sspace_job_name . "\n";
	print " - SSPACE: Library file: " . $sspace_config_file . "\n";
	print " - SSPACE: Abyss scaffold: " . $abyss_scaffolds . "\n";
	print " - SSPACE: Scaffold location: " . $sspace_scaffolds . "\n";
}

$sspace_job_arg = "-J " . $sspace_job_name;
$sspace_cmd = $sspace_exe . " -l " . $sspace_config_file . " -s " . $in_scaffolds_file . " -x 1 -b " . $sspace_scaffolds;

#chdir($out_dir);

system($bsub, $sspace_job_arg, $project_arg, $queue_arg, $sspace_cmd);

#chdir($pwd);


# Run Gap Closer on SSPACE scaffolds (needs SOAPdeNovo config file, SSPACE scaffolds and output file)

$gc_job_name = $job_prefix . "gc";
$gc_sspace_scaffolds = $sspace_scaffolds . ".final.scaffolds.fasta";
$gc_scaffolds = $out_dir . "/final_gc_scaffolds.fa";

if (verbose) {
	print "\n";
	print "Running Gap Closer (will start after SSPACE has completed):\n";
	print " - GC: Job name: " . $gc_job_name . "\n";
	print " - GC: Config file: " . $gc_config_file . "\n";
	print " - GC: SSPACE scaffold file: " . $gc_sspace_scaffolds . "\n";
	print " - GC: GC scaffold location: " . $gc_scaffolds . "\n";
}

$gc_job_arg = "-J " . $gc_job_name;
$gc_wait_arg = "-w 'ended(\"" . $sspace_job_name . "\")'";
$gc_cmd = "GapCloser -a $gc_sspace_scaffolds -b $gc_config_file -o $gc_scaffolds -l $read_length -p 61";

if (verbose) {
	print " - GC: Wait Arg: " . $gc_wait_arg . "\n";
}


system($bsub, $gc_job_arg, $project_arg, $queue_arg, $gc_wait_arg, $gc_cmd);



# Run Analysis scripts to produce report (needs abyss scaffolds, SSPACE scaffolds and gc scaffolds)

$analysis_wait_arg = "-w 'ended(\"" . $gc_job_name . "\")'";
$purnima_seq_inf_script = "~pachorip/clc/clc-ngs-cell-3.0.0beta2-linux_64/sequence_info -n -r ";
$nizar_formatter_script = "~/bin/assembly_stats_formatter.pl";

$abyss_analysis_cmd = $purnima_seq_info_script . $abyss_scaffolds . " | perl " . $nizar_formatter_script . " > abyss_stats.txt";
$sspace_analysis_cmd = $purnima_seq_info_script . $abyss_scaffolds . " | perl " . $nizar_formatter_script . " > abyss_stats.txt";
$gc_analysis_cmd = $analysis_wait_arg, $purnima_seq_info_script . $gc_scaffolds . " | perl " . $nizar_formatter_script . " > gc_stats.txt";


#system($bsub, $project_arg, $queue_arg, $analysis_wait_arg, $abyss_analysis_cmd);

#system($bsub, $project_arg, $queue_arg, $analysis_wait_arg, $sspace_analysis_cmd);

#system($bsub, $project_arg, $queue_arg, $analysis_wait_arg, $gc_analysis_cmd);

