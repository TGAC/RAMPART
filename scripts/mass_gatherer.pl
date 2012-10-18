#!/usr/bin/perl

use strict;

use Getopt::Long;
use Pod::Usage;
use File::Find;
use Cwd;
use Cwd 'abs_path';
use File::Basename;


# Assembler constants
my $A_ABYSS = "abyss";
my $DEF_ASSEMBLER = $A_ABYSS;

# Sequence info path
my $SEQ_INFO_ON_PATH = which('sequence_info');
my $PURNIMA_SEQ_INFO_PATH = '/usr/users/tgac/pachorip/clc/clc-ngs-cell-3.0.0beta2-linux_64/sequence_info';
my $SEQ_INFO_PATH;
if(-e 'sequence_info') {
	$SEQ_INFO_PATH = 'sequence_info';
#	print "Found sequence_info binary in current directory\n\n" if $opt{verbose};
}
elsif(&which('sequence_info')) {
	$SEQ_INFO_PATH = 'sequence_info';
#	print "Found sequence_info binary on path\n\n" if $opt{verbose};
}
elsif(-e $PURNIMA_SEQ_INFO_PATH ) {
	$SEQ_INFO_PATH = $PURNIMA_SEQ_INFO_PATH;
#	print "Found sequence_info binary in Purnima's directory\n\n" if $opt{verbose};
}
else {
#	print "Couldn't find sequence_info binary.  User must specify location via 'seq_info' option.\n\n" if $opt{verbose};
};


# Other constants
my $PWD = getcwd;

# Assign any command line options to variables
my (%opt) = (   "assembler",    $DEF_ASSEMBLER);

$opt{seq_info} = $SEQ_INFO_PATH if ($SEQ_INFO_PATH);

GetOptions (
        \%opt,
        'assembler|a=s',
	'seq_info|si=s',
        'verbose|v',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


# Get input directory
my @in_dir = @ARGV;
my $input_dir = join " ", @in_dir;

print "\n" if $opt{verbose};
print "Command line arguments gathered\n\n" if $opt{verbose};


# Argument Validation

die "Error: No input files specified\n\n" unless @in_dir;
die "Error: Can only analyse one assembly group at a time\n\n" unless (@in_dir == 1);
die "Error: Input directory does not exist: " . $input_dir . "\n\n" unless (-e $input_dir);

die "Error: Could not find sequence_info binary file\n\n" unless $opt{seq_info};

print "Validated arguments\n\n" if $opt{verbose};
print "Input directory: " . $input_dir . "\n" if $opt{verbose};
if ($opt{verbose}) {
        print "Options:\n";
        foreach (keys %opt) {
                print "\t'$_' => " . $opt{$_} . "\n";
        }
        print "\n";
}


# Find all appropriate files in input directory

my @assemblies;

sub wanted { push @assemblies, $File::Find::name };
find(\&wanted, $input_dir);
my @filtered_assemblies = grep(/-scaffolds.fa$/, @assemblies);
my @filtered_assemblies = sort(@filtered_assemblies);

print "Found these files in the input directory:\n" if $opt{verbose};
print (join "\n", @filtered_assemblies) . "\n\n" if $opt{verbose};
print "\n\n" if $opt{verbose};


# Run sequence_info on each and pipe into tabulateor
#$seq_info_cmd = $opt{seq_info} . "-n -r";
my @table;
foreach(@filtered_assemblies) {
	my ($name, $dir) = fileparse(abs_path($0));
	my $tabulate_cmd = $opt{seq_info} . " -n -r " . $_ . " | " . $dir . "/mass_formatter.pl --notitle";
	my $output = `$tabulate_cmd`;
	my $matchstr = $output;
	$matchstr =~ m/-k(\d+)-scaffolds/;
	print "k" . $1 . "\n" if $opt{verbose};
	print $output . "\n" if $opt{verbose};
	push @table, $1 . "|" . $output;
}


# Need to do some extra sorting here to numberically order by kmer size
my @s_table = sort {
	my @name_pair = map { /^(\d+)(\|.*)/; $1 } ($a, $b);
	$name_pair[0] <=> $name_pair[1];
} @table;


print "\nStatistics:\n" if $opt{verbose};
print "kmer|file|nbcontigs|a.pc|c.pc|g.pc|t.pc|n.pc|total|minlen|maxlen|avglen|n50\n";
print (join "", @s_table) . "\n";



sub which {

# Get the passed value
  my $program = shift;

# Return if nothing is provided
  return if (not defined($program));

# Load the path
  my $path = $ENV{PATH};

# Let's replace all \ by /
  $path =~ s/\\/\//g;

# Substitute all /; by ; as there could be some trailing / in the path
  $path =~ s/\/;/;/g;

# Now make an array
  my @path = split(/;/,$path);

# Loop and find if the file is in one of the paths
  foreach (@path) {

  # Concatenate the file
    my $file = $_ . '/' . $program;

  # Return the path if it was found
    return $file if ((-e $file) && (-f $file));
  }
}


__END__

=pod

=head1 NAME

  mass_gatherer.pl


=head1 SYNOPSIS

  mass_gatherer.pl [options] <input_directory>

  For full documentation type: "mass_gatherer.pl --man"


=head1 DESCRIPTION

  Multiple Assembly Statistics Gatherer.  Assesses a group of assemblies and produces a table of assembly statistics.


=head1 OPTIONS

  assembler|a      The assembly program to use.
  verbose|v        Print extra status information during run.
  help|usage|h|?   Print usage message and then exit.
  man              Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

_
