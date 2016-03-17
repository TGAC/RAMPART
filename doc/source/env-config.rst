
.. _env-config:

Environment configuration
=========================

RAMPART is designed to utilise a scheduled environment in order to exploit the large-scale parallelism high performance
computing environments typically offer.  Currently, LSF and PBS schedulers are supported, although it is also possible
to run RAMPART on a regular server in an unscheduled fashion.

In order to use a scheduled environment for executing RAMPART child jobs, some details of your specific environment are
required.  These details will be requested when installing the software, however, they can be overwritten later.  By
default the settings are stored in ``etc`` folder within the project's installation/build directory, and these are the
files that will be used by RAMPART by default.  However, they can be overridden by either keeping a copy in
``~/.tgac/rampart/`` or by explicity specifying the location of the files when running RAMPART.  Priority is as follows:

* custom configuration file specified at runtime via the command line - ``--env_config=<path_to_env_config_file>``
* user config directory - ``~/.tgac/rampart/conan.properties``
* installation directory - ``<installation dir>/etc/conan.properties``


Conan - scheduler configuration
-------------------------------

RAMPART's execution context is specified by default in a file called "conan.properties".  In this file it is possible to
describe the type of scheduling system to use and if so, what queue to run on.  Valid properties:

* ``executionContext.scheduler =`` Valid options {"","LSF","PBS","SLURM"}
* ``executionContext.scheduler.queue =`` The queue/partition to execute child jobs on.
* ``executionContext.locality = LOCAL`` Always use this for now!  In the future it may be possible to execute child jobs at a remote location.
* ``externalProcessConfigFile = <location to external process loading file>`` See next section for details of how to setup this file.

Lines starting with ``#`` are treated as comments.


External process configuration
------------------------------

RAMPART can utilise a number of dependencies, each of which may require modification of environment variables in order
for it to run successfully.  This can be problematic if multiple versions of the same piece of software need to be
available on the same environment.  At TGAC we execute python scripts for configure the environment for a tool, although other
institutes may use an alternative system like "modules".  Instead of configuring all the tools in one go, RAMPART can execute commands
specific to each dependency just prior to it's execution.  Currently known process keys are described below.  In
general the versions indicated have been tested and will work with RAMPART, however other versions may work if their
command line interface has not changed significantly from the listed versions.  Note that these keys are hard coded, please keep
the exact wording as below, even if you are using a different version of the software.
The format for each entry is as follows: ``<key>=<command_to_load_tool>``.  Valid keys::

   # Assemblers
   Abyss_V1.5
   AllpathsLg_V50
   Platanus_Assemble_V1.2
   SOAP_Assemble_V2.4
   Spades_V3.1
   Velvet_V1.2

   # Dataset improving tools
   Sickle_V1.2
   Quake_V0.3
   Musket_V1.0

   # Assembly improving tools
   Platanus_Gapclose_V1.2
   Platanus_Scaffold_V1.2
   SSPACE_Basic_v2.0
   SOAP_GapCloser_V1.12
   SOAP_Scaffold_V2.4
   Reapr_V1
   FastXRC_V0013

   # Assembly analysis tools
   Quast_V2.3
   Cegma_V2.4
   KAT_Comp_V1.0
   KAT_GCP_V1.0
   KAT_Plot_Density_V1.0
   KAT_Plot_Spectra-CN_V1.0

   # Misc tools
   Jellyfish_Count_V1.1
   Jellyfish_Merge_V1.1
   Jellyfish_Stats_V1.1
   Subsampler_V1.0
   KmerGenie_V1.6

By default RAMPART assumes the tools are all available and properly configured.  So if this applies to your environment
then you do not need to setup this file.


Logging
-------

In addition, RAMPART uses SLF4J as a logging facade and is currently configured to use LOG4J.  If you which to alter to
logging configuration then modify the "log4j.properties" file.  For details please consult:
"http://logging.apache.org/log4j/2.x/"


