.. RAMPART documentation master file, created by
   sphinx-quickstart on Wed Dec 11 14:37:43 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

.. image:: RAMPART-logo.png

Introduction and Contents
=========================

RAMPART is an automated de novo assembly pipeline designed to manage the generation of multiple assemblies of the same DNA sequence data using varying assemblers and parameters.  RAMPART also provides options for comparing and selecting assemblies for post-processing.  

The actual quality of assembly is always going to be a function of sequenceing quality, depth and fragment size, the genome size and complexity.  RAMPART enables the bioinformatician to get a reasonable assembly given the constraints just mentioned with minimal effort.  In many cases, particularly for organisms with haploid genomes or relatively simple (i.e. not too heterozygous and not too repeaty) diploid genomes, where appropriate sequenceing has been conducted, RAMPART is sufficient to produce a decent assembly that is suitable for distribution and downstream analysis.

This documentation is designed to help end users install, configure and run the RAMPART pipeline.

Contents:

.. toctree::
   :maxdepth: 2

* :ref:`dependencies`
* :ref:`installation`
* :ref:`env-config`
* :ref:`running`
* :ref:`citing`
* :ref:`license`
* :ref:`contact`
* :ref:`acknowledgments`


.. _dependencies:

Dependencies
============

In order to do useful work RAMPART can call out to a number of 3rd party tools during pipeline execution.  The current list of dependencies is below.  For full functionality, all these tools should be installed on your environment, however they are not mandatory so you only need to install those which you wish to use in the pipeline.

Assemblers (RAMPART is not an assembler itself so you should have at least one of these installed to do useful work):
 
* Abyss V1.3
* ALLPATHS-LG V44837
* SOAPdenovo V2

Dataset improvement tools:

* Sickle V1.1
* Quake V0.3
* Musket V1.0

Assembly improvement tools:

* SSPACE Basic V2.0
* SOAP GapCloser V1.12

Assembly Analysis Tools:

* Quast V2.2
* CEGMA V2.4
* KAT V1.0

Miscellaneous Tools:
 
* TGAC Subsampler V1.0
* Jellyfish V1.1.10

To save time finding all these tools on the internet RAMPART offers an option to download them all to a directory of your choice.  The one exception to this is SSPACE, which requires you to fill out a form prior to download.  To do this, after the core RAMPART pipeline is compiled, type: ``rampart download <dir>``.  Executing this command does not try to install the tools, as this is a complex process and you may wish to run a custom installation in order to compile and configure the tools in a way that is optimal for your particularly environment.

In case the specific tool versions requested are no longer available to download the project URLs are specified below.  It's possible alternative (preferably newer) versions of the software may still work if the interfaces have not changed significantly.  If you find that a tool does not work in the RAMPART pipeline please contact daniel.mapleson@tgac.ac.uk, or raise a job ticket via .  Project URLs:

Abyss           - http://www.bcgsc.ca/platform/bioinfo/software/abyss
AllpathsLg      - http://www.broadinstitute.org/software/allpaths-lg/blog/?page_id=12
SoapDeNovo      - http://soap.genomics.org.cn/soapdenovo.html

Sickle          - https://github.com/najoshi/sickle
Quake           - http://www.cbcb.umd.edu/software/quake/
Musket          - http://musket.sourceforge.net/homepage.htm#latest

SSPACE_Basic    - http://www.baseclear.com/landingpages/basetools-a-wide-range-of-bioinformatics-solutions/sspacev12/
SOAP_GapCloser  - http://soap.genomics.org.cn/soapdenovo.html

Quast           - http://bioinf.spbau.ru/quast
Cegma           - http://korflab.ucdavis.edu/datasets/cegma/
KAT				- http://www.tgac.ac.uk/kat/

Subsampler      - https://github.com/homonecloco/subsampler
Jellyfish		- http://www.cbcb.umd.edu/software/jellyfish/




.. _installation:

Installation
============

Before installing RAMPART please ensure any dependencies listed above are installed.  In addition, the following dependencies are required to install and run RAMPART:

* Make
* Java Runtime Environment (JRE) V1.7+
* Perl

RAMPART can be installed either from a distributable tarball, or from source via a git clone.  These steps are described below.  Before that however, here are a few things to keep in mind during the installation process:

* During the configuration step, RAMPART will ask for details concerning your default environment.  These settings can be overridden later but by default RAMPART will use this information when executing the pipeline.  More information can be found in section: :ref:`env-config`.  
* Should you wish to install RAMPART to a non-standard location please add the ``--prefix=<installation_dir>`` to the configure command.
* If you encounter errors during the installation step, consider if you have appropriate user permission to install software.  You may need to prefix the command with sudo to raise your priviliges. 


From Tarball
------------

