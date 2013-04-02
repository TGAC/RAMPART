#!/usr/bin/perl




# This is going to be a bit complicated...

# Convert to final assembly plus reads to SAM
# bowtie-build asm.fa asm
# For each lib
# 	bowtie -x asm -1 r1.fq -2 r2.fq -S alignments.sam --threads 8

# Convert SAM to AFG to AMOS bank
# e.g. "abyss-sam2afg asm.fa alignments.sam | bank-transact -cfzb assembly.bnk -m -"
	
# Run amosvalidate or FRCurve
# e.g. "amosvalidate assembly_bank" or "FRCurve -D BANK=<bank.bnk>"
	
