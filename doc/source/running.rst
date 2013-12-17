
.. _running:

Running RAMPART
===============

Running RAMPART itself from the command line is relatively straight forward.  The syntax is simply: ``rampart [options] <path_to_job_configuration_file>``.  To get a list and description of available options type: ``rampart --help``.  Upon starting RAMPART will search for a environment configuration file, a logging configuration file and a job configuration file.  RAMPART will configure the execution environment as appropriate and then execute the steps specified in the job configuration file.

Setting up a suitable configuration file for your assembly project is more complex however, and we expect a suitable level of understanding and experience of *de novo* genome assembly, NGS and genome analysis.  From a high level the definition of a job involves supplying information about 3 topics: the organism's genome; the input data; and how the pipeline should execute.  In addition, we recommend the user specifies some metadata about this job for posterity and for reporting reasons.

The job configuration file must be specifed in XML format.  Creating a configuration file from scratch can be daunting, particularly if the user isn't familier with XML or other markup languages, so to make this process easier for the user we provide a number of example configuration files which can be modified and extended as appropriate.  These can be found in the ``etc/example_job_configs`` directory.


The genome to assemble
----------------------

We recommend the user enters these properties for the genome if known:

* Organism name - this is used for reporting and logging purposes and if a prefix for name standardisation isn't provided initials taken from the name are used in the filename and headers of the final assembly.
* Genome ploidy - this is required if you choose to run the ALLPATHS-LG assembler, and is useful for calculating kmer counting hash sizes.
* Estimated Genome Size - has many purposes: 1. Can be used to compare how close the assembly size is the genome size and used as an assembly metric for assembly comparison.  2. If Kmer counting, can be used to automatically determine size of the hash table to use.  3. If requested, this can be used to calculate coverage levels for subsampling reads.
* Estimated GC percentage - Used as an assembly metric to compare assembly GC with the expected GC.

Any example XML snippet containing this information is shown below::

   <organism name="Escherichia coli" ploidy="1" est_genome_size="4600000" 
             est_gc_percentage="50.0"/>


Defining datasets
-----------------

Before an assembly can be made some sequencing data is required.  Sometimes an modern assembly project might involve a single set of sequencing data, othertimes it can involve a number of sequencing projects using different protocols and different data types.  In order to instruct the assemblers and other tools to use the data in the right way, the user must describe each dataset and how to interpret it.

Each dataset description should contain the following information:

* An identifier - so we can point tools to use a specific dataset later.
* Read length - The length of each read in base pairs.
* Average insert size - An estimate of the average insert size in base pairs used for sequencing if this is a paired end or mate pair library.
* Insert error tolerance - How tolerant tools should be when interpreting the average insert size specified above.  This figure should be a percentage, e.g. the tool should accept inserts sizes with a 30% tolerance either side of the average insert size.
* Orientation - If this is a paired end or mate pair library, the orientation of the reads.  For example, paired end libraries are often created using "forward reverse" orientation, and often long mate pairs use "reverse forward" orientation.  The user should specify either "FR" or "RF" for this property.
* Type - The kind of library this is.  Valid options: "SE" - single end; "OPE" - overlapping paired end; "PE" - paired end; "MP" - mate pair.
* File paths - File paths to the actual sequencing data.  For paired end and mate pair datasets this will involve pointers to two separate files.

An example XML snippet of a set of NGS datasets for an assembly project are shown below::

    <libraries>
        <library name="pe1" read_length="101" avg_insert_size="500" insert_err_tolerance="0.3" 
                 orientation="FR" type="PE">
            <files>
                <path>lib1_R1.fastq</path>
                <path>lib1_R2.fastq</path>
            </files>
        </library>
        <library name="mp1" read_length="150" avg_insert_size="4000" insert_err_tolerance="0.3" 
                 orientation="RF" type="MP">
            <files>
                <path>lib2_R1.fastq</path>
                <path>lib2_R2.fastq</path>
            </files>
        </library>
        <library name="ope1" read_length="101" avg_insert_size="180" insert_err_tolerance="0.3" 
                orientation="FR" type="OPE">
            <files>
                <path>lib3_R1.fastq</path>
                <path>lib3_R2.fastq</path>
            </files>
        </library>
    </libraries>


The pipeline
------------

The RAMPART pipeline can loosely be separated into the following stages, all of which are optional customisable:

* MECQ - Multiple Error Correction and Quality Trimming Stage
* KMER_COUNT - Kmer Counting Reads
* MASS - Multiple Assembly Creation
* AMP - Assembly Improvement
* FINALISE - Standardise filename and header names in the final assembly

For more details on each section see the following:

.. toctree::
   
   mecq
   kmer
   mass
   amp
   finalise


Controlling the pipeline... definition in the config file.  Via the command line.


