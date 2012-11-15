package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.SeqFile;

public interface SeqFileDao {

	SeqFile getSeqFile(Long id);
	
	List<SeqFile> getAllSeqFiles();
	
	List<SeqFile> getSeqFileStatsByType(SeqFile.FileType type);
	
	void save(SeqFile sf);
}
