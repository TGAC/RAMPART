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

There are two ways to install RAMPART, the easiest and quickest way is from a distribute tarball.  The other is from
source.

From tarball it is simply a matter of extracting to a directory of your choice: ``tar -xvf <name_of_tarball>``.

From source, you will first need the following dependencies installed:

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

Next RAMPART's dependencies must be installed. To assist with this RAMPART provides a dependency downloader tool.
To use this, after the core RAMPART pipeline is compiled and configured (see below for more information on that, then
type): ``rampart-download-deps <dir>``


More Information
================

Now that RAMPART is installed you can consult the full manual for details on how to install dependencies, configure
RAMPART and start running jobs.  This manual can be found in the ``doc`` directory in the build, or online at::

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

* Bernardo Clavijo (TGAC)
* Robert Davey (TGAC)
* Tony Burdett (EBI)
* Ricardo Ramirez (TGAC)
* Nizar Drou (Formerly TGAC)
* David Swarbreck (TGAC)
* And everyone who contributed to making the tools RAMPART depends on!
