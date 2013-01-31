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
@DiscriminatorValue("improver")
public class ImproverStats extends AssemblyStats implements Comparable<ImproverStats>, Serializable {
	
	private static final long serialVersionUID = 3838911367218298049L;
	
	private Integer stage;
	
	@Column(name="final")
	private Boolean finalAssembly;
	
	public ImproverStats() {}
	
	public ImproverStats(String[] stats) {
		super(Arrays.copyOfRange(stats, 1, 13));
		this.stage = Integer.parseInt(stats[0]);		
	}
	
	public Integer getStage() {
		return stage;
	}
	
	public void setStage(Integer stage) {
		this.stage = stage;
	}
	
	public Boolean isFinalAssembly() {
		return finalAssembly;
	}

	public void setFinalAssembly(Boolean finalAssembly) {
		this.finalAssembly = finalAssembly;
	}

	@Override
	public int compareTo(ImproverStats o) {
		return this.stage.compareTo(o.getStage());
	}
}
