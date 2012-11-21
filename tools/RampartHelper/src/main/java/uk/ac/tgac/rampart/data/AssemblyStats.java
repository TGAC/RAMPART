package uk.ac.tgac.rampart.data;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="assembly_type",discriminatorType=DiscriminatorType.STRING)
@Table(schema="rampart",name="assembly_stats")
@DiscriminatorValue("generic")
public class AssemblyStats implements Serializable {
	
	private static final long serialVersionUID = -1893203475167525362L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="job_id")
	private Job job;
	
	@Column(name="file_path")
	private String filePath;
	
	@Column(name="nb_contigs")
	private Long nbContigs;
	
	@Column(name="nb_bases")
	private Long nbBases;
	
	@Column(name="a_perc")
	private Double aPerc;
	
	@Column(name="c_perc")
	private Double cPerc;
	
	@Column(name="g_perc")
	private Double gPerc;
	
	@Column(name="t_perc")
	private Double tPerc;
	
	@Column(name="n_perc")
	private Double nPerc;
	
	@Column(name="n50")
	private Long n50;
	
	@Column(name="min_len")
	private Long minLen;
	
	@Column(name="avg_len")
	private Double avgLen;
	
	@Column(name="max_len")
	private Long maxLen;
	
	public AssemblyStats() {}
	public AssemblyStats(String[] stats) {
		this.filePath = stats[0];
		this.nbContigs = Long.parseLong(stats[1]);
		this.aPerc = Double.parseDouble(stats[2]);
		this.cPerc = Double.parseDouble(stats[3]);
		this.gPerc = Double.parseDouble(stats[4]);
		this.tPerc = Double.parseDouble(stats[5]);
		this.nPerc = Double.parseDouble(stats[6]);
		this.nbBases = Long.parseLong(stats[7]);
		this.minLen = Long.parseLong(stats[8]);
		this.maxLen = Long.parseLong(stats[9]);
		this.avgLen = Double.parseDouble(stats[10]);
		this.n50 = Long.parseLong(stats[11]);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Long getNbContigs() {
		return nbContigs;
	}
	public void setNbContigs(Long nbContigs) {
		this.nbContigs = nbContigs;
	}
	public Long getNbBases() {
		return nbBases;
	}
	public void setNbBases(Long nbBases) {
		this.nbBases = nbBases;
	}
	public Double getaPerc() {
		return aPerc;
	}
	public void setaPerc(Double aPerc) {
		this.aPerc = aPerc;
	}
	public Double getcPerc() {
		return cPerc;
	}
	public void setcPerc(Double cPerc) {
		this.cPerc = cPerc;
	}
	public Double getgPerc() {
		return gPerc;
	}
	public void setgPerc(Double gPerc) {
		this.gPerc = gPerc;
	}
	public Double gettPerc() {
		return tPerc;
	}
	public void settPerc(Double tPerc) {
		this.tPerc = tPerc;
	}
	public Double getnPerc() {
		return nPerc;
	}
	public void setnPerc(Double nPerc) {
		this.nPerc = nPerc;
	}
	public Long getN50() {
		return n50;
	}
	public void setN50(Long n50) {
		this.n50 = n50;
	}
	public Long getMinLen() {
		return minLen;
	}
	public void setMinLen(Long minLen) {
		this.minLen = minLen;
	}
	public Double getAvgLen() {
		return avgLen;
	}
	public void setAvgLen(Double avgLen) {
		this.avgLen = avgLen;
	}
	public Long getMaxLen() {
		return maxLen;
	}
	public void setMaxLen(Long maxLen) {
		this.maxLen = maxLen;
	}
	
	
}
