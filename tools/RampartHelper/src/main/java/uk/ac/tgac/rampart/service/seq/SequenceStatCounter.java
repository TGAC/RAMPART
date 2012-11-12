package uk.ac.tgac.rampart.service.seq;

import java.io.File;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SequenceFileStats;

public abstract class SequenceStatCounter {
	private File in;
	
	private SequenceFileStats stats;
	
	
	public SequenceStatCounter(File in) {
		this.in = in;
		this.stats = new SequenceFileStats(in);
	}
	
	public abstract void count() throws IOException;
	
	public File getIn() {
		return this.in;
	}

	public SequenceFileStats getSeqStats() {
		return this.stats;
	}
}
