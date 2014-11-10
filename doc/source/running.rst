
.. _running:

Running RAMPART
===============

Running RAMPART itself from the command line is relatively straight forward.  The syntax is simply:
``rampart [options] <path_to_job_configuration_file>``.  To get a list and description of available options type:
``rampart --help``.  Upon starting RAMPART will search for a environment configuration file, a logging configuration
file and a job configuration file.  RAMPART will configure the execution environment as appropriate and then execute
the steps specified in the job configuration file.

Setting up a suitable configuration file for your assembly project is more complex however, and we expect a suitable
level of understanding and experience of *de novo* genome assembly, NGS and genome analysis.  From a high level the
definition of a job involves supplying information about 3 topics: the organism's genome; the input data; and how the
pipeline should execute.  In addition, we recommend the user specifies some metadata about this job for posterity and
for reporting reasons.

The job configuration file must be specified in XML format.  Creating a configuration file from scratch can be daunting,
particularly if the user isn't familiar with XML or other markup languages, so to make this process easier for the user
we provide a number of example configuration files which can be modified and extended as appropriate.  These can be
found in the ``etc/example_job_configs`` directory.  Specifically the file named ``ecoli_full_job.xml`` provides a
working example configuration file once you download the raw reads from: ``http://www.ebi.ac.uk/ena/data/view/DRR015910``.


The genome to assemble
----------------------

We recommend the user enters these properties for the genome if known:

* Organism name - this is used for reporting and logging purposes and if a prefix for name standardisation isn't provided initials taken from the name are used in the filename and headers of the final assembly.
* Genome ploidy - this is required if you choose to run the ALLPATHS-LG assembler, and is useful for calculating kmer counting hash sizes.  If you set to "2", i.e. diploid, then assembly enhancement tools may make use of bubble files if they are available and if the tools are capable.
* Estimated Genome Size - has many purposes: 1. Can be used to compare how close the assembly size is the genome size and used as an assembly metric for assembly comparison.  2. If Kmer counting, can be used to automatically determine size of the hash table to use.  3. If requested, this can be used to calculate coverage levels for subsampling reads.
* Estimated GC percentage - Used as an assembly metric to compare assembly GC with the expected GC.

Any example XML snippet containing this information is shown below::

   <organism name="Escherichia coli" ploidy="1" est_genome_size="4600000" 
             est_gc_percentage="50.0"/>


Defining datasets
-----------------

Before an assembly can be made some sequencing data is required.  Sometimes an modern assembly project might involve a
single set of sequencing data, othertimes it can involve a number of sequencing projects using different protocols and
different data types.  In order to instruct the assemblers and other tools to use the data in the right way, the user
must describe each dataset and how to interpret it.

Each dataset description must contain the following information:

* Attribute "name" - an identifier - so we can point tools to use a specific dataset later.
* Element "files" - Must contain one of more "path" elements containing file paths to the actual sequencing data.  For paired end and mate pair datasets this will involve pointers to two separate files.

Ideally you should specify the following information as well if you want RAMPART to execute all tools with the best settings:

* Attribute "read_length" -The length of each read in base pairs.
* Attribute "avg_insert_size" - An estimate of the average insert size in base pairs used for sequencing if this is a paired end or mate pair library.
* Attribute "insert_err_tolerance" - How tolerant tools should be when interpreting the average insert size specified above.  This figure should be a percentage, e.g. the tool should accept inserts sizes with a 30% tolerance either side of the average insert size.
* Attribute "orientation" - If this is a paired end or mate pair library, the orientation of the reads.  For example, paired end libraries are often created using "forward reverse" orientation, and often long mate pairs use "reverse forward" orientation.  The user should specify either "FR" or "RF" for this property.
* Attribute "type" - The kind of library this is.  Valid options: "SE" - single end; "OPE" - overlapping paired end; "PE" - paired end; "MP" - mate pair.
* Attribute "phred" - The ascii offset to apply to the quality scores in the library.  Valid options: "PHRED_33" (Sanger / Illumina 1.8+); "PHRED_64" (Illumina 1.3 - 1.7).
* Attribute "uniform" - Whether or not the reads have uniform length.  This is set to true by default.  This property is used to work out the fastest way to calculate the number of bases present in the library for downsampling, should that be requested.

An example XML snippet of a set of NGS datasets for an assembly project are shown below::

    <libraries>
        <library name="pe1" read_length="101" avg_insert_size="500" insert_err_tolerance="0.3" 
                 orientation="FR" type="PE" phred="PHRED_64">
            <files>
                <path>lib1_R1.fastq</path>
                <path>lib1_R2.fastq</path>
            </files>
        </library>
        <library name="mp1" read_length="150" avg_insert_size="4000" insert_err_tolerance="0.3" 
                 orientation="RF" type="MP" uniform="false" phred="PHRED_64">
            <files>
                <path>lib2_R1.fastq</path>
                <path>lib2_R2.fastq</path>
            </files>
        </library>
        <library name="ope1" read_length="101" avg_insert_size="180" insert_err_tolerance="0.3" 
                orientation="FR" type="OPE" phred="PHRED_33">
            <files>
                <path>lib3_R1.fastq</path>
                <path>lib3_R2.fastq</path>
            </files>
        </library>
    </libraries>


In the future we plan to interrogate the libraries to work out many of the settings automatically.  However, for the time
being we request that you enter all these details manually.


The pipeline
------------

The RAMPART pipeline can be separated into a number of stages, all of which are optional customisable.
The pipeline can be controlled in two ways.  The first way is by definition in the job configuration file.  If a
pipeline stage is not defined it will not be executed. The second way is via a command line option: ``-s``.  By
specifying which stages you wish to execute here you can run specific stage of the pipeline in isolation, or as a group.
For example by typing: ``rampart -s MECQ,MASS job.cfg``, you instruct RAMPART to run only the MECQ and MASS stages
described in the job.cfg file.  A word of caution here, requesting stages not defined in the configuration file does
not work.  Also you must ensure that each stage has it's pre-requisites fulfilled before starting.  For example, you
cannot run the AMP stage, without a selected assembly to work with.

.. toctree::
   
   mecq
   analyse_reads
   mass
   analyse_assemblies
   amp
   finalise



Potential runtime problems
--------------------------

There are a few issues that can occur during execution of RAMPART which may prevent your jobs from completing successfully.
This part of the documentation attempts to list common problems and suggests workarounds or solutions:

* Quake fails

In this case, if you have set the quake k value high you should try reducing it, probably to the default value unless you
know what you are doing.  Also Quake can only work successfully if you have sufficient sequencing depth in your dataset.
If this is not the case then you should either obtain a new dataset or remove quake error correction from your RAMPART
configuration and try again.

* Kmergenie fails

Often this occurs for the same reasons as Quake, i.e. inadequate coverage.  Check that you have correct set the ploidy value
for your organism in the configuration file (Kmer genie only support haploid or diploid (i.e. 1 or 2), for polyploid
genomes you are on your own!) Also keep in mind that should you remove kmer genie from your pipeline and manually set a
kmer value for an assembler, it is unlikely that your assembly will be very contiguous but RAMPART allows you to try things
out and you maybe able to assemble some useful data.

* Pipeline failed at a random point during execution of one of the external tools

In this case check your system.  Ensure that the computing systems are all up and running, that there have been no power
outages and you have plenty of spare disk space.  RAMPART can produce a lot of data for non-trivial genomes so please
you have plenty of spare disk space before starting a job.




