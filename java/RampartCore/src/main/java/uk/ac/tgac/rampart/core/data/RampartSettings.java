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

import java.io.File;
import java.io.IOException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@Entity
@Table(schema="rampart",name="settings")
public class RampartSettings {

	public static final String SECT_RAMPART	 					= "rampart";
	public static final String KEY_RAMPART_VERSION 				= "version";
	public static final String SECT_QT	 						= "QT";
	public static final String KEY_QT_TOOL 						= "tool";
	public static final String KEY_QT_VERSION 					= "version";
	public static final String KEY_QT_THRESHOLD 				= "threshold";
	public static final String KEY_QT_MINLEN 					= "minlen";
	public static final String SECT_MASS 						= "MASS";
	public static final String KEY_MASS_TOOL 					= "tool";
	public static final String KEY_MASS_VERSION 				= "version";
	public static final String KEY_MASS_MEMORY	 				= "memory";
	public static final String KEY_MASS_KMIN 					= "kmin";
	public static final String KEY_MASS_KMAX 					= "kmax";
	public static final String SECT_IMPROVER					= "IMPROVER";
	public static final String KEY_IMPROVER_SCAFFOLDING_TOOL	= "scaffolding.tool";
	public static final String KEY_IMPROVER_SCAFFOLDING_VERSION	= "scaffolding.version";
	public static final String KEY_IMPROVER_SCAFFOLDING_MEMORY  = "scaffolding.memory";
	public static final String KEY_IMPROVER_DEGAP_TOOL			= "degap.tool";
	public static final String KEY_IMPROVER_DEGAP_VERSION		= "degap.version";
	public static final String KEY_IMPROVER_DEGAP_MEMORY		= "degap.memory";
	public static final String KEY_IMPROVER_CLIP_MINLEN			= "clip.minlen";
	
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long 	id;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="job_id", referencedColumnName="id")
	private Job job;
	
	@Column(name="rampart_version")
	private String 	rampartVersion;
	
	@Column(name="qt_tool")
	private String 	qtTool;
	
	@Column(name="qt_tool_version")
	private String 	qtToolVersion;
	
	@Column(name="qt_threshold")
	private Double 	qtThreshold;
	
	@Column(name="qt_minlen")
	private Integer qtMinLen;
	
	@Column(name="mass_tool")
	private String 	massTool;
	
	@Column(name="mass_tool_version")
	private String 	massToolVersion;
	
	@Column(name="mass_memory")
	private Integer massMemory;
	
	@Column(name="mass_kmin")
	private Integer massKmin;
	
	@Column(name="mass_kmax")
	private Integer massKmax;
	
	@Column(name="imp_scf_tool")
	private String 	impScfTool;
	
	@Column(name="imp_scf_tool_version")
	private String 	impScfToolVersion;
	
	@Column(name="imp_scf_memory")
	private Integer	impScfMemory;
	
	@Column(name="imp_degap_tool")
	private String  impDegapTool;
	
	@Column(name="imp_degap_tool_version")
	private String  impDegapToolVersion;
	
	@Column(name="imp_degap_memory")
	private Integer	impDegapMemory;
	
	@Column(name="imp_clip_minlen")
	private Integer impClipMinLen;

	public RampartSettings() {
		
	}
	
	public RampartSettings(RampartJobFileStructure jobDir) throws IOException {
		
		Section qtSection = new Ini(jobDir.getQtLogFile()).get(SECT_QT);
		Section massSection = new Ini(jobDir.getMassLogFile()).get(SECT_MASS);
		Section improverSection = new Ini(jobDir.getImproverLogFile()).get(SECT_IMPROVER);
		
		setQtFromIniSect(qtSection);
		setMassFromIniSect(massSection);
		setImpFromIniSect(improverSection);
	}
	
	public RampartSettings(File iniFile) throws IOException {
		
		loadFile(iniFile);
	}
	
	public void loadFile(File iniFile) throws IOException {
		
		Ini ini = new Ini(iniFile);
		
		Section rampartSection = ini.get(SECT_RAMPART);
		Section qtSection = ini.get(SECT_QT);
		Section massSection = ini.get(SECT_MASS);
		Section improverSection = ini.get(SECT_IMPROVER);
		
		this.setRampartVersion(rampartSection.get(KEY_RAMPART_VERSION));
		
		setQtFromIniSect(qtSection);
		setMassFromIniSect(massSection);
		setImpFromIniSect(improverSection);
	}
	
	public void saveToIni(File iniFile) throws IOException {
		Ini ini = new Ini(iniFile);
		
		ini.put(SECT_RAMPART, KEY_RAMPART_VERSION, this.getRampartVersion());
		
		ini.put(SECT_QT, KEY_QT_TOOL, this.getQtTool());
		ini.put(SECT_QT, KEY_QT_VERSION, this.getQtToolVersion());
		ini.put(SECT_QT, KEY_QT_THRESHOLD, this.getQtThreshold());
		ini.put(SECT_QT, KEY_QT_MINLEN, this.getQtMinLen());
		
		ini.put(SECT_MASS, KEY_MASS_TOOL, this.getMassTool());
		ini.put(SECT_MASS, KEY_MASS_VERSION, this.getMassToolVersion());
		ini.put(SECT_MASS, KEY_MASS_MEMORY, this.getMassMemory());
		ini.put(SECT_MASS, KEY_MASS_KMIN, this.getMassKmin());
		ini.put(SECT_MASS, KEY_MASS_KMAX, this.getMassKmax());
		
		ini.put(SECT_IMPROVER, KEY_IMPROVER_SCAFFOLDING_TOOL, this.getImpScfTool());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_SCAFFOLDING_VERSION, this.getImpScfToolVersion());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_SCAFFOLDING_MEMORY, this.getImpScfMemory());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_DEGAP_TOOL, this.getImpDegapTool());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_DEGAP_VERSION, this.getImpDegapToolVersion());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_DEGAP_MEMORY, this.getImpDegapMemory());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_CLIP_MINLEN, this.getImpClipMinLen());
		
		ini.store();
	}
	
	protected void setQtFromIniSect(Section iniSection) {
		this.setQtTool(iniSection.get(KEY_QT_TOOL));
		this.setQtToolVersion(iniSection.get(KEY_QT_VERSION));
		this.setQtThreshold(Double.parseDouble(iniSection.get(KEY_QT_THRESHOLD)));
		this.setQtMinLen(Integer.parseInt(iniSection.get(KEY_QT_MINLEN)));
	}
	
	protected void setMassFromIniSect(Section iniSection) {
		this.setMassTool(iniSection.get(KEY_MASS_TOOL));
		this.setMassToolVersion(iniSection.get(KEY_MASS_VERSION));
		this.setMassMemory(Integer.parseInt(iniSection.get(KEY_MASS_MEMORY)));
		this.setMassKmin(Integer.parseInt(iniSection.get(KEY_MASS_KMIN)));
		this.setMassKmax(Integer.parseInt(iniSection.get(KEY_MASS_KMAX)));
	}
	
	protected void setImpFromIniSect(Section iniSection) {
		this.setImpScfTool(iniSection.get(KEY_IMPROVER_SCAFFOLDING_TOOL));
		this.setImpScfToolVersion(iniSection.get(KEY_IMPROVER_SCAFFOLDING_VERSION));
		this.setImpScfMemory(Integer.parseInt(iniSection.get(KEY_IMPROVER_SCAFFOLDING_MEMORY)));
		this.setImpDegapTool(iniSection.get(KEY_IMPROVER_DEGAP_TOOL));
		this.setImpDegapToolVersion(iniSection.get(KEY_IMPROVER_DEGAP_VERSION));
		this.setImpDegapMemory(Integer.parseInt(iniSection.get(KEY_IMPROVER_SCAFFOLDING_MEMORY)));
		this.setImpClipMinLen(Integer.parseInt(iniSection.get(KEY_IMPROVER_CLIP_MINLEN)));
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

	public String getRampartVersion() {
		return rampartVersion;
	}

	public void setRampartVersion(String rampartVersion) {
		this.rampartVersion = rampartVersion;
	}

	public String getQtTool() {
		return qtTool;
	}

	public void setQtTool(String qtTool) {
		this.qtTool = qtTool;
	}

	public String getQtToolVersion() {
		return qtToolVersion;
	}

	public void setQtToolVersion(String qtToolVersion) {
		this.qtToolVersion = qtToolVersion;
	}

	public Double getQtThreshold() {
		return qtThreshold;
	}

	public void setQtThreshold(Double qtThreshold) {
		this.qtThreshold = qtThreshold;
	}

	public Integer getQtMinLen() {
		return qtMinLen;
	}

	public void setQtMinLen(Integer qtMinLen) {
		this.qtMinLen = qtMinLen;
	}

	public String getMassTool() {
		return massTool;
	}

	public void setMassTool(String massTool) {
		this.massTool = massTool;
	}

	public String getMassToolVersion() {
		return massToolVersion;
	}

	public void setMassToolVersion(String massToolVersion) {
		this.massToolVersion = massToolVersion;
	}
	
	public Integer getMassMemory() {
		return massMemory;
	}

	public void setMassMemory(Integer massMemory) {
		this.massMemory = massMemory;
	}

	public Integer getMassKmin() {
		return massKmin;
	}

	public void setMassKmin(Integer massKmin) {
		this.massKmin = massKmin;
	}

	public Integer getMassKmax() {
		return massKmax;
	}

	public void setMassKmax(Integer massKmax) {
		this.massKmax = massKmax;
	}

	public String getImpScfTool() {
		return impScfTool;
	}

	public void setImpScfTool(String impScfTool) {
		this.impScfTool = impScfTool;
	}

	public String getImpScfToolVersion() {
		return impScfToolVersion;
	}

	public void setImpScfToolVersion(String impScfToolVersion) {
		this.impScfToolVersion = impScfToolVersion;
	}
	
	public Integer getImpScfMemory() {
		return impScfMemory;
	}

	public void setImpScfMemory(Integer impScfMemory) {
		this.impScfMemory = impScfMemory;
	}

	public String getImpDegapTool() {
		return impDegapTool;
	}

	public void setImpDegapTool(String impDegapTool) {
		this.impDegapTool = impDegapTool;
	}

	public String getImpDegapToolVersion() {
		return impDegapToolVersion;
	}

	public void setImpDegapToolVersion(String impDegapToolVersion) {
		this.impDegapToolVersion = impDegapToolVersion;
	}
	
	public Integer getImpDegapMemory() {
		return impDegapMemory;
	}

	public void setImpDegapMemory(Integer impDegapMemory) {
		this.impDegapMemory = impDegapMemory;
	}

	public Integer getImpClipMinLen() {
		return impClipMinLen;
	}

	public void setImpClipMinLen(Integer impClipMinLen) {
		this.impClipMinLen = impClipMinLen;
	}

	
}
