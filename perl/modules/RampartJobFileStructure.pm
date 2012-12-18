#!/usr/bin/perl

package RampartJobFileStructure;

use strict;
use warnings;

# Add current directory to @INC
use File::Basename;
use lib basename ($0);

use Cwd;
use Cwd 'abs_path';
use File::Basename;


sub new {
	my ($class) = shift;
	my $self = {
		_root_dir => shift,
		_config_file => shift	
	};

	bless $self, $class;
	return $self;
}


# **** Directories ****

sub getRootDir {
	my ( $self ) = @_;
	return $self->{_root_dir};
}

sub getReadsDir {
	my ( $self ) = @_;
	return $self->getRootDir() . "/reads";
}

sub getMassDir {
	my ( $self ) = @_;
	return $self->getRootDir() . "/mass";
}

sub getMassRawDir {
	my ( $self ) = @_;
	return $self->getMassDir() . "/raw";
}

sub getMassQtDir {
	my ( $self ) = @_;
	return $self->getMassDir() . "/qt";
}

sub getMassStatsDir {
	my ( $self ) = @_;
	return $self->getMassDir() . "/stats";
}

sub getMassBestDir {
	my ( $self ) = @_;
	return $self->getMassDir() . "/best";
}
		
sub getImproverDir {
	my ( $self ) = @_;
	return $self->getRootDir() . "/improver";
}

sub getImproverStatsDir {
	my ( $self ) = @_;
	return $self->getImproverDir() . "/stats";
}

sub getLogDir {
	my ( $self ) = @_;
	return $self->getRootDir() . "/log";
}


# **** Files ****

sub getConfigFile {
	my ( $self ) = @_;
	return $self->{_config_file};
}

sub getRawConfigFile {
	my ( $self ) = @_;
	return $self->getReadsDir() . "/raw.cfg";
}		

sub getQtConfigFile {
	my ( $self ) = @_;
	return $self->getReadsDir() . "/qt.cfg";
}

sub getMassRawStatsFile {
	my ( $self ) = @_;
	return $self->getMassRawDir() . "/contigs/stats.txt";
}

sub getMassQtStatsFile {
	my ( $self ) = @_;
	return $self->getMassQtDir() . "/contigs/stats.txt";
}

sub getMassPlotsFile {
	my ( $self ) = @_;
	return $self->getMassStatsDir() . "/plots.pdf";
}

sub getMassStatsFile {
	my ( $self ) = @_;
	return $self->getMassStatsDir() . "/score.tab";
}

sub getMassSettingsFile {
	my ( $self ) = @_;
	return $self->getMassDir() . "/mass.settings";
}

sub getMassRawSettingsFile {
	my ( $self ) = @_;
	return $self->getMassRawDir() . "/logs/mass.settings";
}

sub getMassQtSettingsFile {
	my ( $self ) = @_;
	return $self->getMassQtDir() . "/logs/mass.settings";
}

sub getBestPathFile {
	my ( $self ) = @_;
	return $self->getMassStatsDir() . "/best.path.txt";
}

sub getBestDatasetFile {
	my ( $self ) = @_;
	return $self->getMassStatsDir() . "/best.dataset.txt";
}

sub getBestAssemblyFile {
	my ( $self ) = @_;
	return $self->getMassBestDir() . "/best_assembly.fa";
}

sub getBestConfigFile {
	my ( $self ) = @_;
	return $self->getMassBestDir() . "/best.cfg";
}

sub getImproverPlotsFile {
	my ( $self ) = @_;
	return $self->getImproverStatsDir() . "/plots.pdf";
}

sub getImproverStatsFile {
	my ( $self ) = @_;
	return $self->getImproverStatsDir() . "/stats.txt";
}
		

1;