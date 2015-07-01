
.. _mass:

MASS - Multiple Assembly Creation
=================================

This tool enables the user to try different assemblers with different settings.  Currently, the following assemblers are
supported by RAMPART (brackets below indicate tool name to use if config file - case insensitive):

* Abyss V1.5 (ABYSS_V1.5)
* ALLPATHS-LG V50xxx (ALLPATHSLG_V50)
* Platanus V1.2 (Platanus_Assemble_V1.2)
* SOAP denovo V2.04 (SOAP_Assemble_V2.4)
* SPAdes V3.1 (Spades_V3.1)
* Velvet V1.2 (Velvet_V1.2)
* Discovar V51xxx (Discovar_V51XXX)


A simple MASS job might be configured as follows::

   <kmer_calc threads="32" memory="20000"/>
   <mass>
      <job name="abyss-raw-kmer" tool="ABYSS_V1.5" threads="16" memory="4000" exp_walltime="60">
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
   </mass>

This instructs RAMPART to run a single Abyss assembly using 16 threads, requesting 4GB RAM, expecting to run for 60mins,
using the optimal kmer value determined by kmer genie on the raw pe1 dataset.  The kmer_calc stage looks ahead to run on
dataset configurations for each MASS job.

In the job element there are two required attributes: "name" and "tool".  The name attribute is primarily used as the name
of the output directory for this job, but it also provides a way of referring to this job from other parts of the pipeline.
The tool attribute must represent one of the supported assemblers, and take one of the assemblers values defined at the
start of this chapter, or in the environment config section of the documentation.

There are also several optional attributes: "threads", "memory", "exp_walltime", "checked_args", "unchecked_args".  The
value entered to threads will be passed to the tool and the scheduler to define the number of threads required for this
job.  memory may get passed to the tool, depending on whether the tool requires it, but will get passed to the scheduler.
exp_walltime, will just go to the scheduler.  It's important to understand how your scheduler works before entering these
values.  The intention is that these figures will represent guidelines to help the scheduler organise it's workload fairly,
such as LSF.  However other schedulers may define these as hardlimits.  For example on PBS there is no notion of "expected"
walltime, only a hard limited walltime, so we double the value entered here in order to create a conservative hard limit
instead.  checked and unchecked args are described later in this section.


Varying kmers for De Bruijn Graph assemblers
--------------------------------------------

Many DeBruijn graph assemblers require you to specify a parameter that defines the kmer size to use in the graph.  It is
not obvious before running the assembly which kmer value will work best and so a common approach to the problem is to
try many kmers to "optimise" to the kmer parameter.  RAMPART allows the user to do this in two different ways.

First, RAMPART supports kmergenie.  If the user enters the kmergenie element in the mass element then kmer genie is used
to determine the best kmer values to use for each distinct mass configuration.  For example, if the same single dataset is used
for each mass job then kmergeneie is run once, and that optimal kmer value is passed on to each mass job.  If different
datasets are used for building contigs in different mass jobs then RAMPART will automatically work out in which combinations
of kmer genie need to be run to drive the pipeline.

The alternative way is to manually specify which kmer to use or to request a kmer spread, i.e. to define the range of kmer
values that should be tried.  This maybe necessary, if for example you would like to do your own analysis of the resultant
assemblies, or if kmer genie fails on your dataset.  If the user specifies both kmergenie and a manual kmer spread, then
the manual kmer spread will override the kmergenie recommendation.
The snippet below shows how to run Abyss using a spread of kmer values::

   <mass>
      <job name="abyss-raw-kmer" tool="ABYSS_V1.5" threads="16" memory="4000">
         <kmer min="61" max="101" step="COARSE"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
   </mass>

As you can see the XML element starting ``<kmer`` has been modified to specify a min, max and step value.  Min and max
obviously set the limits of the kmer range.  You can omit the min and/or max values.  If so the default min value is set
to 35 and the default max value will be automatically determined by the provided input libraries.  Specifically, the default
max K will be 1 less than the read length of the library with smallest read length.

The step value, controls how large the step should be between each assembly.
The valid options include any integer between 2 and 100.  We also provide some special keywords to define step size:
``FINE,MEDIUM,COARSE``, which correspond to steps of ``4,10,20`` respectively.  Alternatively, you can
simply specify a list of kmer values to test.  The following examples all represent the same
kmer range (61,71,81,91,101)::

   <kmer min="61" max="101" step="10"/>
   <kmer min="61" max="101" step="MEDIUM"/>
   <kmer list="61,71,81,91,101"/>

