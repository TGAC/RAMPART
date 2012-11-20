package uk.ac.tgac.rampart.data;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ini4j.Profile.Section;

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
	
	@OneToMany(fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	private List<Library> libsRaw;
	
	@OneToMany(fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	private List<Library> libsQt;
	
	private String author;
	private String collaborator;
	private String institution;
	private String title;
	
	//private Date startDate;
	
	@OneToMany(fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	private List<MassStats> massStats;
	
	@OneToMany(fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	private List<ImproverStats> improverStats;
	
	
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
		this.libsRaw = libsRaw;
	}
	
	public List<Library> getLibsQt() {
		return libsQt;
	}

	public void setLibsQt(List<Library> libsQt) {
		this.libsQt = libsQt;
	}
	
	
	
	public List<MassStats> getMassStats() {
		return massStats;
	}

	public void setMassStats(List<MassStats> massStats) {
		this.massStats = massStats;
	}

	public List<ImproverStats> getImproverStats() {
		return improverStats;
	}

	public void setImproverStats(List<ImproverStats> improverStats) {
		this.improverStats = improverStats;
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
		jd.setId(-1L);
		jd.setAuthor(iniSection.get(KEY_JD_AUTHOR));
		jd.setCollaborator(iniSection.get(KEY_JD_COLLABORATOR));
		jd.setInstitution(iniSection.get(KEY_JD_INSTITUTION));
		jd.setTitle(iniSection.get(KEY_JD_TITLE));
		jd.setJiraSeqinfoId(Long.parseLong(iniSection.get(KEY_JD_JIRA_SEQINFO_ID)));
		jd.setMisoId(Long.parseLong(iniSection.get(KEY_JD_MISO_ID)));	
		return jd;
	}
}


