# This script processes the tables produced from teh raw and quality trimmed datasets and then determines the best assembly to use for the subsequent scaffolding process

# Get command like arguments.  Expects 4:
# 1 - raw statistics file
# 2 - quality trimmed statistics file
# 3 - approximate genome size
# 4 - output directory

args <- commandArgs(trailingOnly = TRUE)
#print(args)

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

# Output
out_file = paste(output_dir, "merged.tab", sep="/");
#print(out_file)
write.table(merged, out_file, sep= "|", quote=FALSE)



# Normalise merged table
scores <- merged

scores$nbcontigs <- scores$nbcontigs - min(scores$nbcontigs)
scores$total <- scores$total - approx_genome_size
scores$minlen <- scores$minlen - min(scores$minlen)
scores$avglen <- scores$avglen - min(scores$avglen)
scores$maxlen <- scores$maxlen - min(scores$maxlen)
scores$n50 <- scores$n50 - min(scores$n50)

scores$nbcontigs <- 1.0 - (scores$nbcontigs / max(scores$nbcontigs))
scores$total <- ( abs(scores$total) / approx_genome_size )
scores$minlen <- scores$minlen / max(scores$minlen)
scores$avglen <- scores$avglen / max(scores$avglen)
scores$maxlen <- scores$maxlen / max(scores$maxlen)
scores$n50 <- scores$n50 / max(scores$n50)



