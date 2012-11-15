package uk.ac.tgac.rampart.dao.impl;

import static uk.ac.tgac.rampart.util.RampartHibernate.HBN_SESSION;

import java.util.List;

import org.hibernate.Query;

import uk.ac.tgac.rampart.dao.SeqFileStatsDao;
import uk.ac.tgac.rampart.data.SeqFileStats;
import uk.ac.tgac.rampart.util.RampartHibernate;

public class SeqFileStatsDaoImpl implements SeqFileStatsDao {

	@Override
	public SeqFileStats getSeqFileStats(Long id) {
		SeqFileStats sfs = (SeqFileStats)HBN_SESSION.getSession().load(SeqFileStats.class, id);
		return sfs;
	}
	
	@Override
	public List<SeqFileStats> getAllSeqFileStats() {
		Query q = HBN_SESSION.getSession().createQuery("from SeqFileStats");
		List<SeqFileStats> sfsl = RampartHibernate.listAndCast(q);
		return sfsl;
	}
	
	@Override
	public List<SeqFileStats> getSeqFileStatsByLib(Long libId) {
		Query q = HBN_SESSION.getSession().createQuery("from SeqFileStats where lib_id = :lib_id" );
		q.setParameter("lib_id", libId);
		List<SeqFileStats> sfsl = RampartHibernate.listAndCast(q);		
		return sfsl;
	}
	
	@Override
	public void save(SeqFileStats sfs) {
		HBN_SESSION.getSession().saveOrUpdate(sfs);
	}
}
