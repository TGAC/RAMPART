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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

//@Entity
//@Table(schema="rampart",name="qt_params")
public class QualityTrimmingParams {
	
	private Long id;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="project_id")
	private Job project;
	
	private Integer qualityScore;
	private Long minLen;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Job getProject() {
		return project;
	}
	public void setProject(Job project) {
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
