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
my $verbose = $opt{verbose};
my $debug = $opt{debug};

die "You must specify a multi-prokka directory" unless $input;
die "You must specify an output file" unless $output;

my @org_names;
my @contigs;
my @bases;
my @rrnas;
my @trnas;
my @cdss;
my @samples;
my @files;

print "Gathering samples...\n";
my @sample_dirs = glob "$input/*";
foreach(@sample_dirs) {
	my $sample_dir = $_;

	if (-d $sample_dir) {
		my $sample_name = substr $sample_dir, rindex($sample_dir, '/') + 1;
		print "Sample found: $sample_name\n";

		my @annot_files = glob "$sample_dir/*.txt";	
		print "Annot file found: $annot_files[0]\n";

		my $file_name = $annot_files[0];

		die "Couldn't find prokka stats file $file_name in $sample_dir" unless -e $file_name;

		push @samples, $sample_name;
		push @files, $file_name;
	}
}
print "Gathered samples\n";

print "Gathering prokka stats\n";
my $num_samples = @samples;
for(my $i = 0; $i < $num_samples; $i++) {
	
	my $file = $files[$i];
	my $sample = $samples[$i];

	print "Opening file: $file; for sample: $sample\n";
	
	open(IN, "<", $file) or die $!;

	while(my $line = <IN>) {
		
	#	print "Line: $line\n";
		my @parts = split ":", $line;

		my $name = $parts[0];
		my $value = substr $parts[1], 1, (length($parts[1]) - 2);

	#	print "Name: $name; Value: $value\n";
		
		if ($name eq "organism") {
			push @org_names, $value;
		}
		elsif ($name eq "contigs") {
			push @contigs, $value;
		}
		elsif ($name eq "bases") {
			push @bases, $value;
		}
		elsif ($name eq "rRNA") {
			push @rrnas, $value;
		}
		elsif ($name eq "tRNA") {
			push @trnas, $value;;
		}
		elsif ($name eq "CDS") {
			push @cdss, $value;
		}
		
		
	}
	close(IN);
}
print "Gathered stats\n";

print "Writing output\n";
open (OUT, ">$output");
print OUT "sample\tcontigs\tbases\trRNA\ttRNA\tCDS\n";
for(my $i = 0; $i < $num_samples; $i++) {
	print OUT "" . $samples[$i] . "\t" . $contigs[$i] . "\t" . $bases[$i] . "\t" . $rrnas[$i] . "\t" . $trnas[$i] . "\t" . $cdss[$i] . "\n";		
}
close (OUT);
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


