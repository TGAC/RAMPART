package uk.ac.tgac.rampart.data;

import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity
//@Table(schema="rampart",name="mass_stats")
public class ImproverStats extends AssemblyStats {
	
	@Id 
	private Long id;
	
	private Long job_id;
	
	private String stage;
	private String filePath;
	
	public ImproverStats() {}
	
	public ImproverStats(String[] stats) {
		super(Arrays.copyOfRange(stats, 2, 13));
		this.stage = stats[0];
		this.filePath = stats[1];
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
	
	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
		
}
