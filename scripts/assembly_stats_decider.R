args <- commandArgs(trailingOnly = TRUE)
print(args)

raw_stats <- args[1]
qt_stats <= args[2]

raw <- read.table(raw_stats, header = TRUE, sep="|", quote = "");
qt <- read.table(qt_stats, header = TRUE, sep="|", quote = "");

# Separate the file column (keeping this column makes the maths harder)
raw_file <- raw[,c('kmer','file')]
qt_file <- qt[,c('kmer','file')]

raw$file <- NULL
qt$file <- NULL




# Get individual features

raw_nbcontigs <- raw[,c('kmer','nbcontigs')]
raw_total <- raw[,c('kmer','total')]
raw_minlen <- raw[,c('kmer','minlen')]
raw_maxlen <- raw[,c('kmer','maxlen')]
raw_avglen <- raw[,c('kmer','avglen')]
raw_n50 <- raw[,c('kmer','n50')]


qt_nbcontigs <- qt[,c('kmer','nbcontigs')]
qt_total <- qt[,c('kmer','total')]
qt_minlen <- qt[,c('kmer','minlen')]
qt_maxlen <- qt[,c('kmer','maxlen')]
qt_avglen <- qt[,c('kmer','avglen')]
qt_n50 <- qt[,c('kmer','n50')]








