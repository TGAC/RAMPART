#!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/../modules";

use Test::More tests => 14;

use lib '../scripts';

use QsOptions;


# Constants
my $qs = "LSF";
my $t = "test";
my $tp = "test/test.path";
my $pn = "test project";
my $jn = "job";
my $wc = "done(test_job)";
my $q = "normal";
my $mem = "2";
my $thr = "2";
my $extra_args = "";
my $input = "input.txt";
my $output = "output.txt";


# Add args to ARGV
my @args = (	"--queueing_system", 	$qs,
				"--tool",				$t,
				"--tool_path",			$tp,
				"--project_name",		$pn,
				"--job_name",			$jn,
				"--wait_condition",		$wc,
				"--queue",				$q,
				"--memory",				$mem,
				"--threads",			$thr,
				"--input",				$input,
				"--output",				$output,
				"--verbose" );

push @ARGV, @args;

# Display argv
print "\nARGV: " . (join " ", @ARGV) . "\n\n";


# Create a Qs Tool and test
my $tool = new QsOptions();
ok(defined($tool), 'constructor');

# Parse ARGV
$tool->parseOptions();
#print "\nParseing test arguments\n";

# Test all variables were set.
ok($tool->getQueueingSystem()	eq $qs,			'get queueing system'	);
ok($tool->getTool() 			eq $t, 			'get tool'				);
ok($tool->getToolPath() 		eq $tp, 		'get tool path'			);
ok($tool->getProjectName() 		eq $pn, 		'get project name'		);
ok($tool->getJobName() 			eq $jn, 		'get job name'			);
ok($tool->getWaitCondition()	eq $wc, 		'get wait condition'	);
ok($tool->getQueue() 			eq $q, 			'get queue'				);
ok($tool->getMemoryGB()			eq $mem, 		'get memory GB'			);
ok($tool->getMemoryMB()			eq $mem * 1000, 'get memory MB'			);
ok($tool->getThreads()			eq $thr, 		'get threads'			);
ok($tool->getInput() 			eq $input, 		'get input'				);
ok($tool->getOutput() 			eq $output, 	'get output'			);
ok($tool->isVerbose()			eq 1,			'get verbose'			);

print "\n";
print $tool->toString() . "\n";
