
.. _running:

Multi-sample projects
=====================

Due to the falling cost and increasing capacity of sequencing devices, as well as improvements in the automation of library
preparation, it is now possible to sequence many strains of the same species.  Assembling these strains in isolation can
be time consuming and error prone, even with pipelines such as RAMPART.  We therefore provide some additional tools to
help the bioinformatician manage these projects and produce reasonable results in short time frames.

The logic we use to approach multi-sample projects is as follows:

1. Use jellyswarm to assess all samples based on their distinct kmer count after rare kmers (sequencing errors) are excluded.  If there is little agreement between samples then more analysis is required.  If there is good agreement then carry on to 2.
2. Exclude outlier samples.  These must be assembled and analysed separately.
3. Use RAMPART to help develop an assembly recipe for a few of the more typical strains (strains where distinct kmer count is close to the mean) to attempt to identify optimal parameters, particularly the k parameter.  If there is little agreement in parameters between samples then all strains must be looked at in isolation.  If there is good agreement carry on to 4.
4. Use RAMPART in multi-sample configuration to execute the recipe for all strains.

Note: This is by no means the only way to approach these projects, nor will it necessarily give the best results, but it
should allow a single bioinformatician to produce reasonable assemblies for a project with hundreds or thousands of samples
within a reasonable timeframe given appropriate computational resources.


Jellyswarm
----------

Jellyswarm uses the jellyfish K-mer counting program to count K-mers found across multiple samples.  Jellyswarm will
attempt to exclude K-mers that are the result of sequencing errors from the results.  It then analyses the number of distinct
k-mers found across all samples and records basic statistics such as the mean and standard deviation of the distribution.

The syntax for running Jellyswarm from the command line is: ``jellyswarm [options] <directory_containing_samples>``.
To get a list and description of available options type: ``jellyswarm --help``. Upon starting jellyswarm will search for
an environment configuration file and a logging configuration exactly like RAMPART.
Jellyswarm will then configure the execution environment as appropriate and then run the pipeline.

Jellyswarm finds fastq samples associated with the samples by interrogating a directory containing all the files.  It then
sorts by name the files found in that directory with an ".fq" or ".fastq" extension.  By default we assume paired end
sequencing was used and group each pair of fastq files together.  If you have interleaved fastq files or have single
end data then activate the single end mode by using the ``-1`` command line option.  Jellyswarm can also interogate all
subdirectories in the parent directory for fastq files by using the ``-r`` command line option.

You can control the amount of resources jellyswarm uses on your HPC environment by using a few other options.  The ``-m``
option allows you to specify a memory limit for each jellyfish count instance that is run.  The amount of memory required
will be determined by the size of your hash and the size of the genome in question.  Depending on the particular environment
used this limit may either represent a hard limit, i.e. if exceeded the job will fail (this is the case on PBS), or it may
represent a resource reservation where by this amount of memory is reserved for the running job (this is the case on LSF).
For LSF, your job may or may not fail if the memory limit is exceeded depending on the availability of memory on the node
on which your job is running.  The number of threads per process is controlled using the ``-t`` option.  Finally, you can
k-mer count all samples in parallel by using the


RAMPART multi-sample mode
-------------------------

RAMPART can be executed in multi-sample mode by removing the ``libraries'' element from the configuration file and replacing
 it with a ``samples'' element containing a ``file'' attribute describing the path to a file containing a list of sample
 libraries to process.  For example::

  <samples file="reads.lst"/>

The file containing the sample libraries should be a tab separated file with columns describing the following:

1. Sample name
2. Phred
3. Path to R1 file
4. Path to R2 file

For example::

  PRO461_S10_B20  PHRED_33        S10_B20_R1.fastq  S10_B20_R2.fastq
  PRO461_S10_D20  PHRED_33        S10_D20_R1.fastq  S10_D20_R2.fastq
  PRO461_S10_F20  PHRED_33        S10_F20_R1.fastq  S10_F20_R2.fastq
  PRO461_S11_H2   PHRED_33        S11_H2_R1.fastq   S11_H2_R2.fastq

We may extend this format to include additional columns describing library options in the future.

In addition to replacing the libraries element with the samples element, you should also add a ``collect'' element inside
 the ``pipeline'' element::

  <collect threads="2" memory="5000"/>

A complete multi-sample configuration file might look like this::

  <?xml version="1.0" encoding="UTF-8"?>
  <rampart author="dan" collaborator="someone" institution="tgac" title="Set of C.Coli assemblies">
        <organism name="ccoli_nextera" ploidy="1">
                <reference name="C.Coli_15-537360" path="Campylobacter_coli_15_537360.GCA_000494775.1.25.dna.genome.fa"/>
        </organism>
        <samples file="reads.lst"/>
        <pipeline parallel="true">
                <mecq>
                        <ecq name="tg" tool="TrimGalore_V0.4" threads="2" memory="5000"/>
                </mecq>
                <mass>
                        <job name="spades_auto" tool="Spades_V3.1" threads="2" memory="10000">
                                <kmer min="71" max="111" step="COARSE"/>
                                <coverage list="50"/>
                                <inputs>
                                        <input ecq="tg"/>
                                </inputs>
                        </job>
                </mass>
                <mass_analysis>
                        <tool name="QUAST" threads="2" memory="5000"/>
                </mass_analysis>
                <mass_select threads="2" memory="5000"/>
                <finalise prefix="Ccoli_Nextera"/>
                <collect threads="2" memory="5000"/>
        </pipeline>
  </rampart>


You can then start RAMPART in the normal way.  RAMPART will output the stages directories as normal but as subdirectories
within a sample directory.

Currently, there are also a number of supplementary scripts to aid the analysis of data across all samples and annotation of each
sample using PROKKA (http://www.vicbioinformatics.com/software.prokka.shtml).  Note that PROKKA is only relevant for prokaryotic
genomes.  The scripts were designed for execution on LSF environments, so some modification of the scripts may be necessary
should you wish to execute in other scheduled environments or on unscheduled systems.  Whilst each script comes with its
own help message and man page, we so not provide extensive
documentation for these and leave it to the bioinformatician to tweak or reuse the scripts as they see fit.  We plan to
incorperate a mechanism into RAMPART to enable it to properly handle prokaryotic genome annotation in the future.