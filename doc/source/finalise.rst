
.. _finalise:

Finaliser
=========

The final step in the RAMPART pipeline is to finalise the assembly.  Currently, this simply involves standardising the
assembly filename and headers in the fasta file.  The finaliser can be invoked this like to use the specifed prefix::

   <finalise prefix="E.coli_Sample1_V1.0"/>

If a prefix is not specified RAMPART will build it's own prefix based on the information provided in the job configuration
file, particularly from the organism details.

The finalising process will take scaffolds and artifically break them into contigs where there are large gaps present.
The size of the gap required to trigger a break into contigs is defined by the ``min_n`` attribute.  By default this is
set to 10.  An example, that applies a prefix and breaks to contigs only on gaps larger than size 20 is shown below::

   <finalise prefix="E.coli_Sample1_V1.0" min_n="20"/>

The input from this stage will either be the best assembly selected from MASS, or the final assembly produced by AMP
depending on how you've setup your job.  The output from this stage will be as follows:

* ``<prefix>.scaffolds.fa`` (the final assembly which can be used for annotation and downstream analysis)
* ``<prefix>.contigs.fa`` (the final set of scaffolds are broken up where stretches of N's exceed a certain limit)
* ``<prefix>.agp`` (a description of where the contigs fit into the scaffolds)
* ``<prefix>.translate`` (how the fasta header names translate back to the input assembly)

