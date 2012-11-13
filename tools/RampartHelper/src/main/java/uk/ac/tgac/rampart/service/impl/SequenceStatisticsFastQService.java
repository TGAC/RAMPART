
package uk.ac.tgac.rampart.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.data.SequenceFileStats;
import uk.ac.tgac.rampart.service.SequenceStatisticsService;

@Primary
@Service
public class SequenceStatisticsFastQService implements SequenceStatisticsService {
	
	@Override
	public SequenceFileStats analyse(File in) throws IOException {
				
		BufferedReader reader = new BufferedReader(new FileReader(in), 1024*1000);
		SequenceFileStats seqStats = new SequenceFileStats(in);
	    
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        char firstChar = line.charAt(0);
	        
	        // Ignore everything but the sequences
	        if (firstChar == '@') {
	            // Get the next line (should be the sequence line)
	            line = reader.readLine();
	            seqStats.addFullSequence(line);
	        }
	        
	        // Ignore everything else
	    }
	    
	    reader.close();
		
		return seqStats;
	}
}
