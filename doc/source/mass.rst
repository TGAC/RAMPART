
.. _mass:

MASS - Multiple Assembly Creation
=================================

This tool enables the user to try different assemblers with different settings and automatically compare the assemblies.
MASS can select an assembly out of the set that scores highest.  Scoring depends on the kinds of evaulation that are
performed and how the user wants to weight specific metrics.

Currently, the following assemblers are supported by RAMPART:

* Abyss V1.3.X
* ALLPATHS-LG V44837

A simple MASS step might be configured as follows::

   <mass>
      <single_mass name="abyss-raw-kmer" tool="ABYSS_V1.3.4" threads="16" memory="4000">
         <kmer list="75"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </single_mass>
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
      <single_mass name="allpaths-raw" tool="ALLPATHS-LG_V44837" threads="16" memory="16000">
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </single_mass>
   </mass>

Note that no kmer value is required to run ALLPATHS-LG.


Varying kmers
-------------

Typically, when running ALLPATHS-LG isn't an option, it is necessary to select a kmer value to use in a De Bruijin
assembler.  Deciding on a kmer value is not a trivial task.  Tools such as KmerGenie http://kmergenie.bx.psu.edu/ will
try to guess the best kmer value prior to assembly.  In the future we might integrate this tool into RAMPART, but for
the time being we offer an alternative, and admittedly computationally more expensive way of determining the best kmer
value... and that is to try a range of assemblies with differing k-values.  This brute force approach has the benefit of
providing hard data, in the form of actual assemblies, which can be directly compared.

The snippet below shows how to run Abyss using a spread of kmer values::

   <mass>
      <single_mass name="abyss-raw-kmer" tool="ABYSS_V1.3.4" threads="16" memory="4000">
         <kmer min="61" max="101" step="COARSE"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </single_mass>
   </mass>

As you can see the XML element starting ``<kmer`` has been modified to specify a min, max and step value.  Min and max
obviously set the limits of the kmer range.  The step value, controls how large the step should be between each assembly.
The valid options include: ``FINE,MEDIUM,COARSE``, which correspond to steps of: ``2,4,10``.  Alternatively, you can
simply specify a list of kmer values to test.  The snippet for this running assemblies using kmers 75, 81 and 95 would
look like this::

   <kmer list="75,81,95"/>



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
      <single_mass name="abyss-raw-cvg" tool="ABYSS_V1.3.4" threads="16" memory="4000">
         <coverage min="50" max="100" step="MEDIUM" all="true"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </single_mass>
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
      <single_mass name="abyss-raw-kmer" tool="ABYSS_V1.3.4" threads="16" memory="4000">
         <kmer min="65" max="85" step="MEDIUM"/>
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </single_mass>
      <single_mass name="allpaths-raw" tool="ALLPATHS-LG_V44837" threads="16" memory="16000">
         <inputs>
            <input ecq="raw" lib="ope1"/>
            <input ecq="raw" lib="mp1"/>
         </inputs>
      </single_mass>
   </mass>

Note that the attribute in MASS called ``parallel`` has been added and set to true.  This says to run the Abyss and
ALLPATHS assemblies in parallel in your environment.  Typically, you would be running on a cluster or some other HPC
architecture when doing this.

The next example, shows running two sets of abyss assemblies (not in parallel this time) each varying kmer values in the
same way, but one set running on error corrected data, the other on raw data::

   <mass parallel="false">
      <single_mass name="abyss-raw-kmer" tool="ABYSS_V1.3.4" threads="16" memory="4000">
         <kmer min="65" max="85" step="MEDIUM"/>
         <inputs>
            <input ecq="raw" lib="pe1"/>
         </inputs>
      </single_mass>
      <single_mass name="allpaths-raw" tool="ALLPATHS-LG_V44837" threads="16" memory="16000">
         <inputs>
            <input ecq="quake" lib="pe1"/>
         </inputs>
      </single_mass>
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

