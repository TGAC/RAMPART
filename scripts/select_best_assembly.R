# This script processes the tables produced from the raw and quality trimmed datasets and then determines the best assembly to use for the subsequent scaffolding process

# Get command like arguments.  Expects 4:
# 1 - raw statistics file
# 2 - quality trimmed statistics file
# 3 - approximate genome size
# 4 - output directory

args <- commandArgs(trailingOnly = TRUE)
print(paste("Argument:", args, sep=" "))

raw_stats <- args[1]
qt_stats <- args[2]
approx_genome_size <- as.numeric(args[3])
output_dir <- args[4]


# Load statistics into data frames

raw <- read.table(raw_stats, header = TRUE, sep="|", quote = "")
qt <- read.table(qt_stats, header = TRUE, sep="|", quote = "")


# Merge the tables into 1
raw$dataset <- "raw"
qt$dataset <- "qt"
merged <- merge(raw, qt, all = TRUE)

# Output
merged_file <- paste(output_dir, "merged.tab", sep="/")
write.table(merged, merged_file, sep= "|", quote=FALSE, row.names=FALSE)
print(paste("Written merged table to: ", merged_file))


# Normalise merged table

score_tab <- merged

score_tab$nbcontigs <- score_tab$nbcontigs - min(score_tab$nbcontigs)
score_tab$total <- score_tab$total - approx_genome_size
score_tab$minlen <- score_tab$minlen - min(score_tab$minlen)
score_tab$avglen <- score_tab$avglen - min(score_tab$avglen)
score_tab$maxlen <- score_tab$maxlen - min(score_tab$maxlen)
score_tab$n50 <- score_tab$n50 - min(score_tab$n50)

score_tab$nbcontigs <- 1.0 - (score_tab$nbcontigs / max(score_tab$nbcontigs))
score_tab$total <- ( abs(score_tab$total) / approx_genome_size )
score_tab$minlen <- score_tab$minlen / max(score_tab$minlen)
score_tab$avglen <- score_tab$avglen / max(score_tab$avglen)
score_tab$maxlen <- score_tab$maxlen / max(score_tab$maxlen)
score_tab$n50 <- score_tab$n50 / max(score_tab$n50)

score_tab_file <- paste(output_dir, "score.tab", sep="/")
write.table(score_tab, score_tab_file, sep="|", quote=FALSE, row.names=FALSE)
print(paste("Written score_tab table to: ", score_tab_file))




# Apply weightings.  Weightings should add up to 100

weightings <- data.frame(
	nbcontigs = c(20),
	total = c(30),
	minlen = c(0),
	avglen = c(5),
	maxlen = c(20),
	n50 = c(25))

weighting_tab <- score_tab

weighting_tab$nbcontigs <- weighting_tab$nbcontigs * weightings[1,'nbcontigs']
weighting_tab$total <- weighting_tab$total * weightings[1,'total']
weighting_tab$minlen <- weighting_tab$minlen * weightings[1,'minlen']
weighting_tab$avglen <- weighting_tab$avglen * weightings[1,'avglen']
weighting_tab$maxlen <- weighting_tab$maxlen * weightings[1,'maxlen']
weighting_tab$n50 <- weighting_tab$n50 * weightings[1,'n50']



# Calculate final scores

temp <- weighting_tab[,c('nbcontigs','total','minlen','avglen','maxlen','n50')]
weighting_tab$score <- apply(temp, 1, sum)

weighting_file <- paste(output_dir, "weighting.tab", sep="/")
write.table(weighting_tab, weighting_file, sep="|", quote=FALSE, row.names=FALSE)
print(paste("Written score_tab table to: ", weighting_file))



# Get best results

best <- weighting_tab[weighting_tab$score == max(weighting_tab$score),]
best_file <- paste(output_dir, "best.tab", sep="/")
write.table(best, best_file, sep="|", quote=FALSE, row.names=FALSE)
print(paste("Written best table to: ", best_file, "\n"))

best_path <- best[1,c('file')]
best_path_file <- paste(output_dir, "best.path.txt", sep="/");
print(best_path);

write.table(best[1,c('file')], file=best_path_file, sep="", quote=FALSE, row.names=FALSE, col.names=FALSE);
print(paste("Written best assembly file path to:", best_path_file));

