   
.. _installation:

Installation
============

Before installing RAMPART please ensure any dependencies listed above are installed.  In addition, the following
dependencies are required to install and run RAMPART:

* Java Runtime Environment (JRE) V1.7+

RAMPART can be installed either from a distributable tarball, or from source via a ``git clone``.  These steps are
described below.  Before that however, here are a few things to keep in mind during the installation process:


From tarball
------------

RAMPART is available as a distributable tarball.  The installation process is simply involves unpacking the compressed
tarball, available from the RAMPART github page: ``https://github.com/TGAC/RAMPART/releases``, to a directory of your
choice: ``tar -xvf <name_of_tarball>``.  This will create a directory called ``rampart-<version>`` and in there should
be the following sub-directories:

* bin - contains the main rampart script and other utility scripts
* doc - a html and pdf copy of this manual
* etc - contains default environment configuration files, and some example job configuration files
* man - contains a copy of the manual in man form
* repo - contains the java classes used to program the pipeline
* support_jars - contains source and javadocs for the main rampart codebase

Should you want to run the tools without referring to their paths, you should ensure the 'bin' sub-directory is on your
PATH environment variable.


From source
-----------

RAMPART is a java 1.7 / maven project.  Before compiling the source code, please make sure the following dependencies are
installed:

* GIT
* Maven 3
* JDK v1.7+
* Sphinx and texlive (If you would like to compile this documentation.  If these are not installed you must comment out
the ``create-manual`` execution element from the pom.xml file.)

You also need to make sure that the system to are compiling on has internet access, as it will try to automatically
incorporate any required java dependencies via maven.  Now type the following::

        git clone https://github.com/TGAC/RAMPART.git
        cd RAMPART
        mvn clean install

Note: If you cannot clone the git repositories using "https", please try "ssh" instead.  Consult github to obtain the
specific URLs.

Assuming there were no compilation errors.  The build, hopefully the same as that described in the previous section, can
now be found in ``./build/rampart-<version>``.  There should also be a ``dist`` sub directory which will contain a
tarball suitable for installing RAMPART on other systems.

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



