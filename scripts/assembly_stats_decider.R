# This script processes the tables produced from teh raw and quality trimmed datasets and then determines the best assembly to use for the subsequent scaffolding process

# Get command like arguments.  Expects 4:
# 1 - raw statistics file
# 2 - quality trimmed statistics file
# 3 - approximate genome size
# 4 - output directory

args <- commandArgs(trailingOnly = TRUE)
print(args)

raw_stats <- args[1]
qt_stats <- args[2]
approx_genome_size <- args[3]
output_dir <- args[4]


# Load statistics into data frames

raw <- read.table(raw_stats, header = TRUE, sep="|", quote = "")
qt <- read.table(qt_stats, header = TRUE, sep="|", quote = "")


# Merge the tables into 1
raw$dataset <- "raw"
qt$dataset <- "qt"
merged <- merge(raw, qt, all = TRUE)




