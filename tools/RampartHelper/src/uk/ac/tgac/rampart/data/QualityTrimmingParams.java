package uk.ac.tgac.rampart.data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(schema="rampart",name="qt_params")
public class QualityTrimmingParams {
	
	private Long id;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="project_id")
	private JobDetails project;
	
	private Integer qualityScore;
	private Long minLen;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public JobDetails getProject() {
		return project;
	}
	public void setProject(JobDetails project) {
		this.project = project;
	}
	public Integer getQualityScore() {
		return qualityScore;
	}
	public void setQualityScore(Integer qualityScore) {
		this.qualityScore = qualityScore;
	}
	public Long getMinLen() {
		return minLen;
	}
	public void setMinLen(Long minLen) {
		this.minLen = minLen;
	}
	
	
}
