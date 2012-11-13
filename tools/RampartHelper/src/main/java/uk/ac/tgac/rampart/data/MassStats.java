package uk.ac.tgac.rampart.data;

import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity
//@Table(schema="rampart",name="mass_stats")
public class MassStats extends AssemblyStats implements Comparable<MassStats> {
	
	@Id 
	private Long id;
	
	private Long job_id;
	
	private Integer kmer;
	private String filePath;
	private String dataset;
	private Double score;
	
	public MassStats() {}
	
	public MassStats(String[] stats) {
		super(Arrays.copyOfRange(stats, 2, 13));
		this.kmer = Integer.parseInt(stats[0]);
		this.filePath = stats[1];
		this.dataset = stats[13];
		//this.score = Double.parseDouble(stats[14]);
		this.score = 0.0;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getJob_id() {
		return job_id;
	}

	public void setJob_id(Long job_id) {
		this.job_id = job_id;
	}

	public Integer getKmer() {
		return kmer;
	}

	public void setKmer(Integer kmer) {
		this.kmer = kmer;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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

	@Override
	public int compareTo(MassStats o) {
		return this.score.compareTo(o.getScore());
	}
	
	
}
