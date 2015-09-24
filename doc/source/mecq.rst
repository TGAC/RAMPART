
.. _mecq:
   
MECQ - Multiple Error Correction and Quality Trimming Tool
==========================================================

The purpose of this step is to try to improve the input datasets.  The user can select from a number of seperate tools
can be executed on one or more of the input datasets provided.  The user can also request whether or not the tools
should be run linearly or in parallel.

Attempting to improve the dataset is a slightly controversial topic.  Although, it is true that having good quality data
is critical to creating a good assembly, then benefits from trimming and correcting input data are debatable.  It is
certainly true that error correction tools in particular can boost assembly contiguity, however this can occasionally
come at the expense of missassemblies.  In addition, trimming reads can alter the kmer-coverage statistics for the
dataset and in turn confuse assemblers into making incorrect choices.

It is also worth noting that some assemblers perform their own error correction, for example, ALLPATHS-LG and SPAdes.  Meaning that
additional error correction via tools such as quake would not be significantly beneficial.  It is a complicated topic
and the decision to quality trim and error correct reads is left to the user.  RAMPART makes it simpler to incorperate
this kind of process into the assembly pipeline for assemblers which don't already have this option built in.
The authors advice is to learn the error correction and assembly tools well, understand how they work and
understand your data.  Finally, RAMPART offers a suitable platform for testing these combinations out, so if you have
the time and computational resources, it might be worth experimenting with different permutations.

Whilst new tools will be added as and when the are needed, currently MECQ supports the following tools:

* Sickle V1.1
* Quake V0.3
* Musket V1.0
* TrimGalore V

An example XML snippet demonstrating how to run two different tools in parallel, one on two datasets, the other on a
single dataset::

   <mecq parallel="false">
      <ecq name="sickle_agressive" tool="SICKLE_V1.2" libs="lib1896-pe,lib1897-mp"/>
      <ecq name="quake" tool="QUAKE_V0.3" libs="lib1896-pe1"
           threads="4" memory="8000"/>
   </mecq>

MECQ produces output in the ``mecq`` directory produced in the specified job output directory.  The directory will
contain sub-directories relating to each ``ecq`` element described in the XML snippet, then further sub-directories
relating to the specified libraries used for that ``ecq``.  The next steps in the pipeline (KMER and MASS) know how to
read this directory structure to get their input automatically.


Adding other command line arguments to the error corrector
----------------------------------------------------------

MECQ offers two ways to add command line arguments to the error corrector.  The first is via a POSIX format string containing
command line options/arguments that should be checked/validated as soon as the configuration file is parsed.  Checked
arguments undergo a limited amount of validation to check the argument name is recognized and that the argument values
(if required) are plausible.  The second method is to add a string containing unchecked arguments directly to the assembler
verbatim.  This second method is not recommended in general because any syntax error in the options will only register
once the assembler starts running, which maybe well into the workflow.  However, it is useful for working around problems that can't
be easily fixed in any other way.  For example, checked args only work if the developer has properly implemented handling
of the argument in the error corrector wrapper.  If this has not been implemented then the only way to work around the
problem is to use unchecked arguments.

The following example demonstrates how to set some checked and unchecked arguments for Quake::

   <mecq>
      <ecq name="quake" tool="QUAKE_V0.3" threads="16" memory="16000"
            libs="lib1896-pe1"
            checked_args="-k 19 -q 30 --hash_size=10000000"
            unchecked_args="-l --log"/>
      </job>
   </mass>

Note that we use POSIX format for the checked arguments, regardless of what the underlying tool typically would expect.
Unchecked arguments are passed verbatim to the tool.

You should also ensure that care is taken not to override variables, otherwise unpredictable behaviour will occur.  In
general options related to input libraries, threads/cpus and memory values are set separately.  Also take care
not to override the same options in both checked_args and unchecked_args.
