   
.. _installation:

Installation
============

Before installing RAMPART please ensure any dependencies listed above are installed.  In addition, the following dependencies are required to install and run RAMPART:

* Java Runtime Environment (JRE) V1.7+

RAMPART can be installed either from a distributable tarball, or from source via a ``git clone``.  These steps are described below.  Before that however, here are a few things to keep in mind during the installation process:


From tarball
------------

RAMPART is available as a distributable tarball.  The installation process is simply involves unpacking the compressed tarball to a directory of your choice: ``tar -xvf <name_of_tarball>``.  This will create a directory called ``rampart-<version>`` and in there should be the following sub-directories:

* bin - contains the main rampart script and other utility scripts
* doc - a html and pdf copy of this manual
* etc - contains default environment configuration files, and some example job configuration files
* repo - contains the java classes used to program the pipeline
* support_jars - contains source and javadocs for the main rampart codebase

Should you want to run the tools without referring to their paths, you should ensure the 'bin' sub-directory is on your PATH environment variable.


From source
-----------

RAMPART is a java 1.7 / maven project.  Before compiling the source code, please make sure the following dependencies are installed:

* GIT
* Maven 3
* JDK v1.7+
* Sphinx (If you would like to compile this documentation)

You also need to make sure that the system to are compiling on has internet access, as it will try to automatically incorperate any required java dependecies via maven.  Now type the following::

        git clone https://github.com/TGAC/RAMPART.git
        cd RAMPART
        mvn clean install

Note: If you cannot clone the git repositories using "https", please try "ssh" instead.  Consult github to obtain the specific URLs.

Assuming there were no compilation errors.  The build, hopefully the same as that described in the previous section, can now be found in ``./build/rampart-<version>``.  There should also be a ``dist`` sub directory which will contain a tarball suitable for installing RAMPART on other systems.