RAMPART is available as a distributable tarball.  The installation process is similar to many other linux based tools (with a small deviation)::

	tar -xvf <name_of_tarball>
	cd rampart-<version>
	./configure
	make
	make install


From source
-----------

RAMPART is a java 1.7 / maven project, which is encapsulated by an autotools project in order to make the compilation and installation of the program easier for the end user.  Before following the instructions below, please make sure the following dependencies are installed:

* Autotools
* GIT
* Maven 3
* JDK v1.7+
* Sphinx (If you would like to compile this documentation)

Now type the following::

	git clone https://github.com/TGAC/RAMPART.git
	cd RAMPART
	./configure
	make
	make install

Note: If you cannot clone the git repositories using "https", please try "ssh" instead.  Consult github to obtain the specific URLs.



.. _env-config:

Environment configuration
=========================

RAMPART is designed to utilise a scheduled environment if available, in order to exploit the large-scale parallelism scheduled environments typically offer.  Currently, LSF and PBS schedulers are supported, although it is also possible to run RAMPART on a regular server in an unscheduled fashion.  

In order to use a scheduled environment for executing RAMPART child jobs, some details of your specific environment are required.  These details will be requested when installing the software, however, they can be overwritten later.  By default the settings are stored in ``etc`` folder within the project's installation directory, and these are the files that will be used by RAMPART by default.  However, they can be overridden by either keeping a copy in ``~/.tgac/rampart/`` or by explicity specifying the location of the files when running RAMPART.  Priority is as follows:

* explicit location specified at runtime
* user config directory - ``~/.tgac/rampart/``
* <installation dir>/etc


Conan - Scheduler configuration
-------------------------------

RAMPART's execution context is specified by default in a file called "conan.properties".  In this file it is possible to describe the type of scheduling system to use and if so, what queue to run on.  Valid properties:

* ``executionContext.scheduler =`` Valid options {"","LSF","PBS"}
* ``executionContext.scheduler.queue =`` The queue to execute child jobs on.
* ``executionContext.locality = LOCAL`` Always use this for now!  In the future it may be possible to execute child jobs at a remote location.
* ``externalProcessConfigFile = <location to external process loading file>`` See next section for details of how to setup this file.

Lines starting with ``#`` are treated as comments.


External process configuration
------------------------------

RAMPART can utilise a number of dependencies, each of which may require modification of environment variables in order for it to run successfully.  This can be problematic if multiple versions of the same piece of software need ot be available on the same environment.  At TGAC we execute python scripts for configure the environment for a tool, other sites may use a system like "modules".  Instead of configuring all the tools in one go, RAMPART can execute commands specific to each dependency just prior to it's execution.  Currently known process keys are as follows (note that these keys are hard coded, please keep the exact wording as below, even if you are using a different version of the software).  The format for each entry is as follows: <key>=<command_to_load_tool>:

* ``Sickle_V1.1``
* ``Abyss_V1.3.4``
* ``SSPACE_Basic_v2.0``
* ``GapCloser_v1.12``
* ``SoapDeNovo_V2.04``
* ``Quake_V0.3.4``
* ``Musket_V1.0.6``
* ``Quast_V2.2``
* ``Cegma_V2.4``
* ``Subsampler_V1.0``
* ``AllpathsLg_V44837``
* ``KAT_Comp_V1.0``
* ``Jellyfish_Count_V1.1``
* ``Jellyfish_Merge_V1.1``
* ``Jellyfish_Stats_V1.1``

By default RAMPART assumes the tools are all available and properly configured.  So if this applies to your environment then you do not need to setup this file.  


Logging
-------

In addition, RAMPART uses SLF4J as a logging facade and is currently configured to use LOG4J.  If you which to alter to logging configuration then modify the "log4j.properties" file.  For details please consult:
"http://logging.apache.org/log4j/2.x/"


.. _running:

Running RAMPART
===============



.. _citing:

Citing
======

The paper for RAMPART is currently being written.  If you use RAMPART in your work and wish to publish in the meantime please refer the project's web page: http://www.tgac.ac.uk/main-icons/computational-biology/tools-resources/rampart/
 


.. _license:

License
=======

RAMPART is available under GNU GLP V3: http://www.gnu.org/licenses/gpl.txt  

For licensing details of other RAMPART dependencies please consult their own documentation.


.. _contact:

Contact
=======

Daniel Mapleson
Analysis Pipelines Project Leader at The Genome Analysis Centre (TGAC)
http://tgac.ac.uk/
daniel.mapleson@tgac.ac.uk


.. _acknowledgments:

Acknowledgements
================

* Bernardo Clavijo (TGAC)
* Robert Davey (TGAC)
* Tony Burdett (EBI)
* Ricardo Ramirez (TGAC)
* Nizar Drou (Formerly TGAC)
* David Swarbreck (TGAC)


