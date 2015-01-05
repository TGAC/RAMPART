
.. _analyse_reads:

Analysing reads
===============

This stage analyses all datasets, both the RAW and those, if any, which have been produced by the MECQ stage.

Currently, the only analysis option provided involves a kmer analysis, using tools called jellyfish and KAT.  This
process will produce GC vs kmer frequence plots, which can highlight potential contamination and indicate whether you
have sufficient coverage in your datasets for assembling your genome.

The user has the option to control, the number of threads and amount of memory to request per process and whether or not the
kmer counting for each dataset should take place in parallel.  An example of this is shown below::

   <analyse_reads kmer="true" parallel="true" threads="16" memory="4000"/>

Note: This step is required if you wish to count kmers in the assemblies and compare the kmer content of reads to assemblies.
See _ref::mass for more details.

