# R script to plot kmer analysis

args <- commandArgs(trailingOnly = TRUE)
print(args)

datasets_file <- args[1]
out_file <- args[2]
title <- args[3]
x_lim <- args[4]
y_lim <- args[5]

print ("Args set")

datasets <- read.table(datasets_file, header=TRUE, sep=" ", quote ="", as.is=TRUE)

files <- datasets$files
titles <- datasets$titles

colours <- rainbow(length(files))

print ("Datasets file loaded")


# Create merged dataset

merged <- list();

pdf(out_file)
for(i in 1:length(files)) {
	
	dataset <- read.table(files[[i]], header = FALSE, sep=" ", quote="")
	
	if (i == 1) {
		plot(dataset[,1], dataset[,2], main=title, col=colours[i], type="l", ylab="Distinct kmers", xlab="Coverage", xlim=range(0:x_lim), ylim=range(0:y_lim))			
	}
	else {
		lines(dataset[,1], dataset[,2], col=colours[i])
	}
}
legend("topright", legend=titles, cex=0.8, col=colours, lty=1:1)
dev.off()

