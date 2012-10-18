#!/usr/bin/perl
use strict;
use Pod::Usage;
use Getopt::Long;

# This is a little parser that takes the output of the clc sequence_info script (in Purnima's home dir)
# using the flags sequence_info -n -r (assembly_fasta_file) and parses from STDIN to produces a JIRA friendly
# tabular format, i.e. fields are separated by pipes (|).


my (%opt) = (	"title",	1	);

GetOptions (
	\%opt,
	'title|t!',
        'help|usage|h|?',
	'man')
or pod2usage( "Try '$0 --help' for more information." );

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};


my $file;
my $total_ctg;
my $a;
my $c;
my $g;
my $t;
my $n;
my $total_bases;
my $min;
my $max;
my $avg;
my $n50;


print "kmer|nbcontigs|a.pc|c.pc|g.pc|t.pc|n.pc|total|minlen|maxlen|avglength|n50\n" if ($opt{title});

while (<STDIN>){
chomp;

	if(/^File\s+(\S+)/){
	$file = $1;
	}

	if(/^Number\sof\ssequences\s+(\d+)/){
	$total_ctg = $1;
	}

	if(/^*A\'s\s+\d+\s+(\S+)/){
	$a = $1;
	}

	if(/^*C\'s\s+\d+\s+(\S+)/){
	$c = $1;
	}

	if(/^*G\'s\s+\d+\s+(\S+)/){
	$g = $1;
	}

	if(/^*T\'s\s+\d+\s+(\S+)/){
	$t = $1;
	}

	if(/^*N\'s\s+\d+\s+(\S+)/){
	$n = $1;
	}

	if(/^*Total\s+(\d+)/){
	$total_bases = $1;
	}

	if(/^*Minimum\s+(\d+)/){
	$min = $1;
	}

	if(/^*Maximum\s+(\d+)/){
	$max = $1;
	}

	if(/^*Average\s+(\d+)/){
	$avg = $1;
	}

	if(/^*N50\s+(\d+)/){
	$n50 = $1;
	}
}

print "$file|$total_ctg|$a|$c|$g|$t|$n|$total_bases|$min|$max|$avg|$n50\n";

exit;

__END__

=pod

=head1 NAME

  mass_formatter.pl


=head1 SYNOPSIS

  mass_formatter.pl [options] <input_directory>

  For full documentation type: "mass_formatter.pl --man"


=head1 DESCRIPTION

  Consumes assembly statistics produced from a fasta file and presents it as a pipe delimited string.


=head1 OPTIONS

  header|head!     Whether to output a header row above the data.
  help|usage|h|?   Print usage message and then exit.
  man              Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut



