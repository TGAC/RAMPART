.. _amp:

AMP - Assembly Improver
=======================

This stage takes a single assembly as input and tries to improve it.  For example, additional scaffolding, gap
filling can be performed at this stage.  AMP stages accept ``threads`` and ``memory`` attributes just like MASS and MECQ.
A simple XML snippet describing a scaffolding and gap closing process is shown below::

   <amp>
      <stage tool="SSPACE_Basic_V2.0" threads="8" memory="16000">
         <inputs>
            <input name="mp1" ecq="raw"/>
         </inputs>
      </stage>
      <stage tool="SOAP_GapCloser_V1.12" threads="6" memory="8000">
         <inputs>
            <input name="pe1" ecq="raw"/>
         </inputs>
      </stage>
   </amp>

Each stage in the AMP pipeline must necessarily run linearly as each stage requires the output from the previous stage.
The user can specify additional arguments to each tool by adding the ``checked_args`` attribute to each stage.  For example to
specify that SSPACE should use PE reads to extend into gaps and to cap min contig length to scaffold as 1KB::

   <amp>
      <stage tool="SSPACE_Basic_V2.0" threads="16" memory="32000" checked_args="-x 1 -z 1000">
         <inputs>
            <input name="mp1" ecq="raw"/>
         </inputs>
      </stage>
      <stage tool="SOAP_GapCloser_V1.12">
         <inputs>
            <input name="pe1" ecq="raw" threads="6" memory="8000"/>
         </inputs>
      </stage>
   </amp>

Output from amp will be placed in a directory called ``amp`` within the job's output directory.  Output from each stage
will be placed in a sub-directory within this and a link will be created to the final assembly produced at the end of
the amp pipeline.  This assembly will be used in the next stage.

Some assembly enhancement tools such as Platanus scaffolder, can make use of bubble files in certain situations to provide
better scaffolding performance.  When you are assembling a diploid organism (i.e. the ploidy attribute of your organism
element is set to "2") and the assembler used in the MASS step produces bubble files, then these are automatically passed
onto the relevant AMP stage.

Some assembly enhancement tools require the input contigs file to have the fasta headers formatted in a particular way,
and sometimes with special information embedded within it.  For example, SOAP scaffolder cannot process input files that
contain gaps, and the platanus scaffolder must contain the kmer coverage value of the contig in the header.  Where possible
RAMPART tries to automatically reformat these files so they are suitable for the assembly enhancement tool.  However,
not all permutations are catered for, and some combinations are probably not possible.  If you are aware of any
compatibility issues between contig assemblers and assembly enhancement tools that RAMPART is not currently addressing
correctly, then please raise a ticket on the RAMPART github page: https://github.com/TGAC/RAMPART/issues, with details
and we will try to fix the issue in a future version.

In the future we plan to make the AMP stage more flexible so that it can handle parameter optimisation like the MASS stage.
