package uk.ac.tgac.rampart.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(schema = "rampart", name = "seq_file_stats")
public class SeqFileStats implements Serializable {

	private static final long serialVersionUID = 6312248774426910406L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="seq_file_id", referencedColumnName="id")
	private SeqFile seqFile;

	@Column(name = "base_count")
	private Long baseCount;

	@Column(name = "seq_count")
	private Long seqCount;

	@Column(name = "a_count")
	private Long aCount;

	@Column(name = "c_count")
	private Long cCount;

	@Column(name = "g_count")
	private Long gCount;

	@Column(name = "t_count")
	private Long tCount;

	@Column(name = "n_count")
	private Long nCount;
	

	public SeqFileStats() {}
	
	public SeqFileStats(long baseCount, long seqCount, long aCount, long cCount, long gCount,
			long tCount, long nCount) {
		this.baseCount = baseCount;
		this.seqCount = seqCount;
		this.aCount = aCount;
		this.cCount = cCount;
		this.gCount = gCount;
		this.tCount = tCount;
		this.nCount = nCount;
	}

	public void addSequencePart(String part) {
		long length = part.length();
		this.baseCount += length;

		for (int i = 0; i < length; i++) {

			char c = part.charAt(i);

			switch (c) {
			case 'A':
				this.aCount++;
				break;
			case 'C':
				this.cCount++;
				break;
			case 'G':
				this.gCount++;
				break;
			case 'T':
				this.tCount++;
				break;
			case 'N':
				this.nCount++;
				break;
			}
		}
	}

	public void addFullSequence(String seq) {

		addSequencePart(seq);
		this.seqCount++;
	}
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void incSeqCount() {
		this.seqCount++;
	}

	public Long getBaseCount() {
		return baseCount;
	}

	public Long getSeqCount() {
		return seqCount;
	}

	public Long getACount() {
		return aCount;
	}

	public Long getCCount() {
		return cCount;
	}

	public Long getGCount() {
		return gCount;
	}

	public Long getTCount() {
		return tCount;
	}

	public Long getNCount() {
		return nCount;
	}

	public SeqFile getSeqFile() {
		return seqFile;
	}

	public void setSeqFile(SeqFile seqFile) {
		this.seqFile = seqFile;
	}

	public void setACount(Long aCount) {
		this.aCount = aCount;
	}

	public void setCCount(Long cCount) {
		this.cCount = cCount;
	}

	public void setGCount(Long gCount) {
		this.gCount = gCount;
	}

	public void setTCount(Long tCount) {
		this.tCount = tCount;
	}

	public void setNCount(Long nCount) {
		this.nCount = nCount;
	}

	public void setBaseCount(Long baseCount) {
		this.baseCount = baseCount;
	}

	public void setSeqCount(Long seqCount) {
		this.seqCount = seqCount;
	}

	public long getATCount() {
		long atCount = this.getACount() + this.getTCount();
		return atCount;
	}

	public long getGCCount() {
		long gcCount = this.getGCount() + this.getCCount();
		return gcCount;
	}

	public long getATGCCount() {
		long atgcCount = this.getGCCount() + this.getATCount();
		return atgcCount;
	}

	public double getGCContent() {
		double gcContent = ((double) this.getGCCount() / (double) this.getATGCCount()) * 100.0;
		return gcContent;
	}

	public double getATGCRatio() {
		double atgcRatio = (double) this.getATCount() / (double) this.getGCCount();
		return atgcRatio;
	}

	public double getNRatio() {
		double nRatio = (double) this.getNCount() / (double) this.getBaseCount();
		return nRatio;
	}

	public double getCoverage(long genomeSize) {
		double coverage = this.getBaseCount() / genomeSize;
		return coverage;
	}

	public double getAverageReadLength() {
		double avgRdLen = (double) this.getBaseCount() / (double) this.getSeqCount();
		return avgRdLen;
	}

	@Override
	public String toString() {
		return "Sequence Statistics for file: " + (seqFile == null ? "" : seqFile.getFilePath()) + "\n" + "Base Count : "
				+ baseCount + "\n" + "Sequence Count: " + seqCount + "\n" + "A Count: " + aCount + "\n" + "C Count: "
				+ cCount + "\n" + "G Count: " + gCount + "\n" + "T Count: " + tCount + "\n" + "N Count: " + nCount
				+ "\n" + "AT Count: " + getATCount() + "\n" + "GC Count: " + getGCCount() + "\n" + "ATGC Count: "
				+ getATGCCount() + "\n" + "GC%: " + getGCContent() + "\n" + "ATGC Ratio: " + getATGCRatio() + "\n"
				+ "N Ratio: " + getNRatio() + "\n";
	}
}
