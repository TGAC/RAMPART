args <- commandArgs(trailingOnly = TRUE)
print(args)

infile <- args[1]
outfile <- args[2]

t1 <- read.table(infile, header = TRUE, sep="|", quote = "");

col1 <- colnames(data)[1]

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
plot(nbcontigs, type="o")
plot(apc, type="o")
plot(cpc, type="o")
plot(gpc, type="o")
plot(npc, type="o")
plot(total, type="o")
plot(minlen, type="o")
plot(maxlen, type="o")
plot(avglen, type="o")
plot(n50, type="o")
dev.off()


