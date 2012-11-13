package uk.ac.tgac.rampart.data;

import java.io.File;

public class SequenceFileStats {

	private File file;
	private long baseCount;
	private long seqCount;
	private long aCount;
	private long cCount;
	private long gCount;
	private long tCount;
	private long nCount;
	
	
	public SequenceFileStats() {
		this(null);
	}
	
	public SequenceFileStats(File file) {
		this(file,0,0,0,0,0,0,0);
	}
	
	public SequenceFileStats(File file, long baseCount, long seqCount, long aCount, long cCount,
			long gCount, long tCount, long nCount) {
		this.file = file;
		this.baseCount = baseCount;
		this.seqCount = seqCount;
		this.aCount = aCount;
		this.cCount = cCount;
		this.gCount = gCount;
		this.tCount = tCount;
		this.nCount = nCount;
	}
	
	public void addSequencePart(String part) {
		long length = part.length();
        this.baseCount += length;
        
        for(int i = 0; i < length; i++) {
        	
        	char c = part.charAt(i);
        	
        	switch (c) {
        	case 'A': 
        		this.aCount++;
        		break;
        	case 'C':
        		this.cCount++;
        		break;
        	case 'G':
        		this.gCount++;
        		break;
        	case 'T':
        		this.tCount++;
        		break;
        	case 'N':
        		this.nCount++;
        		break;
        	}
        }
	}
	
	public void addFullSequence(String seq) {
		
		addSequencePart(seq);
		this.seqCount++;
    }
	
	public void incSeqCount() {
		this.seqCount++;
	}

	public long getBaseCount() {
		return baseCount;
	}

	public long getSeqCount() {
		return seqCount;
	}

	public long getACount() {
		return aCount;
	}

	public long getCCount() {
		return cCount;
	}

	public long getGCount() {
		return gCount;
	}

	public long getTCount() {
		return tCount;
	}

	public long getNCount() {
		return nCount;
	}
	
	public long getATCount() {
		long atCount = this.getACount() + this.getTCount();
		return atCount;
	}
	
	public long getGCCount() {
		long gcCount = this.getGCount() + this.getCCount();
		return gcCount;
	}
	
	public long getATGCCount() {
		long atgcCount = this.getGCCount() + this.getATCount();
		return atgcCount;
	}
	
	public double getGCContent() {		
		double gcContent = ((double)this.getGCCount() / (double)this.getATGCCount()) * 100.0;
		return gcContent;
	}
	
	public double getATGCRatio() {
		double atgcRatio = (double)this.getATCount() / (double)this.getGCCount();
		return atgcRatio;
	}
	
	public double getNRatio() {
		double nRatio = (double)this.getNCount() / (double)this.getBaseCount();
		return nRatio;
	}
	
	public double getCoverage(long genomeSize) {
		double coverage = this.getBaseCount() / genomeSize; 
		return coverage;
	}
	
	public double getAverageReadLength() {
		double avgRdLen = (double)this.getBaseCount() / (double)this.getSeqCount();
		return avgRdLen;
	}
	
	@Override
	public String toString() {
		return 	"Sequence Statistics for file: " + (file == null ? "" : file.getPath()) + "\n" +
				"Base Count : " + baseCount + "\n" +
				"Sequence Count: " + seqCount + "\n" +
				"A Count: " + aCount + "\n" +
				"C Count: " + cCount + "\n" +
				"G Count: " + gCount + "\n" +
				"T Count: " + tCount + "\n" +
				"N Count: " + nCount + "\n" +
				"AT Count: " + getATCount() + "\n" +
				"GC Count: " + getGCCount() + "\n" +
				"ATGC Count: " + getATGCCount() + "\n" +
				"GC%: " + getGCContent() + "\n" +
				"ATGC Ratio: " + getATGCRatio() + "\n" +
				"N Ratio: " + getNRatio() + "\n";
	}
}
