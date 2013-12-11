
# ADDITIONAL FILES TO INSTALL
EXTRA_DIST = README.md LICENSE AUTHORS NEWS

# FILES NOT TO INSTALL
dist_noinst_SCRIPTS = autogen.sh clean.sh

# SCRIPTS TO ADD TO BIN DIR
dist_bin_SCRIPTS = \
        scripts/rampart \
        scripts/citadel-start \
        scripts/citadel-gather

bin_PROGRAMS = kat

kat_SOURCES = \
                inc/gnuplot/gnuplot_i.cc \
                inc/gnuplot/gnuplot_i.hpp \
                inc/jellyfish/fstream_default.hpp \
                inc/jellyfish/jellyfish_helper.hpp \
                inc/matrix/sparse_matrix.hpp \
                inc/matrix/threaded_sparse_matrix.hpp \
                inc/matrix/matrix_metadata_extractor.hpp \
                inc/matrix/matrix_metadata_extractor.cc \
                inc/str_utils.hpp \
                inc/file_utils.hpp \
                inc/common_args.hpp \
                sect/sect_main.cc \
                sect/sect.hpp \
                sect/sect_args.hpp \
                sect/sect_main.hpp \
                comp/comp_main.cc \
                comp/comp.hpp \
                comp/comp_args.hpp \
                comp/comp_main.hpp \
                gcp/gcp_main.cc \
                gcp/gcp.hpp \

