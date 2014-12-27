
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
KAT manual and walkthrough guide to get a better understanding of how to interpret this data.   Note that information
for KAT is not automatically used for selecting the best assembly at present.  See next section for more information about
automatic assembly selection.

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

Assuming at least one analysis option is selected, RAMPART will produce a summary file and a tab separated value file listing
metrics for each assembly, along with scores relating to the contiguity, conservation and problem metrics, and a final overall score
for each assembly.  Each score is given a value between 0.0 and 1.0, where higher values represent better assemblies.  The
assembly with the highest score is then automatically selected as the **best** assembly to be used downstream.
The group scores and the final scores are derived from underlying metrics and can be adjusted to have
different weightings applied to them. This is done by specifying a weighting file to use in the RAMPART pipeline.

By default RAMPART applies its own weightings, which can be found at ``<rampart_dir>/etc/weightings.tab``, so to run the
assembly selection stage with default settings the user simply needs add the following element to the pipeline::

  <select_mass/>

Should the user wish to override the default weights that are assigned to each assembly metric, they can do so by
setting the ``weightings_file`` attribute element.  For example, using an absolute path to a custom
weightings file the XML snippet may look like this::

   <select_mass weightings_file="~/.tgac/rampart/custom_weightings.tab"/>

The format of the weightings key value pair file separated by '=' character.  Comment lines can start using '#'.
Most metrics are derived from Quast results, except for the core eukaryote genes detection score which is gathered from CEGMA.  Note, that some metrics
from Quast will only be used in certain circumstances.  For example, the na50 and nb_ma_ref metrics are only used if a
reference is supplied in the organism element of the configuration file.  Additionally, the nb_bases, nb_bases_gt_1k and
the gc% metrics are used only if the user has supplied either a reference, or has provided estimated size and / or estimated
gc% for the organism respectively.

TODO: Currently the kmer metric, is not included.  In the future this will offer an alternate means of assessing the
assembly completeness.

The file best.fa is particularly important as this is the assembly that will be taken forward to the second half of the pipeline
(from the AMP stage).  Although we have found that scoring system to be generally quite useful, we strongly recommend users
to make their own assessment as to which assembly to take forward as we acknowledge that the scoring system is biased by
outlier assemblies.  For example, consider three assemblies with an N50 of 1000, 1100 and 1200 bp, with scaled scores of
0, 0.5 and 1. We add a third assembly, which does poorly and is disregarded by the user, with an N50 of 200 bp. Now the
weighted N50 scores of the assemblies are 0, 0.8, 0.9 and 1. Even though the user has no intention of using that poor
assembly, the effective weight of the N50 metric of the three good assemblies has decreased drastically by a factor of
(1 - 0) / (1 - 0.8) = 5.  It's possible that the assembly selected as the best would change by adding an irrelevant assembly.
For example consider two metrics, a and b, with even weights of 0.5 for three assemblies, and then again for four assemblies
after adding a fourth irrelevant assembly, which performs worst in both metrics. By adding a fourth irrelevant assembly,
the choice of the best assembly has changed.

Three assemblies:
a = {1000, 1100, 1200}, b = {0, 10, 8}
sa = {0, 0.5, 1}, sb = {0, 1, 0.8}
fa = {0, 0.75, 0.9}
best = 0.9, the assembly with a = 1200

Four assemblies:
a = {200, 1000, 1100, 1200}, b = {0, 0, 10, 8}
sa = {0, 0.8, 0.9, 1}, sb = {0, 0, 1, 0.8}
fa = {0, 0.4, 0.95, 0.9}
best = 0.95, the assembly with a = 1100

To reiterate, we recommend that the user double check the results provided by RAMPART and if necessary overrule the choice
of assembly selected for further processing.  This can be done, i.e. starting from the AMP stage with a user selected
assembly, by using the following command: ``rampart -2 -a <path_to_assembly> <path_to_job_config>``.


Analysing assemblies produced by AMP
------------------------------------

In addition, to analysing assemblies produced by the MASS stage, the same set of anlayses can also be applied to assemblies
produced by the AMP stage.  The same set of attributes that can be applied to ``analyse_mass`` can be applied to ``analyse_amp``.
In addition, it is also possible to specify an additional attribute: ``analyse_all``, which instructs RAMPART to analyse
 assemblies produced at every stage of the AMP pipeline.  By default only the final assembly is analysed.  Also note that
 there is no need to select assemblies from AMP, so there is no corresponding ``select_amp`` element.
