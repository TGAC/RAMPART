args <- commandArgs(trailingOnly = TRUE)
print(args)

infile <- args[1]
outfile <- args[2]

t1 <- read.table(infile, header = TRUE, sep="|", quote = "");

col1 <- colnames(t1)[1]

# Get individual features

nbcontigs <- t1[,c(col1,'nbcontigs')]
apc <- t1[,c(col1,'a.pc')]
cpc <- t1[,c(col1,'c.pc')]
gpc <- t1[,c(col1,'g.pc')]
tpc <- t1[,c(col1,'t.pc')]
npc <- t1[,c(col1,'n.pc')]
total <- t1[,c(col1,'total')]
minlen <- t1[,c(col1,'minlen')]
maxlen <- t1[,c(col1,'maxlen')]
avglen <- t1[,c(col1,'avglen')]
n50 <- t1[,c(col1,'n50')]

pdf(paste(outfile,sep=""))
plot(nbcontigs, type="o", ylab = "Number of Contigs")
plot(apc, type="o", ylab = "A%")
plot(cpc, type="o", ylab = "C%")
plot(gpc, type="o", ylab = "G%")
plot(tpc, type="o", ylab = "T%")
plot(npc, type="o", ylab = "N%")
plot(total, type="o", ylab = "Total Bases")
plot(minlen, type="o", ylab = "Minimum Length")
plot(maxlen, type="o", ylab = "Maximum Length")
plot(avglen, type="o", ylab = "Average Length")
plot(n50, type="o", ylab = "N50")
dev.off()