Note: Depending on the assembler used the values specified for the kmer range, the actual assemblies generated may be
executed with slightly different values.  For example some assemblers do not allow you to use kmers of even value.  Others
may try to optimise the k parameter themselves.  We therefore make a best effort to match the requested RAMPART kmer
range to the actual kmer range executed by the assembler.

Assemblers such as SPAdes and Platanus have their own K optimisation strategies.  In these cases, instead of running
multiple instances of these assemblers, RAMPART will run a single instance, and translate the kmer range information
into the parameters suitable for these assemblers.

Some De Bruijn graph assemblers, such as ALLPATHS-LG, recommend that you do not modify the kmer value.  In these cases
RAMPART lets the assembler manage the k value.  If the selected assembler does require you to specify a k value, and
you omit the kmer element from the config, then RAMPART specifies a default kmer spread for you.  This will have a min
value of 35, the max is automatically determined from the provided libraries as described above, and the step is 20 (COARSE).



Varying coverage
----------------

In the past, sequencing was expensive and slow, which led to sequencing coverage of a genome to be relatively low.  In
those days, you typically would use all the data you could get in your assembly.  These days, sequencing is relatively
cheap and it is often possible to over sequence data, to the point where the gains in terms of separating signal from
noise become irrelevant.  Typically, a sequencing depth of 100X is more than sufficient for most purposes.  Furthermore, over
sequencing doesn't just present problems in terms of data storage, RAM usage and runtime, it also can degrade the
quality of some assemblies.  One common reason for failed assemblies with high coverage can occur if trying to assemble
DNA sequenced from populations rather than a single individual.  The natural variation in the data can make it impossible
to construct unambiguous seed sequences to start a *De Brujin* graph.

Therefore RAMPART offers the ability to randomly subsample the reads to a desired level of coverage.  It does this
either by using the assembler's own subsampling functionality if present (ALLPATHS-LG does have this functionality), or
it will use an external tool developed by TGAC to do this if the assembler doesn't have this functionality.  In both
cases user's interface to this is identical, and an example is shown below::

   <mass>
      <job name="abyss-raw-cvg" tool="ABYSS_V1.5" threads="16" memory="4000">
         <coverage min="50" max="100" step="MEDIUM" all="true"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
   </mass>

This snippet says to run Abyss varying the coverage between 50X to 100X using a medium step.  It also says to run an
abyss assembly using all the reads.  The step options has the following valid values: ``FINE, MEDIUM, COARSE``, which
correspond to steps of: ``10X, 25X, 50X``.  If the user does not wish to run an assembly with all the reads, then they
should set the all option to false.


Varying other variables
-----------------------

MASS provides a mechanism to vary most parameters of any assembler.  This is done with the ``var`` element, and there can
be only one ``var`` element per MASS job.  The parameter name should be specified by an attribute called ``name`` in that
element and the values to test should be put in a single comma separated string under an attribute called ``values``.  For
example, should you wish to alter the coverage cutoff parameter in the velvet assembler you might write something like this::

   <mass>
      <job name="velvet-cc" tool="VELVET_V1.2" threads="16" memory="8000">
         <kmer list="75"/>
         <var name="cov_cutoff" values="2,5,10,auto"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
   </mass>


Note that in this example we set the kmer value to 75 for all tests.  If the kmer value is not specified then the default
for the assembler should be used.


Using multiple input libraries
------------------------------

You can add more than one input library for most assemblers.  You can specify additional libraries to the MASS job by
simply adding additional ``input`` elements inside the ``inputs`` element.

MASS supports the ALLPATHS-LG assembler, which has particular requirements for its input: a so-called fragment library and a jumping
library.  In RAMPART nomenclature, we would refer to a fragment library, as either an overlapping paired end library,
and a jumping library as either a paired end or mate pair library.  ALLPATHS-LG also has the concept of a long jump
library and long library.  RAMPART will translate mate pair libraries with an insert size > 20KBP as long jump libraries
and single end reads longer than 500BP as long libraries.

An simple example of ALLPATHS-LG run, using a single fragment and jumping library is shown below::

   <mass>
      <job name="allpaths-raw" tool="ALLPATHSLG_V50" threads="16" memory="16000">
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </job>
   </mass>




