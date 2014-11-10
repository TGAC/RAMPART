
.. _analyse_assemblies:

Analyse assemblies
==================

RAMPART currently offers 3 assembly analysis options:

* Contiguity
* Kmer read-assembly comparison
* Completeness

These types of analyses can be executed in either the ``analyse_mass`` or ``analyse_amp`` pipeline element.  The available
tool options for the analyses are: QUAST,KAT,CEGMA.

QUAST, compares the assemblies from a contiguity perspective.  This tool runs really fast, and produces statistics such
as the N50, assembly size, max sequence length.  It also produces a nice html report showing cumulative length
distribution curves for each assembly and GC content curves.

KAT, performs a kmer count on the assembly using Jellyfish, and, assuming kmer counting was requested on the reads
previously, will use the Kmer Analysis Toolkit (KAT) to create a comparison matrix comparing kmer counts in the reads to
the assembly.  This can be visualised later using KAT to show how much of the content in the reads has been assembled
and how repetitive the assembly is.  Repetition could be due to heterozygosity in the diploid genomes so please read the
KAT manual and walkthrough guide to get a better understanding of how to interpret this data.

CEGMA aligns highly conserved eukaryotic genes to the assembly.  CEGMA produces a statistic which represents an estimate
of gene completeness in the assembly.  i.e. if we see CEGMA maps 95% of the conserved genes to the assembly we can
assume that the assembly is approximately 95% complete.  This is a very rough guide and shouldn't be taken
literally, but can be useful when comparing other assemblies made from the same data.  CEGMA has a couple of other
disadvantages however, first it is quite slow, second it only works on eukaryotic organisms so is useless for bacteria.

An example snippet for a simple and fast contiguity based analyses is as follows::

  <analyse_mass>
     <tool name="QUAST" threads="16" memory="4000"/>
  </analyse_mass>

In fact we strongly recommend you use QUAST for all your analyses.  Most of RAMPART's system for scoring assemblies (see
below) is derived from Quast metrics and it also runs really fast.  The runtime is insignificant when compared to the time
taken to create assemblies.  For more a complete analysis, which will take a significant amount of time for each assembly,
you can request KAT and CEGMA.  An example showing the use of all analysis tools is as follows::

  <analyse_mass parallel="false">
     <tool name="QUAST" threads="16" memory="4000"/>
     <tool name="KAT" threads="16" memory="50000" parallel="true"/>
     <tool name="CEGMA" threads="16" memory="20000"/>
  </analyse_mass>

Note that you can apply ``parallel`` attributes to both the ``analyse_mass`` and individual tool elements.  This enables you
 to select those process to be run in parallel where possible.  Setting ``parallel="true"`` for the ``analyse_mass`` element
 will override ``parallel`` attribute values for specific tools.

Selecting the best assembly
---------------------------

Assuming at least one analysis option is selected, RAMPART will produce a table listing each assembly as a row, with each
column representing an assembly metric.  The user can specify a weighting file when running RAMPART to assign the
weights to each metric.  Each assembly is then assigned a score, based on the weighted mean of the metrics, and the
assembly with the highest score is then automatically selected as the **best** assembly to be used downstream.

To use these default weightings simply add the following element to the pipeline::

  <select_mass/>

Should the user wish to override the default weights that are assigned to each assembly metric, they can do so by
setting the ``weightings_file`` attribute element.  For example, using an absolute path to a custom
weightings file the XML snippet may look like this::

   <select_mass weightings_file="~/.tgac/rampart/custom_weightings.tab"/>

The format of the weightings file is a pipe separated table as follows::

   nb_seqs|nb_seqs_gt_1k|nb_bases|nb_bases_gt_1k|max_len|n50|l50|gc%|n%|nb_genes|completeness
   0.05|0.1|0.05|0.05|0.05|0.2|0.05|0.05|0.1|0.5|0.25

All the metrics are derived from Quast results, except for the last one.


TODO: Currently the kmer metric, is not included.  In the future this will offer an alternate means of assessing the
assembly completeness.

The file best.fa is particularly important as this is the assembly that will be taken forward to the AMP / FINALISE
stage.  If you are not happy with RAMPART's choice of assembly you should replace best.fa with your selection and re-run
the rampart pipeline from the AMP stage: ``rampart -s AMP,FINALISE job.cfg``.


Analysing assemblies produced by AMP
------------------------------------

In addition, to analysing assemblies produced by the MASS stage, the same set of anlayses can also be applied to assemblies
produced by the AMP stage.  The same set of attributes that can be applied to ``analyse_mass`` can be applied to ``analyse_amp``.
In addition, it is also possible to specify an additional attribute: ``analyse_all``, which instructs RAMPART to analyse
 assemblies produced at every stage of the AMP pipeline.  By default only the final assembly is analysed.  Also note that
 there is no need to select assemblies from AMP, so there is no corresponding ``select_amp`` element.
