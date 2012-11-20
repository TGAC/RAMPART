package uk.ac.tgac.rampart.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.data.SeqFile;
import uk.ac.tgac.rampart.service.SequenceStatisticsService;

@Service
public class SequenceStatisticsServiceImpl implements SequenceStatisticsService {

	/**
	 * Calculates and sequence file statistics for the provided sequence file.  Also links the
	 * created sequence file statistics object to the provided sequence file for persistence
	 * purposes
	 * @param sf The sequence file to analyse
	 * @return The statistics related to the sequence file, linked to the sequence file for 
	 * persistence
	 * @throws IOException
	 */
	@Override
	public void analyse(SeqFile in) throws IOException {
		
		SeqFileAnalyser sfa = SeqFileAnalyser.valueOf(in.getFileType().name());
		sfa.analyse(in);
	}
	
	
	public enum SeqFileAnalyser {

		FASTA {
			@Override
			public void analyse(SeqFile in) throws IOException {
				BufferedReader reader = new BufferedReader(new FileReader(in.getFile()));
				
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        
			    	if (!line.isEmpty()) {
				    	char firstChar = line.charAt(0);
				        
				        // Ignore everything but the sequences
				        // While loop handles multi-line sequences
				        while (firstChar != '>') {
				            // Get the next line (should be the sequence line)
				            String seqPart = reader.readLine();
				            in.addSequencePart(seqPart);
				        }
				        in.incSeqCount();
			    	}
			    }
			    
			    reader.close();
			}
		},
		FASTQ {
			@Override
			public void analyse(SeqFile in) throws IOException {
				BufferedReader reader = new BufferedReader(new FileReader(in.getFile()), 1024*1000);
			    
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        char firstChar = line.charAt(0);
			        
			        // Ignore everything but the sequences
			        if (firstChar == '@') {
			            // Get the next line (should be the sequence line)
			            line = reader.readLine();
			            in.addFullSequence(line);
			        }
			        
			        // Ignore everything else
			    }
			    
			    reader.close();
			}
		},
		FASTFASTQ {
			@Override
			public void analyse(SeqFile in) throws IOException {
				
				final int BUFFER_SIZE = 1024 * 32;
				final int MAX_LINE_LENGTH = 200;
				
				FileReader fr = new FileReader(in.getFile());
				//int nlines = 0;

				char buffer[] = new char[BUFFER_SIZE + 1];
				char lineBuf[] = new char[MAX_LINE_LENGTH];

				
				int nChars = 0;
				int nextChar = 0;
				int startChar = 0;
				boolean eol = false;
				boolean seqLineNext = false;
				int lineLength = 0;
				char c = 0;
				int n;
				int j;

				while (true) {
					if (nextChar >= nChars) {
						n = fr.read(buffer, 0, BUFFER_SIZE);
						if (n == -1) { // EOF
							break;
						}
						nChars = n;
						startChar = 0;
						nextChar = 0;
					}

					for (j = nextChar; j < nChars; j++) {
						c = buffer[j];
						if ((c == '\n') || (c == '\r')) {
							eol = true;
							break;
						}
					}
					nextChar = j;

					int len = nextChar - startChar;
					if (eol) {
						nextChar++;
						if ((lineLength + len) > MAX_LINE_LENGTH) {
							fr.close();
							throw new IOException("Line buffer not big enough for this file.  Line buffer size: " + MAX_LINE_LENGTH + ". File: " + in.getFilePath());
						} else {
							System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
						}
						lineLength += len;
						
						if (seqLineNext) {
							in.addFullSequence(String.valueOf(lineBuf));
						}
											
						seqLineNext = lineBuf[0] == '@' ? true : false;
						
						//nlines++;

						if (c == '\r') {
							if (nextChar >= nChars) {
								n = fr.read(buffer, 0, BUFFER_SIZE);
								if (n != -1) {
									nextChar = 0;
									nChars = n;
								}
							}

							if ((nextChar < nChars) && (buffer[nextChar] == '\n'))
								nextChar++;
						}
						startChar = nextChar;
						lineLength = 0;
						continue;
					}

					if ((lineLength + len) > MAX_LINE_LENGTH) {
						fr.close();
						throw new IOException("Line buffer not big enough for this file.  Line buffer size: " + MAX_LINE_LENGTH + ". File: " + in.getFilePath());
					} else {
						System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
					}
					lineLength += len;
				}
				fr.close();
			}
		};
		
		/**
		 * Method that analyses a sequence file and populates the contents
		 * @param sf
		 */
		public abstract void analyse(SeqFile in) throws IOException;
	}
}
