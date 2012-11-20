package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.AssemblyStats;
import uk.ac.tgac.rampart.data.ImproverStats;
import uk.ac.tgac.rampart.data.MassStats;

public interface AssemblyStatsDao {

	MassStats getMassStats(Long id);
	
	ImproverStats getImproverStats(Long id);
	
	List<AssemblyStats> getAllAssemblyStats();
	
	List<MassStats> getAllMassStats();
	
	List<ImproverStats> getAllImproverStats();
	
	List<MassStats> getMassStatsForJob(Long jobId);
	
	List<ImproverStats> getImproverStatsForJob(Long jobId);
	
	MassStats getBestAssembly(Long jobId);
	
	long count();
	
	void persist(final MassStats massStats);
	
	void persist(final ImproverStats improverStats);
	
	void persistMassStatsList(final List<MassStats> massStatsList);
	
	void persistImproverStatsList(final List<ImproverStats> improverStatsList);
}
