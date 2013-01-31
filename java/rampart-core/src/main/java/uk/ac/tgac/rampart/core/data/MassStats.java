/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.core.data;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Arrays;

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
