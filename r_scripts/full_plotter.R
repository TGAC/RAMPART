args <- commandArgs(trailingOnly = TRUE)
print(args)

infile <- args[1]
outfile <- args[2]

t1 <- read.table(infile, header = TRUE, sep="|", quote = "");

# Get individual features

raw <- t1[t1$dataset=="raw",]
qt <- t1[t1$dataset=="qt",]

pdf(outfile)

nbc_range <- range(raw$nbcontigs, qt$nbcontigs)
plot(nbcontigs ~ kmer, data = raw, type = "l", ylab = "Number of Contigs", ylim=nbc_range)
lines(qt$kmer, qt$nbcontigs, col = "red")

apc_range <- range(raw$a.pc, qt$a.pc)
plot(a.pc ~ kmer, data = raw, type = "l", ylab = "A%", ylim=apc_range)
lines(qt$kmer, qt$a.pc, col = "red")

cpc_range <- range(raw$c.pc, qt$c.pc)
plot(c.pc ~ kmer, data = raw, type = "l", ylab = "C%", ylim=cpc_range)
lines(qt$kmer, qt$c.pc, col = "red")

gpc_range <- range(raw$g.pc, qt$g.pc)
plot(g.pc ~ kmer, data = raw, type = "l", ylab = "G%", ylim=gpc_range)
lines(qt$kmer, qt$g.pc, col = "red")

tpc_range <- range(raw$t.pc, qt$t.pc)
plot(t.pc ~ kmer, data = raw, type = "l", ylab = "T%", ylim=tpc_range)
lines(qt$kmer, qt$t.pc, col = "red")

npc_range <- range(raw$n.pc, qt$n.pc)
plot(n.pc ~ kmer, data = raw, type = "l", ylab = "N%", ylim=npc_range)
lines(qt$kmer, qt$n.pc, col = "red")

total_range <- range(raw$total, qt$total)
plot(total ~ kmer, data = raw, type = "l", ylab = "Total Bases", ylim=total_range)
lines(qt$kmer, qt$total, col = "red")

minlen_range <- range(raw$minlen, qt$minlen)
plot(minlen ~ kmer, data = raw, type = "l", ylab = "Minimum Length", ylim=minlen_range)
lines(qt$kmer, qt$minlen, col = "red")

maxlen_range <- range(raw$maxlen, qt$maxlen)
plot(maxlen ~ kmer, data = raw, type = "l", ylab = "Maximum Length", ylim=maxlen_range)
lines(qt$kmer, qt$maxlen, col = "red")

avglen_range <- range(raw$avglen, qt$avglen)
plot(avglen ~ kmer, data = raw, type = "l", ylab = "Average Length", ylim=avglen_range)
lines(qt$kmer, qt$avglen, col = "red")

n50_range <- range(raw$n50, qt$n50)
plot(n50 ~ kmer, data = raw, type = "l", ylab = "N50", ylim=n50_range)
lines(qt$kmer, qt$n50, col = "red")

dev.off()
