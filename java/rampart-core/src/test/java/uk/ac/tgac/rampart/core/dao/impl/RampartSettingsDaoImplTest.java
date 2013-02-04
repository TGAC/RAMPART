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
import uk.ac.tgac.rampart.core.dao.RampartSettingsDao;
import uk.ac.tgac.rampart.core.data.RampartSettings;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class RampartSettingsDaoImplTest {

	@Autowired 
	ApplicationContext ctx;
	
	private RampartSettingsDao rsd;
	
	@Before
	public void before() {
		this.rsd = ctx.getAutowireCapableBeanFactory().createBean(RampartSettingsDaoImpl.class);
	}
	
	@Test
	@Transactional
	public void testGetAllJobs() {
		List<RampartSettings> rsl = rsd.getAllRampartSettings();
		
		RampartSettings row0 = rsl.get(0);
		
		assertTrue(row0.getRampartVersion().equals("0.2"));
	}

	@Test
	@Transactional
	public void testGetJob() {
		RampartSettings rs = rsd.getRampartSettings(1L);
		
		assertTrue(rs.getRampartVersion().equals("0.2"));
	}
	
	@Test
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Rollback(true)
	public void testPersist() {
		
		RampartSettings rs = new RampartSettings();
		rs.setRampartVersion("0.2");
		rs.setQtTool("sickle");
		rs.setQtToolVersion("1.1");
		rs.setQtThreshold(0.3);
		rs.setQtMinLen(75);
		rs.setMassTool("abyss");
		rs.setMassToolVersion("1.3.4");
		rs.setMassKmin(41);
		rs.setMassKmax(95);
		rs.setImpScfTool("sspace");
		rs.setImpScfToolVersion("2.0-Basic");
		rs.setImpDegapTool("gapcloser");
		rs.setImpDegapToolVersion("1.12");
		rs.setImpClipMinLen(1000);
				
		long count = rsd.count(); 
		rsd.persist(rs);
		long newCount = rsd.count();
		
		assertTrue(newCount == count+1);
		
	}
	
}
