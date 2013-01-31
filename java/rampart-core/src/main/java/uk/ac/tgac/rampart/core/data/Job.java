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

import org.hibernate.annotations.Cascade;
import org.ini4j.Profile.Section;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema="rampart",name="job")
public class Job implements Serializable {
	
	public static final String SECTION_JOB_DETAILS = "JOB";
	
	public static final String KEY_JD_AUTHOR = "author";
	public static final String KEY_JD_COLLABORATOR = "collaborator";
	public static final String KEY_JD_INSTITUTION = "institution";
	public static final String KEY_JD_TITLE = "title";
	public static final String KEY_JD_JIRA_SEQINFO_ID = "jira_seqinfo_id";
	public static final String KEY_JD_MISO_ID = "miso_id";
	
	private static final long serialVersionUID = -8280863990140875866L;

	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="miso_id")
	private Long misoId;
	
	@Column(name="jira_seqinfo_id")
	private Long jiraSeqinfoId;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@Cascade( org.hibernate.annotations.CascadeType.SAVE_UPDATE )
	@JoinColumn(name="job_id")
	private List<Library> libsRaw;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@Cascade( org.hibernate.annotations.CascadeType.SAVE_UPDATE )
	@JoinColumn(name="job_id")
	private List<Library> libsQt;
	
	private String author;
	private String collaborator;
	private String institution;
	private String title;
	
	//private Date startDate;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@Cascade( org.hibernate.annotations.CascadeType.SAVE_UPDATE )
	@JoinColumn(name="job_id")
	private List<MassStats> massStats;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@Cascade( org.hibernate.annotations.CascadeType.SAVE_UPDATE )
	@JoinColumn(name="job_id")
	private List<ImproverStats> improverStats;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="job", targetEntity=RampartSettings.class)
	@Cascade( org.hibernate.annotations.CascadeType.SAVE_UPDATE )
	@JoinColumn(referencedColumnName = "job_id")
	private RampartSettings rampartSettings;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMisoId() {
		return misoId;
	}

	public void setMisoId(Long misoId) {
		this.misoId = misoId;
	}

	public Long getJiraSeqinfoId() {
		return jiraSeqinfoId;
	}

	public void setJiraSeqinfoId(Long jiraSeqinfoId) {
		this.jiraSeqinfoId = jiraSeqinfoId;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCollaborator() {
		return collaborator;
	}

	public void setCollaborator(String collaborator) {
		this.collaborator = collaborator;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	/*
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}*/

	public List<Library> getLibsRaw() {
		return libsRaw;
	}
	
	public void setLibsRaw(List<Library> libsRaw) {
		if (libsRaw != null) {
			for(Library lib : libsRaw) {
				lib.setJob(this);
			}
		}
		
		this.libsRaw = libsRaw;
	}
	
	public List<Library> getLibsQt() {
		return libsQt;
	}

	public void setLibsQt(List<Library> libsQt) {
		
		if (libsQt != null) {
			for(Library lib : libsQt) {
				lib.setJob(this);
			}
		}
		
		this.libsQt = libsQt;
	}
	
	
	
	public List<MassStats> getMassStats() {
		return massStats;
	}

	public void setMassStats(List<MassStats> massStats) {
		if (massStats != null) {
			for(MassStats stats : massStats) {
				stats.setJob(this);
			}
		}
		this.massStats = massStats;
	}

	public List<ImproverStats> getImproverStats() {
		return improverStats;
	}

	public void setImproverStats(List<ImproverStats> improverStats) {
		if (improverStats != null) {
			for(ImproverStats stats : improverStats) {
				stats.setJob(this);
			}
		}
		this.improverStats = improverStats;
	}
	
	public RampartSettings getRampartSettings() {
		return this.rampartSettings;
	}
	
	public void setRampartSettings(RampartSettings rampartSettings) {		
		if (rampartSettings != null) {
			rampartSettings.setJob(this);
		}
		this.rampartSettings = rampartSettings;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[JOB_DETAILS]\n")
		.append(KEY_JD_AUTHOR + "=" + this.getAuthor() + "\n")
		.append(KEY_JD_COLLABORATOR + "=" + this.getAuthor() + "\n")
		.append(KEY_JD_INSTITUTION + "=" + this.getAuthor() + "\n")
		.append(KEY_JD_TITLE + "=" + this.getAuthor() + "\n")
		.append(KEY_JD_JIRA_SEQINFO_ID + "=" + this.getAuthor() + "\n")
		.append(KEY_JD_MISO_ID + "=" + this.getAuthor() + "\n");
		//.append(this.getLibraries().toString());
		
		return sb.toString();
	}
	
	public static Job parseIniSection(Section iniSection) {
		Job jd = new Job();
		jd.setAuthor(iniSection.get(KEY_JD_AUTHOR));
		jd.setCollaborator(iniSection.get(KEY_JD_COLLABORATOR));
		jd.setInstitution(iniSection.get(KEY_JD_INSTITUTION));
		jd.setTitle(iniSection.get(KEY_JD_TITLE));
		jd.setJiraSeqinfoId(Long.parseLong(iniSection.get(KEY_JD_JIRA_SEQINFO_ID).replaceAll("SEQINFO-", "")));
		jd.setMisoId(Long.parseLong(iniSection.get(KEY_JD_MISO_ID).replaceAll("PRO", "")));	
		return jd;
	}
}


