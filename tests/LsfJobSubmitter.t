#!/usr/bin/perl

use strict;
use warnings;

use Test::More tests => 3;

use lib '../scripts';

use LsfJobSubmitter;
use QsOptions;



my $PROJECT_NAME = "Test";
my $LSF_PROJECT_CMD = "-PTest";

my $qst = new QsOptions();
$qst->setQueueingSystem("LSF");
$qst->setProjectName($PROJECT_NAME);
my $job = new LsfJobSubmitter($qst);

ok( defined($job), 'constructor defined' );
#ok( $job eq 'LsfJobSumbitter', 'constructor correct type');
ok( $job->isVerbose() eq 0, 'getter verbose' );
ok( $job->getProjectName() eq $LSF_PROJECT_CMD, 'getter project name' );

# Not sure how to test submit
#ok( $job->submit
