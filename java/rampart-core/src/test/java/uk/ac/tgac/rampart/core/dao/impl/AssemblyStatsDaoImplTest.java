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
import uk.ac.tgac.rampart.core.dao.AssemblyStatsDao;
import uk.ac.tgac.rampart.core.data.AssemblyStats;
import uk.ac.tgac.rampart.core.data.ImproverStats;
import uk.ac.tgac.rampart.core.data.MassStats;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class AssemblyStatsDaoImplTest {

	@Autowired 
	ApplicationContext ctx;
	
	private AssemblyStatsDao asd;
	
	@Before
	public void before() {
		this.asd = (AssemblyStatsDao)ctx.getAutowireCapableBeanFactory().createBean(AssemblyStatsDaoImpl.class);
	}
	
	
	
	@Test
	@Transactional
	public void testGetMassStats() {
		MassStats ms = asd.getMassStats(1L);
		
		assertTrue(ms.getFilePath().equals("./assembly1.fa"));
		assertTrue(ms.getDataset().equals("RAW"));
		assertTrue(!ms.getBest().booleanValue());
	}

	@Test
	@Transactional
	public void testGetImproverStats() {
		ImproverStats is = asd.getImproverStats(3L);
		
		assertTrue(is.getFilePath().equals("./improver1.fa"));
		assertTrue(is.getStage().intValue() == 1);
	}
	
	
	@Test
	@Transactional
	public void testGetAllAssemblyStats() {
		List<AssemblyStats> asl = asd.getAllAssemblyStats();
		
		AssemblyStats as0 = asl.get(0);
		AssemblyStats as2 = asl.get(2);
		
		assertTrue(as0.getFilePath().equals("./assembly1.fa"));
		assertTrue(as2.getFilePath().equals("./improver1.fa"));
	}
	
	
	@Test
	@Transactional
	public void testGetAllMassStats() {
		List<MassStats> msl = asd.getAllMassStats();
		
		MassStats ms0 = msl.get(0);
		MassStats ms2 = msl.get(2);
		
		assertTrue(ms0.getFilePath().equals("./assembly1.fa"));
		assertTrue(ms2.getFilePath().equals("./assembly3.fa"));
	}
	
	@Test
	@Transactional
	public void testGetAllImproverStats() {
		List<ImproverStats> isl = asd.getAllImproverStats();
		
		ImproverStats is0 = isl.get(0);
		ImproverStats is1 = isl.get(1);
		
		assertTrue(is0.getFilePath().equals("./improver1.fa"));
		assertTrue(is1.getFilePath().equals("./improver2.fa"));
	}
	
	
	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testPersistMS() {
		
		MassStats ms = new MassStats();
		ms.setFilePath("/test/mass.fa");
		ms.setNbContigs(100L);
		ms.setNbBases(100000L);
		ms.setaPerc(20.0);
		ms.setcPerc(20.0);
		ms.setgPerc(20.0);
		ms.settPerc(20.0);
		ms.setnPerc(20.0);
		ms.setN50(50000L);
		ms.setMinLen(500L);
		ms.setAvgLen(1000.0);
		ms.setMaxLen(10000L);
		ms.setKmer(85);
		ms.setDataset("RAW");
		ms.setScore(50.0);
		ms.setBest(true);
		
		long count = asd.count(); 
		asd.persist(ms);
		long newCount = asd.count();
		
		assertTrue(newCount == count+1);
		
	}

	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testPersistIS() {
		
		ImproverStats is = new ImproverStats();
		is.setFilePath("/test/amp.fa");
		is.setNbContigs(100L);
		is.setNbBases(100000L);
		is.setaPerc(20.0);
		is.setcPerc(20.0);
		is.setgPerc(20.0);
		is.settPerc(20.0);
		is.setnPerc(20.0);
		is.setN50(50000L);
		is.setMinLen(500L);
		is.setAvgLen(1000.0);
		is.setMaxLen(10000L);
		is.setStage(1);
		
		long count = asd.count(); 
		asd.persist(is);
		long newCount = asd.count();
		
		assertTrue(newCount == count+1);
		
	}

}
