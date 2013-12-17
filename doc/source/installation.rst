   
.. _installation:

Installation
============

Before installing RAMPART please ensure any dependencies listed above are installed.  In addition, the following dependencies are required to install and run RAMPART:

* Make
* Java Runtime Environment (JRE) V1.7+
* Perl

RAMPART can be installed either from a distributable tarball, or from source via a git clone.  These steps are described below.  Before that however, here are a few things to keep in mind during the installation process:


From tarball
------------

RAMPART is available as a distributable tarball.  The installation process is simply involves unpacking the compressed tarball to a directory of your choice: ``tar -xvf <name_of_tarball>``.  In addition, should you want to run the tools without referring to their paths, you should ensure the 'bin' directory is on your PATH environment variable.


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

Assuming there were no compilation errors.  The executables can now be found in ``./build/rampart-<version>``.

Note: If you cannot clone the git repositories using "https", please try "ssh" instead.  Consult github to obtain the specific URLs.