Multiple MASS runs
------------------

It is possible to ask MASS to conduct several MASS runs.  You may wish to do this for several reasons.  The first might
be to compare different assemblers, another reason might be to vary the input data being provided to a single assembler.

The example below shows how to run a spread of Abyss assemblies and a single ALLPATHS assembly on the same data::

   <mass parallel="true">
      <job name="abyss-raw-kmer" tool="ABYSS_V1.5" threads="16" memory="4000">
         <kmer min="65" max="85" step="MEDIUM"/>
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </job>
      <job name="allpaths-raw" tool="ALLPATHSLG_V50" threads="16" memory="16000">
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </job>
   </mass>

Note that the attribute in MASS called ``parallel`` has been added and set to true.  This says to run the Abyss and
ALLPATHS assemblies in parallel in your environment.  Typically, you would be running on a cluster or some other HPC
architecture when doing this.

The next example, shows running two sets of abyss assemblies (not in parallel this time) each varying kmer values in the
same way, but one set running on error corrected data, the other on raw data::

   <mass parallel="false">
      <job name="abyss-raw-kmer" tool="ABYSS_V1.5" threads="16" memory="4000">
         <kmer min="65" max="85" step="MEDIUM"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
      <job name="abyss-raw-kmer" tool="ABYSS_V1.5" threads="16" memory="4000">
         <inputs>
            <input ecq="quake" lib="pe1"/>
         </inputs>
      </job>
   </mass>

Adding other command line arguments to the assembler
----------------------------------------------------

MASS offers two ways to add command line arguments to the assembler.  The first is via a POSIX format string containing
command line options/arguments that should be checked/validated as soon as the configuration file is parsed.  Checked
arguments undergo a limited amount of validation to check the argument name is recognized and that the argument values
(if required) are plausible.  The second method is to add a string containing unchecked arguments directly to the assembler
verbatim.  This second method is not recommended in general because any syntax error in the options will only register
once the assembler starts running, which maybe well into the workflow.  However, it is useful for working around problems that can't
be easily fixed in any other way.  For example, checked args only work if the developer has properly implemented handling
of the argument in the assembler wrapper script.  If this has not been implemented then the only way to work around the
problem is to use unchecked arguments.

The following example demonstrates how to set some checked and unchecked arguments for Abyss::

   <mass>
      <job name="abyss" tool="ABYSS_V1.5" threads="16" memory="16000"
            checked_args="-n 20 -t 250"
            unchecked_args="p=0.8 q=5 s=300 S=350">
         <kmer list="83"/>
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </job>
   </mass>

Note that we use POSIX format for the checked arguments, regardless of what the underlying tool typically would expect.
Unchecked arguments are passed verbatim to the tool.

You should also ensure that care is taken not to override variables, otherwise unpredictable behaviour will occur.  In
general options related to input libraries, threads/cpus, memory and kmer values are set separately.  Also remember not
to override arguments that you may be varying using a ``var`` element.


Navigating the directory structure
----------------------------------

Once MASS starts it will create a directory within the job's output directory called ``mass``.  Inside this directory you
might expect to see something like this::

  - <Job output directory>
  -- mass
  --- <mass_job_name>
  ---- <assembly> (contains output from the assembler for this assembly)
  ---- ...
  ---- unitigs (contains links to unitigs for each assembly and analysis of unitigs)
  ---- contigs (contains links to contigs for each assembly and analysis of contigs)
  ---- scaffolds (contains links to scaffolds for each assembly and analysis of scaffolds)
  --- ...

The directory structure is created as the assemblers run.  So the full file structure may not be visible straight after
MASS starts.  Also, we create the symbolic links to unitigs, contigs and scaffolds on an as needed basis.  Some assemblers
may not produce certain types of assembled sequences and in those cases we do not create the associated links directory.


Troubleshooting
---------------

Here are some issues that you might run into during the MASS stage:

1. ABySS installed but without MPI support. RAMPART requires ABySS to be configured with openmpi in order to use
parallelisation in ABySS.  If you encounter the following error message lease reinstall ABySS and specify the --with-mpi
option during configuration::

  mpirun was unable to find the specified executable file, and therefore did not launch the job.  This error was first
  reported for process rank 0; it may have occurred for other processes as well.

  NOTE: A common cause for this error is misspelling a mpirun command
      line parameter option (remember that mpirun interprets the first
      unrecognized command line token as the executable).



