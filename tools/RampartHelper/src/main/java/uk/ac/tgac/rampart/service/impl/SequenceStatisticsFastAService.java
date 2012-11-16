
package uk.ac.tgac.rampart.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import uk.ac.tgac.rampart.data.SeqFile;
import uk.ac.tgac.rampart.service.SequenceStatisticsService;

public class SequenceStatisticsFastAService implements SequenceStatisticsService {

	@Override
	public SeqFile analyse(File in) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));
		SeqFile seqStats = new SeqFile();
	    
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        
	    	if (!line.isEmpty()) {
		    	char firstChar = line.charAt(0);
		        
		        // Ignore everything but the sequences
		        // While loop handles multi-line sequences
		        while (firstChar != '>') {
		            // Get the next line (should be the sequence line)
		            String seqPart = reader.readLine();
		            seqStats.addSequencePart(seqPart);
		        }
		        seqStats.incSeqCount();
	    	}
	    }
	    
	    reader.close();
	    
	    return seqStats;
	}
}
