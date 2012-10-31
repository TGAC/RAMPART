#args <- commandArgs(trailingOnly = TRUE)
#print(args)

#infile <- args[1]
#outfile <- args[2]

#t1 <- read.table(infile, header = TRUE, sep="|", quote = "");

# Get individual features

raw <- t1[t1$dataset=="raw",]
qt <- t1[t1$dataset=="qt",]
cs <- t1[t1$dataset=="cs",]





pdf(paste("mv_assembly_plots.pdf",sep=""))

nbc_range <- range(raw$nbcontigs, qt$nbcontigs, cs$nbcontigs)
plot(nbcontigs ~ kmer, data = raw, type = "l", ylab = "Number of Contigs", ylim=nbc_range)
lines(qt$kmer, qt$nbcontigs, col = "blue")
lines(cs$kmer, cs$nbcontigs, col = "red")

apc_range <- range(raw$a.pc, qt$a.pc, cs$a.pc)
plot(a.pc ~ kmer, data = raw, type = "l", ylab = "A%", ylim=apc_range)
lines(qt$kmer, qt$a.pc, col = "blue")
lines(cs$kmer, cs$a.pc, col = "red")

cpc_range <- range(raw$c.pc, qt$c.pc, cs$c.pc)
plot(c.pc ~ kmer, data = raw, type = "l", ylab = "C%", ylim=cpc_range)
lines(qt$kmer, qt$c.pc, col = "blue")
lines(cs$kmer, cs$c.pc, col = "red")

gpc_range <- range(raw$g.pc, qt$g.pc, cs$g.pc)
plot(g.pc ~ kmer, data = raw, type = "l", ylab = "G%", ylim=gpc_range)
lines(qt$kmer, qt$g.pc, col = "blue")
lines(cs$kmer, cs$g.pc, col = "red")

tpc_range <- range(raw$t.pc, qt$t.pc, cs$t.pc)
plot(t.pc ~ kmer, data = raw, type = "l", ylab = "T%", ylim=tpc_range)
lines(qt$kmer, qt$t.pc, col = "blue")
lines(cs$kmer, cs$t.pc, col = "red")

npc_range <- range(raw$n.pc, qt$n.pc, cs$n.pc)
plot(n.pc ~ kmer, data = raw, type = "l", ylab = "N%", ylim=npc_range)
lines(qt$kmer, qt$n.pc, col = "blue")
lines(cs$kmer, cs$n.pc, col = "red")

total_range <- range(raw$total, qt$total, cs$total)
plot(total ~ kmer, data = raw, type = "l", ylab = "Total Bases", ylim=total_range)
lines(qt$kmer, qt$total, col = "blue")
lines(cs$kmer, cs$total, col = "red")

minlen_range <- range(raw$minlen, qt$minlen, cs$minlen)
plot(minlen ~ kmer, data = raw, type = "l", ylab = "Minimum Length", ylim=minlen_range)
lines(qt$kmer, qt$minlen, col = "blue")
lines(cs$kmer, cs$minlen, col = "red")

maxlen_range <- range(raw$maxlen, qt$maxlen, cs$maxlen)
plot(maxlen ~ kmer, data = raw, type = "l", ylab = "Maximum Length", ylim=maxlen_range)
lines(qt$kmer, qt$maxlen, col = "blue")
lines(cs$kmer, cs$maxlen, col = "red")

avglen_range <- range(raw$avglen, qt$avglen, cs$avglen)
plot(avglen ~ kmer, data = raw, type = "l", ylab = "Average Length", ylim=avglen_range)
lines(qt$kmer, qt$avglen, col = "blue")
lines(cs$kmer, cs$avglen, col = "red")

n50_range <- range(raw$n50, qt$n50, cs$n50)
plot(n50 ~ kmer, data = raw, type = "l", ylab = "N50", ylim=n50_range)
lines(qt$kmer, qt$n50, col = "blue")
lines(cs$kmer, cs$n50, col = "red")

dev.off()
