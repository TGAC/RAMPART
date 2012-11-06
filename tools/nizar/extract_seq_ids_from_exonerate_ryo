#!/usr/bin/perl

my $ryo_file = $ARGV[0];
my $id;

open (FH,"$ryo_file")|| die "Cannot open input exonerate file $ryo_file\n\n";

while (<FH>){
chomp;
	if(/^ryo\t(\w+)\:.*/ or /^ryo\t(\w+\|\w+):.*/ or /^ryo\t(scaffold\d+\.\d+\|\w+)\:.*/){
	$id = $1;
	}
print ">$id\n";
}
close FH;
