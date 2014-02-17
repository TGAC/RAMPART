.. _amp:

AMP - Assembly Improver
=======================

This stage takes the selected assembly from MASS and tries to improve it.  For example, additional scaffolding, gap
filling or sequence filtering can be performed at this stage.  A simple XML snippet describing exactly this is shown
below::

   <amp>
      <stage tool="SSPACE_Basic_V2.0"/>
      <stage tool="GapCloser_V1.12"/>
   </amp>

Each stage in the AMP pipeline must necessarily run linearly as each stage requires the output from the previous stage.
The user can specify additional arguments to each tool by adding the ``args`` attribute to each stage.  For example to
specify that SSPACE should use PE reads to extend into gaps and to cap min contig length to scaffold as 1KB::

   <amp>
      <stage tool="SSPACE_Basic_V2.0" args="-x 1 -z 1000"/>
      <stage tool="GapCloser_V1.12"/>
   </amp>

Output from amp will be placed in a directory called ``amp`` within the job's output directory.  Output from each stage
will be placed in a sub-directory within this and a link will be created to the final assembly produced at the end of
the amp pipeline.  This assembly will be used in the next stage.


