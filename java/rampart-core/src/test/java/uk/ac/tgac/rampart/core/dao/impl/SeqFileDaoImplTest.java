/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.core.dao.impl;

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
import uk.ac.tgac.rampart.core.dao.SeqFileDao;
import uk.ac.tgac.rampart.core.data.SeqFile;
import uk.ac.tgac.rampart.core.data.SeqFile.FileType;

import java.io.File;

import static org.junit.Assert.assertTrue;

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
