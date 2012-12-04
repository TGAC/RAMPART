#!/usr/bin/perl

use strict;
use warnings;

# 3rd Part modules
use Getopt::Long;
use Pod::Usage;
use Cwd;

my $PWD   = getcwd;

# Parse tool specific options
my (%opt) =  ( "job_dir", $PWD );

GetOptions (
        \%opt,
        'job_dir|job=s',
        'help|usage|h|?',
        'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

my $job_dir = $opt{job_dir};

print "Job Directory cannot be found at: " . $job_dir unless $job_dir;

my $reads_dir = $job_dir . "/reads";
my $mass_dir = $job_dir . "/mass";
my $improver_dir = $job_dir . "/improver";
my $report_dir = $job_dir . "/report";
my $log_dir = $job_dir . "/log";


system("rm -R -f " . $reads_dir);
system("rm -R -f " . $mass_dir);
system("rm -R -f " . $improver_dir);
system("rm -R -f " . $report_dir);
system("rm -R -f " . $log_dir);


__END__

=pod

=head1 NAME

B<clean_job.pl>


=head1 SYNOPSIS

B<clean_job.pl> [B<--job_dir> F<job_directory>]

For full documentation type: "clean_job.pl --man"


=head1 DESCRIPTION

This script is designed to clean a rampart job directory, leaving only those files that can't be regenerated by re-running rampart.  


=head1 OPTIONS

=over

=item B<--job_dir>,B<--job>

The job directory to be cleaned.  Default: Currentl working directory (".")

=item B<--help>,B<--usage>,B<-h>,B<-?>

Print usage message and then exit.

=item B<--man>

Display manual.

=back


=head1 AUTHORS

Daniel Mapleson <daniel.mapleson@tgac.ac.uk>

Nizar Drou <nizar.drou@tgac.ac.uk>

=cut
