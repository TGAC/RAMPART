
.. _mass:

MASS - Multiple Assembly Creation
=================================

This tool enables the user to try different assemblers with different settings and automatically compare the assemblies.
MASS can select an assembly out of the set that scores highest.  Scoring depends on the kinds of evaulation that are
performed and how the user wants to weight specific metrics.

Currently, the following assemblers are supported by RAMPART:

* Abyss V1.3
* ALLPATHS-LG V44837

A simple MASS step might be configured as follows::

   <mass>
      <job name="abyss-raw-kmer" tool="ABYSS_V1.3" threads="16" memory="4000">
         <kmer list="75"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
   </mass>

This instructs RAMPART to run a single Abyss assembly using 16 threads, requesting 4GB RAM, with kmer value 75 on the
raw pe1 dataset.  It will also analyse the assemblies contiguity and conduct a kmer analysis, comparing kmers in the
raw reads to those found in the assembly.

MASS also supports ALLPATHS-LG, which has more requirements for its input: a so-called fragment library and a jumping
library.  In RAMPART nomenclature, we would refer to a fragment library, as either an overlapping paired end library,
and a jumping library as either a paired end or mate pair library.  ALLPATHS-LG also has the concept of a long jump
library and long library.  RAMPART will translate mate pair libraries with an insert size > 20KB as long jump libraries
and single end reads longer than 500B as long libraries.

An simple example of ALLPATHS-LG run, using a single fragment and jumping library is shown below::

   <mass>
      <job name="allpaths-raw" tool="ALLPATHS-LG_V44837" threads="16" memory="16000">
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </job>
   </mass>


Varying kmers for De Bruijn Graph assemblers
--------------------------------------------

Many DeBruijn graph assemblers require you to specify a parameter that defines the kmer size to use in the graph.  It is
not obvious before running the assembly which kmer value will work best and so a common approach to the problem is to
try many kmers to "optimise" to the kmer parameter.  RAMPART allows the user to define the range of kmer values that should
 be tried.

The snippet below shows how to run Abyss using a spread of kmer values::

   <mass>
      <job name="abyss-raw-kmer" tool="ABYSS_V1.3" threads="16" memory="4000">
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
may try to optimise the k parameter automatically and may not let you specify specific k values.  We therefore make a best
effort to match the requested RAMPART kmer range to the actual kmer range executed by the assembler.

Some De Bruijn graph assemblers, such as ALLPATHS-LG, recommend that you do not modify the kmer value.  In these cases
it is permissible to omit the kmer element, and this will simply let the assembler decide how to choose the k value.  If
the selected assembler does require you to specify a k value, and you omit the kmer element from the config, then RAMPART
specifies a default kmer spread for you.  This will have a min value of 35, the max is automatically determined from the
provided libraries as described above, and the step is 10 (MEDIUM).

In the future, we  may try to integrate tools such as KmerGenie http://kmergenie.bx.psu.edu/ into RAMPART in order to
guess the best kmer value prior to assembly, thus allowing the user to skip the computationally expensive process of testing
multiple kmer values.


Varying coverage
----------------

In the past, sequencing was expensive and slow, which led to sequencing coverage of a genome to be relatively low.  In
those days, you typically would use all the data you could get in your assembly.  These days, sequencing is relatively
cheap and it is often possible to over sequence data, to the point where the gains in terms of separting signal from
noise become irrelevant.  Typically, a sequencing depth of 100X is sufficient for most purposes.  Furthermore, over
sequencing doesn't just present problems in terms of data storage, RAM usage and runtime, it also can degrade the
quality of some assemblies.  One common reason for failed assemblies with high coverage can occur if trying to assemble
DNA sequenced from populations rather than a single individual.  The natural variation in the data can make it impossible
to construct unambiguous seed sequences to start a *De Brujin* graph.

Therefore RAMPART offers the ability to randomly subsample the reads to a desired level of coverage.  It does this
either by using the assembler's own subsampling functionality if present (ALLPATHS-LG does have this functionality), or
it will use an external tool developed by TGAC to do this if the assembler doesn't have this functionality.  In both
cases user's interface to this is identical, and an example is shown below::

   <mass>
      <job name="abyss-raw-cvg" tool="ABYSS_V1.3" threads="16" memory="4000">
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



Multiple MASS runs
------------------

It is possible to ask MASS to conduct several MASS runs.  You may wish to do this for several reasons.  The first might
be to compare different assemblers, another reason might be to vary the input data being provided to a single assembler.

The example below shows how to run a spread of Abyss assemblies and a single ALLPATHS assembly on the same data::

   <mass parallel="true">
      <job name="abyss-raw-kmer" tool="ABYSS_V1.3" threads="16" memory="4000">
         <kmer min="65" max="85" step="MEDIUM"/>
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </job>
      <job name="allpaths-raw" tool="ALLPATHS-LG_V44837" threads="16" memory="16000">
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
      <job name="abyss-raw-kmer" tool="ABYSS_V1.3" threads="16" memory="4000">
         <kmer min="65" max="85" step="MEDIUM"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </job>
      <job name="allpaths-raw" tool="ALLPATHS-LG_V44837" threads="16" memory="16000">
         <inputs>
            <input ecq="quake" lib="pe1"/>
         </inputs>
      </job>
   </mass>


Navigating the directory structure
----------------------------------

MASS will take input from the MECQ and KMER stages, and is controlled via the job configuration file.  Once it has been
executed it will create a directory within the job's output directory called ``mass``.  Inside this directory

As an example, you might expect to see something like this::

  - <Job output directory>
  -- mass
  --- <single_mass_id>
  ---- <assembly> (contains output from the assembler for this assembly)
  ---- ...
  ---- unitigs (contains links to unitigs for each assembly and analysis of unitigs)
  ---- contigs (contains links to contigs for each assembly and analysis of contigs)
  ---- scaffolds (contains links to scaffolds for each assembly and analysis of scaffolds)
  --- ...

The file best.fa is particularly important as this is the assembly that will be taken forward to the AMP / FINALISE
stage.  If you are not happy with RAMPART's choice of assembly you should replace best.fa with your selection and re-run
the rampart pipeline from the AMP stage: ``rampart -s AMP,FINALISE job.cfg``.

