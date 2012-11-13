package uk.ac.tgac.rampart.data;


public class AssemblyStats {
	
	private Long nbContigs;
	private Long nbBases;
	private Double aPerc;
	private Double cPerc;
	private Double gPerc;
	private Double tPerc;
	private Double nPerc;
	private Long n50;
	private Long minLen;
	private Double avgLen;
	private Long maxLen;
	
	public AssemblyStats() {}
	public AssemblyStats(String[] stats) {
		this.nbContigs = Long.parseLong(stats[0]);
		this.aPerc = Double.parseDouble(stats[1]);
		this.cPerc = Double.parseDouble(stats[2]);
		this.gPerc = Double.parseDouble(stats[3]);
		this.tPerc = Double.parseDouble(stats[4]);
		this.nPerc = Double.parseDouble(stats[5]);
		this.nbBases = Long.parseLong(stats[6]);
		this.minLen = Long.parseLong(stats[7]);
		this.maxLen = Long.parseLong(stats[8]);
		this.avgLen = Double.parseDouble(stats[9]);
		this.n50 = Long.parseLong(stats[10]);
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
