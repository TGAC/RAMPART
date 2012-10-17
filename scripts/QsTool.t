#!/usr/bin/perl

use strict;
use warnings;

use Test::More tests => 12;

use QsTool;


# Constants
my $qs = "LSF";
my $t = "test";
my $tp = "test/test.path";
my $pn = "test project";
my $jn = "job";
my $wc = "done(test_job)";
my $q = "normal";
my $res = "rusage[mem=2000]";
my $extra_args = "";
my $input = "input.txt";
my $output = "output.txt";


# Add args to ARGV
my @args = (	"--queueing_system", 	$qs,
		"--tool",		$t,
		"--tool_path",		$tp,
		"--project_name",	$pn,
		"--job_name",		$jn,
		"--wait_condition",	$wc,
		"--queue",		$q,
		"--resources",		$res,
		"--input",		$input,
		"--output",		$output,
		"--verbose" );

push @ARGV, @args;

# Display argv
print "\nARGV: " . (join " ", @ARGV) . "\n\n";


# Create a Qs Tool and test
my $tool = new QsTool();
ok(defined($tool), 'constructor');

# Parse ARGV
$tool->parseOptions();
#print "\nParseing test arguments\n";

# Test all variables were set.
ok($tool->getQueueingSystem()	eq $qs,		'get queueing system'	);
ok($tool->getTool() 		eq $t, 		'get tool'		);
ok($tool->getToolPath() 	eq $tp, 	'get tool path'		);
ok($tool->getProjectName() 	eq $pn, 	'get project name'	);
ok($tool->getJobName() 		eq $jn, 	'get job name'		);
ok($tool->getWaitCondition()	eq $wc, 	'get wait condition'	);
ok($tool->getQueue() 		eq $q, 		'get queue'		);
ok($tool->getResources()	eq $res, 	'get resources'		);
ok($tool->getInput() 		eq $input, 	'get input'		);
ok($tool->getOutput() 		eq $output, 	'get output'		);
ok($tool->isVerbose()		eq 1,		'get verbose'		);

print "\n";
print $tool->toString() . "\n";
