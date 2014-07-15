
.. _running:

Multi-strain projects
=====================

Due to the falling cost and increasing capacity of sequencing devices, as well as improvements in the automation of library
preparation, it is now possible to sequence many strains of the same species.  Assembling these strains in isolation can
be time consuming and error prone, even with pipelines such as RAMPART.  We therefore provide some additional tools to
help the bioinformatician manage these projects and produce reasonable results in short time frames.

The logic we use to approach multi-strain projects is as follows:

1. Use jellyswarm to assess all samples based on their distinct kmer count after rare kmers (sequencing errors) are excluded.  If there is little agreement between samples then more analysis is required.  If there is good agreement then carry on to 2.
2. Exclude outlier samples.  These must be assembled and analysed separately.
3. Use the abyss and/or velvet assembler (or any other assembler that runs quickly and is suitable for assembling data from the species in question) via RAMPART on a few of the more typical strains (strains where distinct kmer count is close to the mean) to attempt to identify optimal parameters, particularly the k parameter.  If there is little agreement in parameters between samples then all strains must be looked at in isolation.  If there is good agreement carry on to 4.
4. Create a template RAMPART configuration file containing the optimal assembler and settings and then use the citadel script to execute for all strains.

Note: This is by no means the only way to approach these projects, nor will it necessarily give the best results, but it
should allow a single bioinformatician to produce reasonable assemblies for a project with 100s of samples within a week,
assuming they have access to a suitable high performance computing cluster.


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


Citadel
-------

Citadel is a perl script that is designed to execute RAMPART on all input samples by using a template configuration file.
There are also a number of supplementary scripts to aid the analysis of data across all samples and annotation of each
sample using PROKKA (http://www.vicbioinformatics.com/software.prokka.shtml).  Note that PROKKA is only relevant for prokaryotic
genomes.

All the scripts were designed for execution on LSF environments, so some modification of the scripts may be necessary
should you wish to execute in other scheduled environments
or on unscheduled systems.  Whilst each script comes with it's own help message and man page, we so not provide extensive
documentation on citadel in this version of RAMPART and leave it to the bioinformatician to tweak or reuse the scripts
as they see fit.