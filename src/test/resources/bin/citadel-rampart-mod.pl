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
        'citadel_dir|i=s',
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
my $citadel_dir = $opt{citadel_dir};
my $template = $opt{template};
my $verbose = $opt{verbose};
my $debug = $opt{debug};

die "You must specify the citadel dir" unless $citadel_dir;
die "You must specify the template file" unless $template;


my @samples;

my @sample_dirs = glob "$citadel_dir/*";
print "Gathering samples\n";
foreach(@sample_dirs) {
        my $sample_name = substr $_, rindex($_, '/') + 1;
        print "Sample found: $sample_name\n";
        push @samples, $sample_name;
}
print "Gathered samples\n";

my $num_samples = @samples;
for(my $i = 0; $i < $num_samples; $i++) {
	my $sample = $samples[$i];
	my $dir = $sample_dirs[$i];
	system("sed 's/SAMPLE/" . $sample. "/g' " . $template . " > " . $dir . "/rampart-mod.cfg");

}



print("Executing RAMPART for each sample\n");
foreach(@sample_dirs) {
	
	my $sample_dir = $_;
	system("bsub -oo" . $sample_dir . "/rampart-mod.lsf.log -qProd128 \"source rampart-0.7.0; rampart run -o " . $sample_dir . " " . $sample_dir . "/rampart-mod.cfg\"");

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


