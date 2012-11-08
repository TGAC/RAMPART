
package uk.ac.tgac.rampart.service.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SequenceFileStats;

public class FastQStatCounter extends SequenceStatCounter {

	public FastQStatCounter(File in) {
		super(in);
	}
	
	@Override
	public void count() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.getIn()));
	    
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        char firstChar = line.charAt(0);
	        
	        // Ignore everything but the sequences
	        if (firstChar == '@') {
	            // Get the next line (should be the sequence line)
	            line = reader.readLine();
	            this.getSeqStats().addFullSequence(line);
	        }
	        
	        // Ignore everything else
	    }
	    
	    reader.close();
	}

	public static SequenceFileStats analyse(File in) throws IOException {
		FastQStatCounter counter = new FastQStatCounter(in);
		counter.count();
		return counter.getSeqStats();
	}
}
