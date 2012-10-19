args <- commandArgs(trailingOnly = TRUE)
print(args)

infile <- args[1]
outfile <- args[2]

t1 <- read.table(infile, header = TRUE, sep="|", quote = "");

# Get individual features

nbcontigs <- t1[,c('kmer','nbcontigs')]
apc <- t1[,c('kmer','a.pc')]
cpc <- t1[,c('kmer','c.pc')]
gpc <- t1[,c('kmer','g.pc')]
tpc <- t1[,c('kmer','t.pc')]
npc <- t1[,c('kmer','n.pc')]
total <- t1[,c('kmer','total')]
minlen <- t1[,c('kmer','minlen')]
maxlen <- t1[,c('kmer','maxlen')]
avglen <- t1[,c('kmer','avglen')]
n50 <- t1[,c('kmer','n50')]

pdf(paste(outfile,sep=""))
plot(nbcontigs, type="o", ylim=c(0,max_y))
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


