package uk.ac.tgac.rampart.dao.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.tgac.rampart.dao.SeqFileDao;
import uk.ac.tgac.rampart.data.SeqFile;
import uk.ac.tgac.rampart.data.SeqFile.FileType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class SeqFileDaoImplTest {
	
	@Autowired 
	ApplicationContext ctx;
	
	private SeqFileDao sfd;
			
	@Before
	public void before() {
		sfd = (SeqFileDao)ctx.getAutowireCapableBeanFactory().createBean(SeqFileDaoImpl.class);
	}
	
	@Test
	@Transactional
	public void testGetSeqFile() {
		SeqFile sf = sfd.getSeqFile(1L);
		
		assertTrue(sf.getFile().getPath().equalsIgnoreCase("/home/maplesod/maplesod_cluster_workarea/GEL-133/Reads/LIB1782_NoIndex_L001_R1_001.fastq"));
		assertTrue(sf.getFileType()==FileType.FASTQ);
		assertTrue(sf.getLibrary().getId()==1);
	}
	
	/*@Test
	public void testGetAllSeqFiles() {
		//List<SeqFile> getAllSeqFiles();
		fail();
	}
	
	@Test
	public void testGetSeqFilesByType() {
		//List<SeqFile> getSeqFileStatsByType(SeqFile.FileType type);
		fail();
	}*/
	
	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testSave() {
		
		long count = sfd.count();
		
		SeqFile sf = new SeqFile(new File("/test/test.fq"), 100L, 10L, 20L, 20L, 20L, 20L, 20L);		
		sfd.persist(sf);
		
		long newCount = sfd.count();
		
		assertTrue(newCount == count+1);
	}

}
