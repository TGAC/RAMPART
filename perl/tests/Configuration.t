#!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/../modules";

use Test::More tests => 14;

use lib '../scripts';

use Configuration;
use Config::IniFiles;

# Create the test file
my $DATA_DIR = "./data";
my $ini_path = $DATA_DIR . "/test.cfg";
my $ini_file_contents = <<"EOT";
#Test Configuration File with two short paired end libraries
[LIB1]
max_rd_len=155
avg_ins=504
reverse_seq=0
q1=$DATA_DIR/Test_reads_LIB1_P1.fastq
q2=$DATA_DIR/Test_reads_LIB1_P2.fastq
[LIB2]
max_rd_len=36
avg_ins=200
reverse_seq=1
q1=$DATA_DIR/Test_reads_LIB2_P1.fastq
q2=$DATA_DIR/Test_reads_LIB2_P2.fastq
EOT

open (CFG, ">$ini_path");
print CFG $ini_file_contents;
close (CFG); 

# Create a Configuration object and test
my $cfg = new Configuration($ini_path);
ok(defined($cfg), 'constructor');

# Check properties
my @files = $cfg->getAllInputFiles();
ok(@files		eq 4,			'get number of files'	);
ok($files[0]	eq $DATA_DIR. "/Test_reads_LIB1_P1.fastq",	'first file path'    );
ok($files[1]	eq $DATA_DIR. "/Test_reads_LIB1_P2.fastq",	'second file path'   );
ok($files[2]	eq $DATA_DIR. "/Test_reads_LIB2_P1.fastq",	'third file path'    );
ok($files[3]	eq $DATA_DIR. "/Test_reads_LIB2_P2.fastq",	'fourth file path'   );

ok($cfg->getNbSections()	eq 2,		'number of sections');

my $section0 = $cfg->getSectionAt(0);
ok($section0->{max_rd_len}	eq 155,		'sect0 - maximum read length');
ok($section0->{avg_ins}		eq 504,		'sect0 - average insert size');
ok($section0->{reverse_seq}	eq 0,		'sect0 - reverse seq');

my $section1 = $cfg->getSectionAt(1);
ok($section1->{max_rd_len}	eq 36,		'sect1 - maximum read length');
ok($section1->{avg_ins}		eq 200,		'sect1 - average insert size');
ok($section1->{reverse_seq}	eq 1,		'sect1 - reverse seq');

