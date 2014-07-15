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
        'output|o=s',
	'x_max|x=s',
	'y_max|y=s',
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
my $x_max = $opt{x_max};
my $y_max = $opt{y_max};
my $verbose = $opt{verbose};
my $debug = $opt{debug};

my @input_dir = ($input);


my @mx_files;
sub find_mx {
	my $filename = $File::Find::name;

	if ($filename =~ /.mx$/) {
		push @mx_files, $filename;
	}
}

my @samples = glob "$input/*";
foreach(@samples) {
	
	my $sample = substr $_, rindex($_, '/');
	my $sample_dir = $input . "/" . $sample;
	
	@mx_files = ();
	find({ wanted => \&find_mx, no_chdir=>1}, $sample_dir);

	my $i = 1;
	foreach(@mx_files) {
		system("kat plot spectra-cn -x " . $x_max . " -y " . $y_max . " -o " . $output . "/" . $sample . "-kat-cn-plot." . $i . ".png " . $_);
		$i++;
	}	
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


