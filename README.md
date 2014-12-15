RAMPART
=======

RAMPART is a configurable pipeline for *de novo* assembly of DNA sequence data. RAMPART is not a *de novo* assembler.
There are already many very good freely available assembly tools, however, few will always produce a good assembly first time
using the default settings. Sometimes one particular assembler may perform well on your data and genome, but in other
cases another performs better. Sometimes an assembly will be better if the reads are error corrected, other times not.
Also depending on the data available, it maybe possible to improve the assembly with additional scaffolding or gap
filling steps. There are many combinations of tools that could be tried and no clear way of knowing *a priori*, which will
work best. RAMPART can be though of as a framework for these kinds of assembly projects. It is pipeline makes use of
tried and tested tools for read pre-processing, assembly and assembly improvement, that allows the user to configure
these tools and specify how they should be executed in a single configuration file. RAMPART also provides options for
comparing and analysing sequence data and assemblies.

This README file contains some brief information about how to install and get up and running with RAMPART.  For MUCH more
information please read the manual, which can be found in the doc subdirectory, or online at::

http://rampart.readthedocs.org/en/latest/index.html


Installation
============

There are three ways to install RAMPART: using homebrew, from tarball, and from source code.  All installation methods
require you to have JRE V1.7+ installed.  For more detailed installation instructions please consult the RAMPART manual.

To install from homebrew, please first ensure you have homebrew or linuxbrew installed and the homebrew science repo tapped.
Then simply type: ``brew install rampart``.

To install from tarball please go to the releases section of the RAMPART github page: ``https://github.com/TGAC/RAMPART/releases``   
Then extract to a directory of your choice: ``tar -xvf <name_of_tarball>``.

Alternatively, from source, you will first need the following dependencies installed:

* GIT
* Maven 3
* JDK v1.7+
* Sphinx and texlive (If you would like to compile the manual.  If these are not installed you must comment out the create-manual execution element from the pom.xml file.))

You also need to make sure that the system to are compiling on has internet access, as it will try to automatically
incorporate any required java dependencies via maven. Now type the following::

    git clone https://github.com/TGAC/RAMPART.git
    cd RAMPART
    mvn clean install

Note: If you cannot clone the git repositories using "https", please try "ssh" instead. Consult github to obtain the
specific URLs.

Assuming there were no compilation errors. The build can be found in ./build/rampart-<version>. There should also be a
dist sub directory which will contain a tarball suitable for installing RAMPART on other systems.

Dependencies
------------

Next RAMPART's dependencies must be installed. To save time finding all these tools on the internet RAMPART provides two options.  
The first and recommended approach is to download a compressed tarball of all supported versions of the tools, which is available on the github releases page:
``https://github.com/TGAC/RAMPART/releases``.  The second option is to download them all to a directory of your
choice.  The one exception to this is SSPACE, which requires you to fill out a form prior to download.  RAMPART can help
with this.  After the core RAMPART pipeline is compiled, type: ``rampart-download-deps <dir>``.  The tool will place all
downloaded packages in a sub-directory called "rampart_dependencies" off of the specified directory.  Note that executing this
command does not try to install the tools, as this can be a complex process and you may wish to run a custom installation
in order to compile and configure the tools in a way that is optimal for your environment.


Common installation problems
----------------------------

Some common errors the user may encounter, and steps necessary to fix the, during the installation procedure follow:

1. Old Java Runtime Environment (JRE) installed:

``Exception in thread "main" java.lang.UnsupportedClassVersionError: uk/ac/tgac/rampart/RampartCLI : Unsupported major.minor version 51.0``

This occurs When trying to run RAMPART, or any associated tools, with an old JRE version.  If you see this, install or load
JRE V1.7 or later and try again.  Note that if you are trying to compile RAMPART you will need JDK (Java Development Kit)
V1.7 or higher as well.

2. Incorrect sphinx configuration:

If you are compiling RAMPART from source, if you encounter a problem when creating the documention with sphinx it maybe
that your sphinx version is different from what is expected.  Specifically please check that your sphinx configuration
provides a tool called ``sphinx_build`` and that tool is available on the PATH.  Some sphinx configurations may have
an executable named like this ``sphinx_build_<version>``.  If this is the case you can try making a copy of this executable
and remove the version suffix from it.  Alternatively if you do not wish to compile the documentation just remove it
by commenting out the ``create-manual`` element from the pom.xml file.

3. Texlive not installed:

``“[ERROR] Failed to execute goal org.apache.maven.plugins:maven-antrun-plugin:1.7:run (create-manual) on project rampart: An Ant BuildException has occured: Warning: Could not find file RAMPART.pdf to copy.” Creating an empty RAMPART.pdf file in the specified directory fixed this issue, allowing RAMPART to successfully build``

This error occurs because the RAMPART.pdf file was not created when trying to compile the documentation.  RAMPART.pdf is created from the documentation via sphinx and texlive.
If you see this error then probably sphinx is working fine but texlive is not installed.  Properly installing and configuring 
texlive so it's available on your path should fix this issue.  Alternatively if you
do not wish to compile the documentation just remove it by commenting out the ``create-manual`` element from the pom.xml file.


Quick(ish) Start
================

To fully understand how RAMPART works and how to drive it you will need to read the documentation (see below).  However,
you can get up and running quickly by using the job configuration file named ``ecoli_full_job.xml``, which can be found
in the ``etc`` subdirectory.  This example configuration file provides a working example configuration file once you 
download the raw reads from: ``http://www.ebi.ac.uk/ena/data/view/DRR015910``.


More Information
================

Now that RAMPART is installed you can consult the manual for details on how to install dependencies, configure
RAMPART and start running jobs.  This manual can be found in the ``doc`` subdirectory, or online at::

   http://rampart.readthedocs.org/en/latest/index.html


License
=======

RAMPART is available under GNU GLP V3: http://www.gnu.org/licenses/gpl.txt

For licensing details of other RAMPART dependencies please consult their own documentation.


Contact
=======

Daniel Mapleson - Analysis Pipelines Project Leader at The Genome Analysis Centre (TGAC)

Website: http://www.tgac.ac.uk/bioinformatics/genome-analysis/daniel-mapleson/

Email: daniel.mapleson@tgac.ac.uk



Acknowledgements
================

* Nizar Drou (Formerly TGAC)
* David Swarbreck (TGAC)
* Bernardo Clavijo (TGAC)
* Robert Davey (TGAC)
* Sarah Bastkowski (TGAC)
* Tony Burdett (EBI)
* Ricardo Ramirez (TGAC)
* Purnima Pachori (TGAC)
* Mark McCullen (TGAC)
* Ram Krishna Shrestha
* And everyone who contributed to making the tools RAMPART depends on!
