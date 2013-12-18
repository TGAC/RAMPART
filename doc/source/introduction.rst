
.. _introduction:

Introduction
============

RAMPART is a configurable pipeline for *de novo* assembly of DNA sequence data.  RAMPART is not a *de novo* assembler.  There are already many very good freely available assembly tools, however, few will produce a good assembly first time using the default settings.  Sometimes one particular assembler may perform well on your data and genome, but in other cases another performs better.  Sometimes an assembly will be better if the reads are error corrected, other times not.  Also depending on the data aviailable, it maybe possible to improve the assembly with additional scaffolding or gap filling steps.  There are many combinations of tools that could be tried and no clear way of knowing *a priori*, which will work best.  RAMPART can be though of as a framework for these kinds of assembly projects.  It is pipeline makes use of tried and tested tools for read pre-processing, assembly and assembly improvement, that allows the user to configure these tools and specify how they should be executed in a single configuration file.  RAMPART also provides options for comparing and analysing sequence data and assemblies.

This functionality means that RAMPART can be used for at least 4 different purposes:

* Analysing sequencing data and understanding novel genomes.  
* Comparing and testing different assemblers and related tools on known datasets.  
* An automated pipeline for *de novo* assembly projects. 
* Provides a single common interface for a number of different tools assembly tools.

The intention is that RAMPART gives the user the possibility of producing a decent assembly that is suitable for distribution and downstream analysis.  Of course, in practice not every assembly project is so straight forward, the actual quality of assembly is always going to be a function of at least these sequencing variables:

* sequencing quality
* sequencing depth
* read length
* read insert size

... and the genome properties such as:

* genome size
* genome ploidy
* genome repetitiveness

RAMPART enables a bioinformatician to get a reasonable assembly given the constraints just mentioned with minimal effort.  In many cases, particularly for organisms with haploid genomes or relatively simple (i.e. not too heterozygous and not too repeaty) diploid genomes, where appropriate sequenceing has been conducted, RAMPART can produce an assembly that suitable for annotation and downstream analysis.

RAMPART is designed with High Performance Computing (HPC) resources in mind.  Currently, LSF and PBS schedulers are supported and RAMPART can execute jobs in parallel over many nodes if requested.  Having said this RAMPART can be told to run all parts of the pipeline in sequence on a regular server provided enough memory is available for the job in question.

This documentation is designed to help end users install, configure and run the RAMPART.


