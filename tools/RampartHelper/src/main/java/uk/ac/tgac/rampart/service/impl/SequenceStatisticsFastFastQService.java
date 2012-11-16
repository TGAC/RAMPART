package uk.ac.tgac.rampart.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.stereotype.Service;

import uk.ac.tgac.rampart.data.SeqFile;
import uk.ac.tgac.rampart.service.SequenceStatisticsService;

@Service
public class SequenceStatisticsFastFastQService implements SequenceStatisticsService {

	private final int BUFFER_SIZE = 1024 * 32;
	private final int MAX_LINE_LENGTH = 200;
	
	/**
	 * WARNING: There are still a few bugs in this code!!!  Do not use in production until fixed!
	 * The algorithm was designed to save time compared to a simple BufferedReader implementation.
	 * It is faster although not significantly (approx 2-3%).  Also the algorithm is currently 
	 * overcounting, this needs to be investigated before being used... although because performance
	 * is not that great it's probably better to stick with the BufferedReader implementation:
	 * {@code SequenceStatisticsFastQService}.
	 */
	@Override
	public SeqFile analyse(File in) throws IOException {

		SeqFile seqStats = new SeqFile();
		
		FileReader fr = new FileReader(in);
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
					throw new IOException("Line buffer not big enough for this file.  Line buffer size: " + MAX_LINE_LENGTH + ". File: " + in.getPath());
				} else {
					System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
				}
				lineLength += len;
				
				if (seqLineNext) {
					seqStats.addFullSequence(String.valueOf(lineBuf));
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
				throw new IOException("Line buffer not big enough for this file.  Line buffer size: " + MAX_LINE_LENGTH + ". File: " + in.getPath());
			} else {
				System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
			}
			lineLength += len;
		}
		fr.close();
		
		return seqStats;
	}
}
