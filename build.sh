#!/bin/bash

# Directory structure
SCRIPT_DIR = "./scripts"
TOOLS_DIR = "./tools"
TEMPLATE_DIR = "./templates"
DIST_DIR = "./dist"

# Script / Tool file names
ARTIFACT = "./artifact.sh"
ASSEMBLER = $SCRIPT_DIR"/assembler.pl"
SCAFFOLDER = $SCRIPT_DIR"/scaffolder.pl"
GAPCLOSER = $SCRIPT_DIR"/gapcloser.pl"
REPORTER = $TOOLS_DIR"/reporter/dist/reporter.jar"

# Copy tools to the dist directory
cp $ARTIFACT $DIST_DIR
cp $ASSEMBLER $DIST_DIR
cp $SCAFFOLDER $DIST_DIR
cp $GAPCLOSER $DIST_DIR
cp $REPORTER $DIST_DIR
