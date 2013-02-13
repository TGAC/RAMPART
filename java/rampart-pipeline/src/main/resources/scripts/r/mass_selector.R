# This script processes the tables produced from the raw and quality trimmed datasets and then determines the best assembly to use for the subsequent scaffolding process

# Get command like arguments.  Expects 4:
# 1 - raw statistics file
# 2 - quality trimmed statistics file
# 3 - approximate genome size
# 4 - output directory
# 5 - weighting file

args <- commandArgs(trailingOnly = TRUE)
print(paste("Argument:", args, sep=" "))

raw_stats <- args[1]
qt_stats <- args[2]
approx_genome_size <- as.numeric(args[3])
output_dir <- args[4]
weightings_file <- args[5]


# Load statistics into data frames
raw <- read.table(raw_stats, header = TRUE, sep="|", quote = "")
qt <- read.table(qt_stats, header = TRUE, sep="|", quote = "")

# Load weightings into data frame
weightings <- read.table(weightings_file, header = TRUE, sep="|", quote = "")

# Merge the tables into 1
raw$dataset <- "raw"
qt$dataset <- "qt"
merged <- merge(raw, qt, all = TRUE)


# Output
merged_file <- paste(output_dir, "merged.tab", sep="/")
write.table(merged, merged_file, sep= "|", quote=FALSE, row.names=FALSE)
print(paste("Written merged table to: ", merged_file))


# Normalise merged table

norm_tab <- merged

norm_tab$nbcontigs <- norm_tab$nbcontigs - min(norm_tab$nbcontigs)
#norm_tab$total <- norm_tab$total - approx_genome_size
norm_tab$minlen <- norm_tab$minlen - min(norm_tab$minlen)
norm_tab$avglen <- norm_tab$avglen - min(norm_tab$avglen)
norm_tab$maxlen <- norm_tab$maxlen - min(norm_tab$maxlen)
norm_tab$n50 <- norm_tab$n50 - min(norm_tab$n50)

norm_tab$nbcontigs <- 1.0 - (norm_tab$nbcontigs / max(norm_tab$nbcontigs))
#norm_tab$total <- ( abs(norm_tab$total) / approx_genome_size )
norm_tab$minlen <- norm_tab$minlen / max(norm_tab$minlen)
norm_tab$avglen <- norm_tab$avglen / max(norm_tab$avglen)
norm_tab$maxlen <- norm_tab$maxlen / max(norm_tab$maxlen)
norm_tab$n50 <- norm_tab$n50 / max(norm_tab$n50)

norm_tab_file <- paste(output_dir, "norm.tab", sep="/")
write.table(norm_tab, norm_tab_file, sep="|", quote=FALSE, row.names=FALSE)
print(paste("Written norm_tab table to: ", norm_tab_file))




# Apply weightings.  Weightings should add up to 100

weighting_tab <- norm_tab

weighting_tab$nbcontigs <- weighting_tab$nbcontigs * weightings[1,'nbcontigs']
#weighting_tab$total <- weighting_tab$total * weightings[1,'total']
weighting_tab$minlen <- weighting_tab$minlen * weightings[1,'minlen']
weighting_tab$avglen <- weighting_tab$avglen * weightings[1,'avglen']
weighting_tab$maxlen <- weighting_tab$maxlen * weightings[1,'maxlen']
weighting_tab$n50 <- weighting_tab$n50 * weightings[1,'n50']



# Calculate final scores
score_tab <- merged
#temp <- weighting_tab[,c('nbcontigs','total','minlen','avglen','maxlen','n50')]
temp <- weighting_tab[,c('nbcontigs','minlen','avglen','maxlen','n50')]
score_tab$score <- apply(temp, 1, sum)

score_tab_file <- paste(output_dir, "score.tab", sep="/")
write.table(score_tab, score_tab_file, sep="|", quote=FALSE, row.names=FALSE)
print(paste("Written score_tab table to: ", score_tab_file))



# Get best results

best <- weighting_tab[score_tab$score == max(score_tab$score),]
best_file <- paste(output_dir, "best.tab", sep="/")
write.table(best, best_file, sep="|", quote=FALSE, row.names=FALSE)
print(paste("Written best table to: ", best_file, "\n"))

best_path <- best[1,c('file')]
best_path_file <- paste(output_dir, "best.path.txt", sep="/")
print(best_path)

write.table(best[1,c('file')], file=best_path_file, sep="", quote=FALSE, row.names=FALSE, col.names=FALSE)
print(paste("Written best assembly file path to:", best_path_file))

best_dataset_file <- paste(output_dir, "best.dataset.txt", sep="/")
write.table(best[1,c('dataset')], file=best_dataset_file, sep="", quote=FALSE, row.names=FALSE, col.names=FALSE)
print(paste("Written best dataset file path to:", best_dataset_file))

