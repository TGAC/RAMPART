
.. _env-config:

Environment configuration
=========================

RAMPART is designed to utilise a scheduled environment if available, in order to exploit the large-scale parallelism scheduled environments typically offer.  Currently, LSF and PBS schedulers are supported, although it is also possible to run RAMPART on a regular server in an unscheduled fashion.  

In order to use a scheduled environment for executing RAMPART child jobs, some details of your specific environment are required.  These details will be requested when installing the software, however, they can be overwritten later.  By default the settings are stored in ``etc`` folder within the project's installation/build directory, and these are the files that will be used by RAMPART by default.  However, they can be overridden by either keeping a copy in ``~/.tgac/rampart/`` or by explicity specifying the location of the files when running RAMPART.  Priority is as follows:

* custom configuration file specified at runtime via the ``--env_config=<path_to_env_config_file>`` option.
* user config directory - ``~/.tgac/rampart/conan.properties``
* installation directory - ``<installation dir>/etc/conan.properties``


Conan - scheduler configuration
-------------------------------

RAMPART's execution context is specified by default in a file called "conan.properties".  In this file it is possible to describe the type of scheduling system to use and if so, what queue to run on.  Valid properties:

* ``executionContext.scheduler =`` Valid options {"","LSF","PBS"}
* ``executionContext.scheduler.queue =`` The queue to execute child jobs on.
* ``executionContext.locality = LOCAL`` Always use this for now!  In the future it may be possible to execute child jobs at a remote location.
* ``externalProcessConfigFile = <location to external process loading file>`` See next section for details of how to setup this file.

Lines starting with ``#`` are treated as comments.


External process configuration
------------------------------

RAMPART can utilise a number of dependencies, each of which may require modification of environment variables in order for it to run successfully.  This can be problematic if multiple versions of the same piece of software need ot be available on the same environment.  At TGAC we execute python scripts for configure the environment for a tool, other sites may use a system like "modules".  Instead of configuring all the tools in one go, RAMPART can execute commands specific to each dependency just prior to it's execution.  Currently known process keys are as follows (note that these keys are hard coded, please keep the exact wording as below, even if you are using a different version of the software).  The format for each entry is as follows: ``<key>=<command_to_load_tool>``.  Valid keys::

Sickle_V1.1
Abyss_V1.3.4
SSPACE_Basic_v2.0
GapCloser_v1.12
SoapDeNovo_V2.04
Quake_V0.3.4
Musket_V1.0.6
Quast_V2.2
Cegma_V2.4
Subsampler_V1.0
AllpathsLg_V44837
KAT_Comp_V1.0
Jellyfish_Count_V1.1
Jellyfish_Merge_V1.1
Jellyfish_Stats_V1.1

By default RAMPART assumes the tools are all available and properly configured.  So if this applies to your environment then you do not need to setup this file.


Logging
-------

In addition, RAMPART uses SLF4J as a logging facade and is currently configured to use LOG4J.  If you which to alter to logging configuration then modify the "log4j.properties" file.  For details please consult:
"http://logging.apache.org/log4j/2.x/"


