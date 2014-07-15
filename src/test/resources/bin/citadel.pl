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
	'template|t=s',
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
my $template = $opt{template};
my $verbose = $opt{verbose};
my $debug = $opt{debug};

my @samples;
my @file_prefixes;

print "Gathering samples...\n";
my @files = glob "$input/*_R1.fastq";
foreach(@files) {
	my $file_name = substr $_, rindex($_, '/') + 1;
	my $file_prefix = substr $file_name, 0, index($file_name, '_R1');
	my $start = substr $file_name, index($file_name, 'LIB');
	my $sample_name = substr $start, 0, index($start, '_');
	print "Sample found: $sample_name\n";
	push @samples, $sample_name;
	push @file_prefixes, $file_prefix;
}
print "Gathered samples\n";


my @sample_dirs;

print"Configuring citadel: Setting up RAMPART for each sample...\n";
my $num_samples = @samples;
for(my $i = 0; $i < $num_samples; $i++) {
	my $file_prefix = $file_prefixes[$i];
	my $sample = $samples[$i];
	mkdir "$output/$sample";
	system("ln -s -f " . $input . "/" . $file_prefix . "_R1.fastq " . $output . "/" . $sample . "/" . $sample . "_R1.fastq");
	system("ln -s -f " . $input . "/" . $file_prefix . "_R2.fastq " . $output . "/" . $sample . "/" . $sample . "_R2.fastq");
	system("sed 's/SAMPLE/" . $sample. "/g' " . $template . " > " . $output . "/" . $sample . "/rampart.cfg");	
}
print "Citadel configured\n";


print("Executing RAMPART for each sample\n");
foreach(@samples) {
	
	my $sample_dir = $output . "/" . $_;
	system("bsub -oo" . $sample_dir . "/rampart.lsf.log -qProd128 \"java -jar ~/dev/RAMPART/target/rampart-0.7.0.jar run -o " . $sample_dir . " " . $sample_dir . "/rampart.cfg\"");

}


print("Done\n");




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


