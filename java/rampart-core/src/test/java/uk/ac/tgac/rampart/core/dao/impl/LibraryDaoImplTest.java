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
import uk.ac.tgac.rampart.core.dao.JobDao;
import uk.ac.tgac.rampart.core.dao.LibraryDao;
import uk.ac.tgac.rampart.core.data.Job;
import uk.ac.tgac.rampart.core.data.Library;
import uk.ac.tgac.rampart.core.data.Library.Dataset;
import uk.ac.tgac.rampart.core.data.Library.Usage;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class LibraryDaoImplTest {

	/*private LibraryDao ld;
	
	@Before
	public void before() {
		this.ld = ctx.getAutowireCapableBeanFactory().createBean(LibraryDaoImpl.class);
	}
	
	@Test
	@Transactional
	public void testGetLibraryLong() {
		Library l = ld.getLibrary(1L);
		
		assertTrue(l.getName().equals("LIB1782"));
		assertTrue(l.getDataset() == Dataset.RAW);
	}

	@Test
	@Transactional
	public void testGetAllLibraries() {
		List<Library> ll = ld.getAllLibraries();
		
		Library row0 = ll.get(0);
		Library row1 = ll.get(1);
		
		assertTrue(row0.getName().equals("LIB1782"));
		assertTrue(row0.getDataset() == Dataset.RAW);
		
		assertTrue(row1.getName().equals("LIB1782"));
		assertTrue(row1.getDataset() == Dataset.QT);
	}

	@Test
	@Transactional
	public void testGetLibraryStringDataset() {
		List<Library> ll = ld.getLibraries("LIB1782", Dataset.RAW);
		
		assertTrue(ll.size() == 2);		
		assertTrue(ll.get(0).getName().equals("LIB1782"));
		assertTrue(ll.get(0).getDataset() == Dataset.RAW);
		assertTrue(ll.get(0).getIndex() == 1);
		assertTrue(ll.get(1).getName().equals("LIB1782"));
		assertTrue(ll.get(1).getDataset() == Dataset.RAW);
		assertTrue(ll.get(1).getIndex() == 2);
	}

	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testPersist() {
		
		JobDao jd = ctx.getAutowireCapableBeanFactory().createBean(JobDaoImpl.class);
		
		
		Library l = new Library();
		l.setName("test_lib");
		l.setDataset(Dataset.RAW);
		l.setAverageInsertSize(500);
		l.setInsertErrorTolerance(0.3);
		l.setReadLength(150);
		l.setUsage("QT,ASM,SCF");
		l.setIndex(1);
		
		Job j = new Job();
		j.setAuthor("libtest_author");
		j.setCollaborator("libtest_collaborator");
		j.setInstitution("libtest_institution");
		j.setTitle("libtest_title");
		j.setJiraSeqinfoId(500L);
		j.setMisoId(500L);
		l.setJob(j);
		
		long count = ld.count(); 
		jd.persist(j);
		ld.persist(l);
		long newCount = ld.count();
		
		assertTrue(newCount == count+1);
	}   */

}
