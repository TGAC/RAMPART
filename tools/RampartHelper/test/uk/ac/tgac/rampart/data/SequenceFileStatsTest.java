package uk.ac.tgac.rampart.data;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SequenceFileStatsTest {

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
	
	private SequenceFileStats sfs;
	
	@Before
	public void setUp() {
		this.sfs = new SequenceFileStats(null, BASE_COUNT, SEQ_COUNT, A_COUNT, C_COUNT, G_COUNT, T_COUNT, N_COUNT);
	}
	
	
	@Test
	public void testGetters() {
		
		assertTrue(sfs.getBaseCount() == BASE_COUNT);
		assertTrue(sfs.getSeqCount() == SEQ_COUNT);
		assertTrue(sfs.getACount() == A_COUNT);
		assertTrue(sfs.getCCount() == C_COUNT);
		assertTrue(sfs.getGCount() == G_COUNT);
		assertTrue(sfs.getTCount() == T_COUNT);
		assertTrue(sfs.getNCount() == N_COUNT);
	}

	@Test
	public void testAddSequencePart() {
		this.sfs.addSequencePart(SEQ1);
		assertTrue(this.sfs.getATGCCount() == ATGC_COUNT + 12);
		assertTrue(this.sfs.getNCount() == N_COUNT + 1);
		assertTrue(this.sfs.getSeqCount() == SEQ_COUNT);
	}

	@Test
	public void testAddFullSequence() {
		this.sfs.addFullSequence(SEQ1);
		assertTrue(this.sfs.getATGCCount() == ATGC_COUNT + 12);
		assertTrue(this.sfs.getNCount() == N_COUNT + 1);
		assertTrue(this.sfs.getSeqCount() == SEQ_COUNT + 1);
	}

	@Test
	public void testIncSeqCount() {
		sfs.incSeqCount();
		assertTrue(sfs.getSeqCount() == SEQ_COUNT+1);
	}

	@Test
	public void testGetATCount() {
		assertTrue((AT_COUNT) == sfs.getATCount());
	}

	@Test
	public void testGetGCCount() {
		assertTrue((GC_COUNT) == sfs.getGCCount());
	}

	@Test
	public void testGetATGCCount() {
		assertTrue((ATGC_COUNT) == sfs.getATGCCount());
	}

	@Test
	public void testGetGCContent() {
		
		double gcpc = ((double)GC_COUNT / (double)ATGC_COUNT) * 100.0;
		assertTrue(sfs.getGCContent() == gcpc);
	}

	@Test
	public void testGetATGCRatio() {
		double atgc_ratio = (double)AT_COUNT / (double)GC_COUNT;
		assertTrue(sfs.getATGCRatio() == atgc_ratio);
	}

	@Test
	public void testGetNRatio() {
		assertTrue(sfs.getNRatio() == (double)N_COUNT / (double)BASE_COUNT);
	}

	@Test
	public void testGetCoverage() {		
		assertTrue(sfs.getCoverage(GENOME_SIZE) == (double)BASE_COUNT / (double)GENOME_SIZE);
	}

	@Test
	public void testGetAverageReadLength() {
		assertTrue(sfs.getAverageReadLength() == (double)BASE_COUNT / (double)SEQ_COUNT);
	}

}
