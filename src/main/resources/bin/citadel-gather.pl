#!/usr/bin/env perl

use warnings;
use strict;

use Getopt::Long;
use Pod::Usage;

use Cwd;
use Cwd 'abs_path';
use File::Basename;
use File::Find;



# Assign any command line options to variables
my %opt;

GetOptions(
        \%opt,
        'input|i=s',
	'plots|p=s',
	'prefix|x=s',
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
my $plots = $opt{plots};
my $prefix = $opt{prefix};
my $output  = $opt{output};
my $verbose = $opt{verbose};
my $debug = $opt{debug};

my @input_dir = ($input);


my $asm_links_dir = $output . "/quast";
mkdir($asm_links_dir);

my $kat_plots_dir = $output . "/kat-cn-plots";
mkdir($kat_plots_dir);

system("cp " . $plots . "/* " . $kat_plots_dir);

my @samples = glob "$input/*";
foreach(@samples) {
	
	my $sample = substr $_, rindex($_, '/');
	my $sample_dir = $input . "/" . $sample;
	my $output_sample_dir = $output . "/assemblies" . $sample;
	
	system("mkdir -p " . $output_sample_dir);
	system("cp -r " . $sample_dir . "/final/* " . $output_sample_dir);

	system("cp " . $output_sample_dir . "/*scaffolds.fa " . $asm_links_dir);
	system("cp " . $kat_plots_dir . $sample . "-kat-cn-plot.1.png " . $output_sample_dir);
}



print("Done\n");

exit(0);


__END__

=pod

=head1 NAME

  citadel-cn_plot.pl

=head1 SYNOPSIS

  citadel-cn_plot.pl B<--input> F<input_citadel_directory> B<--output> F<output_directory> B<--x_max> F<X_axis_limit> B<--y_max> F<Y_axis_limit>

  For full documentation type: "citadel-cn_plot.pl --man"


=head1 DESCRIPTION

  Scans a citadel directory for kat comp matrix files.  For each matrix, plots a copy number graph and bundles in the same directory.


