
# This script creates cumulative length plots for a number of datasets

# Get arguments from the command line and print to console
args <- commandArgs(trailingOnly = TRUE);
print(args);

# Input file
infile <- args[1];

# Output file
outfile <- args[2];

# Limit on the x-axis and the tail on the X is likely not to contain useful information
max_x <- args[3];

# Load the datasets file and store in a dataframe, separate file paths from dataset titles.
datasets.df <- read.table(infile, header = TRUE, sep=" ", quote = "", as.is = TRUE);

files <- datasets.df$files;
titles <- datasets.df$titles;

# Create merged dataset

merged <- list();

# For each dataset load the file path
for(i in 1:length(files)) {
	dataset.df <- read.table(files[[i]], header = TRUE, sep="|", quote="");
	dataset.df$title <- titles[[i]];
	dataset.df$index <- c(1:nrow(dataset.df));	
	
	merged[[i]] <- dataset.df;
}

print (merged);

max_height <- 0;
max_width <- 0;

# Determine max height and width of the plot
for(i in 1:length(files)) {
	max_height <- max(max_height, max(merged[[i]]$Cumulative_len));
	max_width <- max(max_width, max(merged[[i]]$index));
}

print (max_height);

# Create colors for each dataset
col <- rainbow(length(files));

# If max_x command line arg is greater 0 then cap the width
if (max_x > 0) {
	max_width <- as.numeric(max_x); 
}

# Create the first plot (defining all the plot properties)
pdf(paste(outfile,sep=""))
plot(Cumulative_len ~ index, 
	data=merged[[1]], 
	type="l", 
	main = "Cumulative Length Distributions", 
	xlim = c(1,max_width),
	ylim = c(0,max_height), 
	xlab = "Length Sorted Sequence Index x1000", 
	ylab = "Cumulative Length",
	col=col[[1]]);

# Add the rest of the datasets to the plot
for(i in 2:length(files)) {
	lines(merged[[i]]$index, merged[[i]]$Cumulative_len, col = col[[i]]);
}

# Add the legend to the plot, using the titles specified in the input file and the color vector we
# created earlier
legend("bottomright", legend = titles, cex = 0.8, fill = col);

dev.off()
