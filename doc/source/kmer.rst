
.. _kmer:

Kmer counting reads
===================

Does K-mer counting on all datasets, both the RAW and those, if any, which have been produced by the MECQ stage.  The kmer counting is done with a tool called jellyfish.  The user has the option to control, the number of threads and amount of memory to request per process and whether or not the counting for each dataset should take place in parallel.  An example of this is shown below::

   <kmer-reads parallel="true" threads="16" memory="4000"/>

This step is required if you wish to count kmers in the assemblies and compare the kmer content of reads to assemblies.  See _ref::mass for more details.

