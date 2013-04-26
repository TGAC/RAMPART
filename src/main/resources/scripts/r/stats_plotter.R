args <- commandArgs(trailingOnly = TRUE)
print(args)

infile <- args[1]
outfile <- args[2]

t1 <- read.table(infile, header = TRUE, sep="|", quote = "");

col1 <- colnames(t1)[1]

# Get individual features

nbcontigs <- t1[,c(col1,'nb_seqs')]
apc <- t1[,c(col1,'a.pc')]
cpc <- t1[,c(col1,'c.pc')]
gpc <- t1[,c(col1,'g.pc')]
tpc <- t1[,c(col1,'t.pc')]
npc <- t1[,c(col1,'n.pc')]
total <- t1[,c(col1,'nb_bases')]
minlen <- t1[,c(col1,'min_len')]
avglen <- t1[,c(col1,'avg_len')]
maxlen <- t1[,c(col1,'max_len')]
n80 <- t1[,c(col1,'n80')]
n50 <- t1[,c(col1,'n50')]
n20 <- t1[,c(col1,'n20')]
l50 <- t1[,c(col1,'l50')]

pdf(paste(outfile,sep=""))
plot(nbcontigs, type="o", ylab = "Number of Sequences")
plot(apc, type="o", ylab = "A%")
plot(cpc, type="o", ylab = "C%")
plot(gpc, type="o", ylab = "G%")
plot(tpc, type="o", ylab = "T%")
plot(npc, type="o", ylab = "N%")
plot(total, type="o", ylab = "Total Bases")
plot(minlen, type="o", ylab = "Minimum Length")
plot(avglen, type="o", ylab = "Average Length")
plot(maxlen, type="o", ylab = "Maximum Length")
plot(n80, type="o", ylab = "N80")
plot(n50, type="o", ylab = "N50")
plot(n20, type="o", ylab = "N20")
plot(l50, type="o", ylab = "L50")
dev.off()
