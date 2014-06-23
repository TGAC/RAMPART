.. _amp:

AMP - Assembly Improver
=======================

This stage takes a single assembly as input and tries to improve it.  For example, additional scaffolding, gap
filling can be performed at this stage.  A simple XML snippet describing exactly this is shown
below::

   <amp>
      <stage tool="SSPACE_Basic_V2.0">
         <inputs>
            <input name="mp1" type="raw"/>
         </inputs>
      </stage>
      <stage tool="GapCloser_V1.12">
         <inputs>
            <input name="pe1" type="raw"/>
         </inputs>
      </stage>
   </amp>

Each stage in the AMP pipeline must necessarily run linearly as each stage requires the output from the previous stage.
The user can specify additional arguments to each tool by adding the ``args`` attribute to each stage.  For example to
specify that SSPACE should use PE reads to extend into gaps and to cap min contig length to scaffold as 1KB::

   <amp>
      <stage tool="SSPACE_Basic_V2.0" args="-x 1 -z 1000">
         <inputs>
            <input name="mp1" type="raw"/>
         </inputs>
      </stage>
      <stage tool="GapCloser_V1.12">
         <inputs>
            <input name="pe1" types="raw"/>
         </inputs>
      </stage>
   </amp>

Output from amp will be placed in a directory called ``amp`` within the job's output directory.  Output from each stage
will be placed in a sub-directory within this and a link will be created to the final assembly produced at the end of
the amp pipeline.  This assembly will be used in the next stage.


