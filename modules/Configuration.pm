#!/usr/bin/perl

package Configuration;

use strict;
use warnings;

# Add current directory to @INC
use File::Basename;
use lib basename ($0);

use IniFiles;

sub new {
	my $class = shift;
	my $self = {
		_cfg => Config::IniFiles->new( -file => shift )
	};

	bless $self, $class;
	return $self;
}


sub getRawStructure {
	my ( $self ) = @_;
	return $self->{_cfg};
}

sub getAll {
	my ($self) = shift;

	return $self->{_cfg}->v;
}

sub getNbSections {
	my ($self) = shift;

	my $cfg = $self->{_cfg};
	my $count = keys %{$cfg->{v}};

	return $count;
}

sub getSectionAt {
	my ($self, $index) = @_;

	my $cfg = $self->{_cfg};
	my @sections = @{$cfg->{sects}};

	my $section = $cfg->{v}->{$sections[$index]};

	return $section;
}

sub getSectionNameAt {
	my ($self, $index) = @_;

	my $cfg = $self->{_cfg};
	my @sects = $cfg->Sections();
	my $section_name = $sects[$index];
	
	return $section_name;
}

sub getSectionByName {
	my ($self, $section_name) = @_;

	my $cfg = $self->{_cfg};
	my @section_names = $cfg->Sections();
	
	my $found_section = undef;
	my $i = 0;
	foreach(@section_names) {
		if (lc($_) eq lc($section_name)) {
			$found_section = $self->getSectionAt($i);
		} 
		$i++;
	}
	
	return $found_section;
}


sub getAllInputFiles {
	
	my ($self) = shift;

	my @fq_files = ();

	my $cfg = $self->{_cfg};
	my @sections = @{$cfg->{sects}};

	foreach (@sections) {
		my $section = $cfg->{v}->{$_};		
		my $fq1 = $section->{q1};
		my $fq2 = $section->{q2};

		push @fq_files, $fq1;
		push @fq_files, $fq2;
	}

	return @fq_files;	
}

sub save {
	my ($self, $out_path) = @_;
	
	$self->{_cfg}->WriteConfig($out_path);
}


# Ensures that all the relevant information is in the configuration file
sub validate {
	
}


1;
