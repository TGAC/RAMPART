
package uk.ac.tgac.rampart.service.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SequenceFileStats;

public class FastAStatCounter extends SequenceStatCounter {

	public FastAStatCounter(File in) {
		super(in);
	}
	
	@Override
	public void count() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.getIn()));
	    
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        
	    	if (!line.isEmpty()) {
		    	char firstChar = line.charAt(0);
		        
		        // Ignore everything but the sequences
		        // While loop handles multi-line sequences
		        while (firstChar != '>') {
		            // Get the next line (should be the sequence line)
		            String seqPart = reader.readLine();
		            this.getSeqStats().addSequencePart(seqPart);
		        }
		        this.getSeqStats().incSeqCount();
	    	}
	    }
	    
	    reader.close();
	}

	public static SequenceFileStats analyse(File in) throws IOException {
		FastAStatCounter counter = new FastAStatCounter(in);
		counter.count();
		return counter.getSeqStats();
	}
}
