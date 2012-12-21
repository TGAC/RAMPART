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
package uk.ac.tgac.rampart.core.data;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class SeqFileTest {

	final long BASE_COUNT = 1000000;
	final long SEQ_COUNT = 10000;
	final long A_COUNT = 250000;
	final long C_COUNT = 200000;
	final long G_COUNT = 200000;
	final long T_COUNT = 300000;
	final long N_COUNT = 5000;
	
	final long GC_COUNT = G_COUNT + C_COUNT;
	final long AT_COUNT = A_COUNT + T_COUNT;
	final long ATGC_COUNT = AT_COUNT + GC_COUNT;
	
	final long GENOME_SIZE = 100000; 
	
	final String SEQ1 = "ATGCATGCATGCN";
	
	private SeqFile sf;
	
	@Before
	public void setUp() {
		this.sf = new SeqFile(
				new File("test_file.fq"), 
				BASE_COUNT, SEQ_COUNT, 
				A_COUNT, C_COUNT, G_COUNT, T_COUNT, N_COUNT);
	}
	
	
	@Test
	public void testGetters() {
		
		assertTrue(sf.getBaseCount() == BASE_COUNT);
		assertTrue(sf.getSeqCount() == SEQ_COUNT);
		assertTrue(sf.getACount() == A_COUNT);
		assertTrue(sf.getCCount() == C_COUNT);
		assertTrue(sf.getGCount() == G_COUNT);
		assertTrue(sf.getTCount() == T_COUNT);
		assertTrue(sf.getNCount() == N_COUNT);
	}

	@Test
	public void testAddSequencePart() {
		this.sf.addSequencePart(SEQ1);
		assertTrue(this.sf.getATGCCount() == ATGC_COUNT + 12);
		assertTrue(this.sf.getNCount() == N_COUNT + 1);
		assertTrue(this.sf.getSeqCount() == SEQ_COUNT);
	}

	@Test
	public void testAddFullSequence() {
		this.sf.addFullSequence(SEQ1);
		assertTrue(this.sf.getATGCCount() == ATGC_COUNT + 12);
		assertTrue(this.sf.getNCount() == N_COUNT + 1);
		assertTrue(this.sf.getSeqCount() == SEQ_COUNT + 1);
	}

	@Test
	public void testIncSeqCount() {
		sf.incSeqCount();
		assertTrue(sf.getSeqCount() == SEQ_COUNT+1);
	}

	@Test
	public void testGetATCount() {
		assertTrue((AT_COUNT) == sf.getATCount());
	}

	@Test
	public void testGetGCCount() {
		assertTrue((GC_COUNT) == sf.getGCCount());
	}

	@Test
	public void testGetATGCCount() {
		assertTrue((ATGC_COUNT) == sf.getATGCCount());
	}

	@Test
	public void testGetGCContent() {
		
		double gcpc = ((double)GC_COUNT / (double)ATGC_COUNT) * 100.0;
		assertTrue(sf.getGCContent() == gcpc);
	}

	@Test
	public void testGetATGCRatio() {
		double atgc_ratio = (double)AT_COUNT / (double)GC_COUNT;
		assertTrue(sf.getATGCRatio() == atgc_ratio);
	}

	@Test
	public void testGetNRatio() {
		assertTrue(sf.getNRatio() == (double)N_COUNT / (double)BASE_COUNT);
	}

	@Test
	public void testGetCoverage() {		
		assertTrue(sf.getCoverage(GENOME_SIZE) == (double)BASE_COUNT / (double)GENOME_SIZE);
	}

	@Test
	public void testGetAverageReadLength() {
		assertTrue(sf.getAverageReadLength() == (double)BASE_COUNT / (double)SEQ_COUNT);
	}

}
