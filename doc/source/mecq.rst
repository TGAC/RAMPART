
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

It is also worth noting that some assemblers perform their own error correction, for example, ALLPATHS-LG.  Meaning that
additional error correction via tools such as quake would not be significantly beneficial.  It is a complicated topic
and the decision to quality trim and error correct reads is left to the user.  RAMPART makes it simpler to incorperate
this data into the assembly pipeline.  The authors advice is to learn the tools well, understand how they work,
understand your data and how read pre-processing might benefit or detract from the final assemblies.  Finally, RAMPART
offers a suitable platform for testing these combinations out, so if you have the time and computational resources, it
might be worth experimenting with different permutations.

Whilst new tools will be added as and when the are needed, currently MECQ supports the following tools:

* Sickle V1.1
* Quake V0.3
* Musket V1.0

An example XML snippet demonstrating how to run two different tools in parallel, one on two datasets, the other on a
single dataset::

   <mecq parallel="false">
      <ecq name="sickle_agressive" tool="SICKLE_V1.2" libs="lib1896-pe,lib1897-mp"/>
      <ecq name="quake" tool="QUAKE_V0.3" libs="lib1896-pe1"
           threads="4" memory="2000"/>
   </mecq>

MECQ produces output in the ``mecq`` directory produced in the specified job output directory.  The directory will
contain sub-directories relating to each ``ecq`` element described in the XML snippet, then further sub-directories
relating to the specified libraries used for that ``ecq``.  The next steps in the pipeline (KMER and MASS) know how to
read this directory structure to get their input automatically.

