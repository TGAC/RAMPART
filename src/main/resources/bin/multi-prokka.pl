#!/usr/bin/env perl

use warnings;
use strict;

use Getopt::Long;
use Pod::Usage;

use Cwd;
use Cwd 'abs_path';
use File::Basename;



# Assign any command line options to variables
my %opt;

GetOptions(
        \%opt,
        'input|i=s',
        'output|o=s',
	'genus|g=s',
	'species|s=s',
	'threads|t=i',
        'verbose|v',
        'debug',
        'help|usage|h|?',
        'man'
) or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

# Get args
my $input = $opt{input};
my $output  = $opt{output};
my $genus = $opt{genus};
my $species = $opt{species};
my $threads = $opt{threads};
my $verbose = $opt{verbose};
my $debug = $opt{debug};

die "You must specify a genus" unless $genus;
die "You must specify a species" unless $species;
die "You must specify the number of threads" unless $threads;


my @samples;
my @prefixes;

print "Gathering samples...\n";
my @files = glob "$input/*.fa";
foreach(@files) {
	my $file_name = substr $_, rindex($_, '/') + 1;
	my $prefix_end = substr $file_name, 0, index($file_name, 'LIB') - 1;
	my $prefix = substr $prefix_end, 5;
	my $lib_start = substr $file_name, index($file_name, 'LIB');
	my $sample_name = substr $lib_start, 0, index($lib_start, '_');
	print "Sample found: $sample_name\n";
	push @samples, $sample_name;
	push @prefixes, $prefix;
}
print "Gathered samples\n";


my @sample_dirs;

#system("source prokka-1.7.2");

print "Initiating prokka\n";
my $num_samples = @samples;
for(my $i = 0; $i < $num_samples; $i++) {
	my $file = $files[$i];
	my $sample = $samples[$i];
	my $prefix = $prefixes[$i] . '_' . $sample;
	my $sample_out_dir = "$output/$sample";
	mkdir $sample_out_dir;
	system("bsub -oo $sample_out_dir/$sample.lsf.log -qProd128 \"prokka --genus $genus --species $species --strain $sample --cpus $threads --outdir $sample_out_dir --force --locustag $prefix --prefix $prefix --centre TGAC --increment 5 --mincontiglen 200 $file\"");
}
print "Done\n";


exit(0);




__END__

=pod

=head1 NAME

  citadel.pl

=head1 SYNOPSIS

  citadel.pl B<--input> F<input_directory> B<--output> F<output_directory> B<--template_in> F<rampart_template>

  For full documentation type: "citadel --man"


=head1 DESCRIPTION

  Scans a directory for fastq files.  Tries to group by sample.  For each sample, create a directory and fill in variables within the rampart template.  Then run rampart for each sample.


