package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.SeqFileStats;

public interface SeqFileStatsDao {

	SeqFileStats getSeqFileStats(Long id);
	
	List<SeqFileStats> getAllSeqFileStats();
	
	List<SeqFileStats> getSeqFileStatsByLib(Long libId);
	
	void save(SeqFileStats sfs);
}
