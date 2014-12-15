
.. _introduction:

Introduction
============

RAMPART is a configurable pipeline for *de novo* assembly of DNA sequence data.  RAMPART is not a *de novo* assembler.
There are already many very good freely available assembly tools, however, few will produce a good assembly, suitable for
annotation and downstream analysis, first time around.  The reason for this is that genome assembly of non-model organisms
are often complex and involve tuning of parameters and potentially, pre and post processing of the assembly.  There are
many combinations of tools that could be tried and no clear way of knowing *a priori*, which will work best.

RAMPART makes use of tried and tested tools for read pre-processing, assembly and assembly improvement, and allows the
user to configure these tools and specify how they should be executed in a single configuration file.  RAMPART also
provides options for comparing and analysing sequence data and assemblies.

This functionality means that RAMPART can be used for at least 4 different purposes:

* Analysing sequencing data and understanding novel genomes.  
* Comparing and testing different assemblers and related tools on known datasets.  
* An automated pipeline for *de novo* assembly projects. 
* Provides a single common interface for a number of different assembly tools.

The intention is that RAMPART gives the user the possibility of producing a decent assembly that is suitable for
distribution and downstream analysis.  Of course, in practice not every assembly project is so straight forward, the
actual quality of assembly is always going to be a function of at least these sequencing variables:

* sequencing quality
* sequencing depth
* read length
* read insert size

... and the genome properties such as:

* genome size
* genome ploidy
* genome repetitiveness

RAMPART enables a bioinformatician to get a reasonable assembly, given the constraints just mentioned, with minimal effort.
In many cases, particularly for organisms with haploid genomes or relatively simple (i.e. not too heterozygous and not
too repeaty) diploid genomes, where appropriate sequenceing has been conducted, RAMPART can produce an assembly that
suitable for annotation and downstream analysis.

RAMPART is designed with High Performance Computing (HPC) resources in mind.  Currently, LSF and PBS schedulers are
supported and RAMPART can execute jobs in parallel over many nodes if requested.  Having said this RAMPART can be told
to run all parts of the pipeline in sequence on a regular server provided enough memory is available for the job in question.

This documentation is designed to help end users install, configure and run RAMPART.

Comparison to other systems
---------------------------

**Roll-your-own Make files**  This method probably offers the most flexibility.  It allows you to define exactly how you
want your tools to run in whatever order you wish.  However, you will need to define all the inputs and outputs to each tool.
And in some cases write scripts to manage interoperability between some otherwise incompatible tools.  RAMPART takes all
this complication away from the user as all input and output between each tool is managed automatically.  In addition,
RAMPART offers more support for HPC environments, making it easier to parallelize steps in the pipeline.  Managing this
manually is difficult and time consuming.

**Galaxy** This is a platform for chaining together tools in such a way as to promote reproducible analyses for biomedical
research.  It also has support for HPC environments.  However, it is a heavy weight solution, and is not trivial to install
and configure locally.  RAMPART itself is lightweight in comparison, and ignoring dependencies, much easier to install.  In
addition, galaxy is not designed with *de novo* genome assembly specifically in mind, whereas RAMPART
does.  RAMPART places more constraints in the workflow design process as well as more checks initially before the
workflow is started.  In addition, as mentioned above RAMPART will automatically manage interoperability between tools, which
will likely save the user time debugging workflows and writing their own scripts to manage specific tool interaction issues.

**A5-miseq** and **BugBuilder** Both are domain specific pipeline for automating assembly of microbial organisms.
They are are designed specifically with microbial genomes in mind and keep their interfaces simple and easy to use.  RAMPART,
while more complex to use, is far more configurable as a result.  RAMPART also allows users to tackle eukaryote assembly projects.

**iMetAMOS** This is a configurable pipeline for isolate genome assembly and annotation.  One distinct advantage of iMetAMOS is
that it offers the ability to annotate your genome.  It also supports some assemblers that RAMPART currently does not.
Both systems are highly configurable, allowing the user to create bespoke pipelines and compare and validate the results of multiple
assemblers. However, in it's current form, iMetAMOS doesn't have as much provision for automating or managing assembly scaffolding
or gap filling steps in the assembly workflow. In addition, we would argue that RAMPART is more configurable, easier to use
and has more support for HPC environments.
