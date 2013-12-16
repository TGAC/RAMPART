
.. _introduction:

Introduction
============

RAMPART is a software tool for automated *de novo* assembly of DNA sequence data.  The pipeline makes use of tried and tested tools for read pre-processing, assembly and assembly improvement and enables the user to configure these tools and specify how they should be executed in a single configuration file.  RAMPART also provides options for comparing and analysing sequence data and assemblies.

This functionality means that RAMPART can be used for at least 4 different purposes.  The first is to explore and understand novel genomes.  By looking at metrics produced by the reads and assemblies it is possible to say something about the genome ploidy, genome size and genome complexity, as well as the quality of your sequence data.  The second is as a workbench for testing different assemblers and related tools on known data.  Third, RAMPART can be used as an automated pipeline for *de novo* assembly projects. Finally, it can be used as a single common interface to controlling and executing a number of different tools.

The intention is that RAMPART gives you the possibility of producing a decent assembly that is suitable for distribution and downstream analysis.  Of course your mileage may vary, the actual quality of assembly is always going to be a function of at least these sequencing variables:

* sequencing quality
* sequencing depth
* read length
* read insert size

... and the genome properties such as:

* genome size
* genome ploidy
* genome repetitiveness and complexity

RAMPART enables a bioinformatician to get a reasonable assembly given the constraints just mentioned with minimal effort.  In many cases, particularly for organisms with haploid genomes or relatively simple (i.e. not too heterozygous and not too repeaty) diploid genomes, where appropriate sequenceing has been conducted, RAMPART can produce an .

Finally, RAMPART is designed with High Performance Computing (HPC) resources in mind.  Currently, LSF and PBS schedulers are supported and RAMPART can execute jobs in parallel over many many if requested.  Having said this RAMPART can be told to run all parts of the pipeline in sequence on a regular server providing enough memory is available for the job in question.

This documentation is designed to help end users install, configure and run the RAMPART pipeline.


