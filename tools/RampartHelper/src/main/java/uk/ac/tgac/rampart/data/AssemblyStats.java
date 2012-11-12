package uk.ac.tgac.rampart.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema="rampart",name="assembly_stats")
public class AssemblyStats {
	
	@Id
	private Long id;
	
	private Long nbContigs;
	private Long nbBases;
	private Double aPerc;
	private Double cPerc;
	private Double gPerc;
	private Double tPerc;
	private Double nPerc;
	private Long n50;
	private Long minLen;
	private Long avgLen;
	private Long maxLen;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public Long getAvgLen() {
		return avgLen;
	}
	public void setAvgLen(Long avgLen) {
		this.avgLen = avgLen;
	}
	public Long getMaxLen() {
		return maxLen;
	}
	public void setMaxLen(Long maxLen) {
		this.maxLen = maxLen;
	}
	
	
}
