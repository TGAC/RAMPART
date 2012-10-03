#!/bin/bash

# Directory structure
SCRIPT_DIR="./scripts"
TOOLS_DIR="./tools"
TEMPLATE_DIR="./templates"
BIN_DIR="./bin"

# Script / Tool file names
ARTIFACT="./artifact.sh"
ASSEMBLER=$SCRIPT_DIR"/assembler.pl"
A_ANALYSER=$SCRIPT_DIR"/assembly_analyser.pl"
AS_GATHERER=$SCRIPT_DIR"/assembly_stats_gatherer.pl"
AS_FORMATTER=$SCRIPT_DIR"/assembly_stats_formatter.pl"
AS_PLOTTER=$SCRIPT_DIR"/assembly_stats_plotter.R"
AS_DECIDER=$SCRIPT_DIR"/assembly_stats_decider.R"
SCAFFOLDER=$SCRIPT_DIR"/scaffolder.pl"
GAPCLOSER=$SCRIPT_DIR"/gapcloser.pl"
REPORTER=$TOOLS_DIR"/reporter/dist/reporter.jar"
SEQ_INFO=$TOOLS_DIR"/sequence_info"

# Copy tools to the dist directory
cp $ARTIFACT $BIN_DIR
cp $ASSEMBLER $BIN_DIR
cp $A_ANALYSER $BIN_DIR
cp $AS_GATHERER $BIN_DIR
cp $AS_FORMATTER $BIN_DIR
cp $AS_PLOTTER $BIN_DIR
cp $AS_DECIDER $BIN_DIR
cp $SCAFFOLDER $BIN_DIR
cp $GAPCLOSER $BIN_DIR
cp $REPORTER $BIN_DIR
cp $SEQ_INFO $BIN_DIR
