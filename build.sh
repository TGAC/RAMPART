#!/bin/bash

# Directory structure
SCRIPT_DIR="./scripts"
TOOLS_DIR="./tools"
TEMPLATE_DIR="./templates"
BIN_DIR="./bin"

# Script / Tool file names
REPORTER=$TOOLS_DIR"/ReportGenerator*.jar"
SEQ_INFO=$TOOLS_DIR"/sequence_info"

# Copy tools to the dist directory
cp *.pl $BIN_DIR
cp *.pm $BIN_DIR
cp *.R $BIN_DIR
cp $REPORTER $BIN_DIR
cp $SEQ_INFO $BIN_DIR
