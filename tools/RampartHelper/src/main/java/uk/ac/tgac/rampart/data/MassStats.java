package uk.ac.tgac.rampart.data;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("mass")
public class MassStats extends AssemblyStats implements Comparable<MassStats>, Serializable {
	
	private static final long serialVersionUID = 6332206371157656186L;

	@Column(name="kmer")
	private Integer kmer;
	
	@Column(name="dataset")
	private String dataset;
	
	@Column(name="score")
	private Double score;
	
	@Column(name="best")
	private Boolean best = Boolean.FALSE;
	
	public MassStats() {}
	
	public MassStats(String[] stats) {
		super(Arrays.copyOfRange(stats, 1, 13));
		this.kmer = Integer.parseInt(stats[0]);
		this.dataset = stats[13];
		this.score = Double.parseDouble(stats[14]);
		this.best = Boolean.FALSE;
	}
	
	public Integer getKmer() {
		return kmer;
	}
	public void setKmer(Integer kmer) {
		this.kmer = kmer;
	}
	public String getDataset() {
		return dataset;
	}
	public void setDataset(String dataset) {
		this.dataset = dataset;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public Boolean getBest() {
		return best;
	}
	public void setBest(Boolean best) {
		this.best = best;
	}

	@Override
	public int compareTo(MassStats o) {
		return this.score.compareTo(o.getScore());
	}
}
