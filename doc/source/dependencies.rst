

.. _dependencies:

Dependencies
============

In order to do useful work RAMPART can call out to a number of third party tools during execution.  The current list of dependencies is shown below.  For full functionality, all these tools should be installed on your environment, however they are not mandatory so you only need to install those which you wish to use.

Assemblers (RAMPART is not an assembler itself so you should have at least one of these installed to do useful work):

* Abyss V1.5
* ALLPATHS-LG V44837
* Platanus V1.2
* SPAdes 3.1
* SOAPdenovo V2
* Velvet V1.2

Dataset improvement tools:

* Sickle V1.2
* Quake V0.3
* Musket V1.0

Assembly improvement tools:

* Platanus V1.2 (for scaffolding and gap closing)
* SSPACE Basic V2.0
* SOAP de novo V2 (for scaffolding)
* SOAP GapCloser V1.12
* Reapr V1.0

Assembly Analysis Tools:

* Quast V2.2 - for contiguity analysis
* CEGMA V2.4 - for assembly completeness analysis
* KAT V1.0 - for kmer analysis

Miscellaneous Tools:

* TGAC Subsampler V1.0 - for reducing coverage of reads
* Jellyfish V1.1.10 - for kmer counting
* Kmer Genie V1.6 - for determining optimal kmer values for assembly

To save time finding all these tools on the internet RAMPART provides two options.  The first and recommended approach is
to download a compressed tarball of all supported versions of the tools, which is available on the github releases page:
``https://github.com/TGAC/RAMPART/releases``.  The second option is to download them all to a directory of your
choice.  The one exception to this is SSPACE, which requires you to fill out a form prior to download.  RAMPART can help
with this.  After the core RAMPART pipeline is compiled, type: ``rampart-download-deps <dir>``.  Note that executing this
command does not try to install the tools, as this can be a complex process and you may wish to run a custom installation
in order to compile and configure the tools in a way that is optimal for your environment.

In case the specific tool versions requested are no longer available to download the project URLs are specified below.
It's possible that alternative (preferably newer) versions of the software may still work if the interfaces have not
changed significantly.  If you find that a tool does not work in the RAMPART pipeline please contact daniel.mapleson@tgac.ac.uk,
or raise a job ticket via the github issues page: https://github.com/TGAC/RAMPART/issues.

Project URLs:

* Abyss           - http://www.bcgsc.ca/platform/bioinfo/software/abyss
* ALLPATHS-LG     - http://www.broadinstitute.org/software/allpaths-lg/blog/?page_id=12
* Cegma           - http://korflab.ucdavis.edu/datasets/cegma/
* KAT             - http://www.tgac.ac.uk/kat/
* Kmer Genie      - http://kmergenie.bx.psu.edu/
* Jellyfish       - http://www.cbcb.umd.edu/software/jellyfish/
* Musket          - http://musket.sourceforge.net/homepage.htm#latest
* Quake           - http://www.cbcb.umd.edu/software/quake/
* Quast           - http://bioinf.spbau.ru/quast
* Platanus        - http://http://platanus.bio.titech.ac.jp/platanus-assembler/
* Reapr           - https://www.sanger.ac.uk/resources/software/reapr/#t_2
* Sickle          - https://github.com/najoshi/sickle
* SoapDeNovo      - http://soap.genomics.org.cn/soapdenovo.html
* SOAP_GapCloser  - http://soap.genomics.org.cn/soapdenovo.html
* SPAdes          - http://spades.bioinf.spbau.ru
* SSPACE_Basic    - http://www.baseclear.com/landingpages/basetools-a-wide-range-of-bioinformatics-solutions/sspacev12/
* Subsampler      - https://github.com/homonecloco/subsampler
* Velvet          - https://www.ebi.ac.uk/~zerbino/velvet/

