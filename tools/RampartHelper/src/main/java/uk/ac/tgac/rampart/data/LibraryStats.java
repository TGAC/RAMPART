package uk.ac.tgac.rampart.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema="rampart",name="library_stats")
public class LibraryStats {
	
	@Id 
	private Long id;
	
	private Long nbReads;
	private Long nbBases;
	private Double gcPerc;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getNbReads() {
		return nbReads;
	}
	public void setNbReads(Long nbReads) {
		this.nbReads = nbReads;
	}
	public Long getNbBases() {
		return nbBases;
	}
	public void setNbBases(Long nbBases) {
		this.nbBases = nbBases;
	}
	public Double getGcPerc() {
		return gcPerc;
	}
	public void setGcPerc(Double gcPerc) {
		this.gcPerc = gcPerc;
	}
}
