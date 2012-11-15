package uk.ac.tgac.rampart.dao.impl;

import static uk.ac.tgac.rampart.util.RampartHibernate.HBN_SESSION;

import java.util.List;

import org.hibernate.Query;

import uk.ac.tgac.rampart.dao.SeqFileDao;
import uk.ac.tgac.rampart.data.SeqFile;
import uk.ac.tgac.rampart.util.RampartHibernate;

public class SeqFileDaoImpl implements SeqFileDao {
	
	@Override
	public SeqFile getSeqFile(Long id) {
		SeqFile sf = (SeqFile)HBN_SESSION.getSession().load(SeqFile.class, id);
		return sf;
	}
	
	@Override
	public List<SeqFile> getAllSeqFiles() {
		Query q = HBN_SESSION.getSession().createQuery("from SeqFile");
		List<SeqFile> sfl = RampartHibernate.listAndCast(q);
		return sfl;
	}
	
	@Override
	public List<SeqFile> getSeqFileStatsByType(SeqFile.FileType type) {
		Query q = HBN_SESSION.getSession().createQuery("from SeqFileStats where file_type = :file_type" );
		q.setParameter("file_type", type);
		List<SeqFile> sfl = RampartHibernate.listAndCast(q);		
		return sfl;
	}
	
	@Override
	public void save(SeqFile sf) {
		HBN_SESSION.getSession().saveOrUpdate(sf);
	}
}
