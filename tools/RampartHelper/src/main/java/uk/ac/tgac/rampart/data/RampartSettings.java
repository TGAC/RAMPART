package uk.ac.tgac.rampart.data;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

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
	public static final String KEY_MASS_KMIN 					= "kmin";
	public static final String KEY_MASS_KMAX 					= "kmax";
	public static final String SECT_IMPROVER					= "IMPROVER";
	public static final String KEY_IMPROVER_ITERATIONS 			= "iterations";
	public static final String KEY_IMPROVER_SCAFFOLDING_NAME	= "scaffolding.tool";
	public static final String KEY_IMPROVER_SCAFFOLDING_VERSION	= "scaffolding.version";
	public static final String KEY_IMPROVER_DEGAP_NAME			= "degap.tool";
	public static final String KEY_IMPROVER_DEGAP_VERSION		= "degap.version";
	public static final String KEY_IMPROVER_DEDUP				= "dedup";
	public static final String KEY_IMPROVER_CLIP				= "clip";
	public static final String KEY_IMPROVER_CLIP_MINLEN			= "clip.minlen";
	

	private String 	rampartVersion;
	private String 	qtName;
	private String 	qtVersion;
	private Double 	qtThreshold;
	private Integer qtMinLen;
	private String 	massName;
	private String 	massVersion;
	private Integer massKmin;
	private Integer massKmax;
	private Integer impIterations;
	private String 	impScfName;
	private String 	impScfVersion;
	private String  impDegapName;
	private String  impDegapVersion;
	private Boolean impDedup;
	private Boolean impClip;
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
		
		ini.put(SECT_QT, KEY_QT_TOOL, this.getQtName());
		ini.put(SECT_QT, KEY_QT_VERSION, this.getQtVersion());
		ini.put(SECT_QT, KEY_QT_THRESHOLD, this.getQtThreshold());
		ini.put(SECT_QT, KEY_QT_MINLEN, this.getQtMinLen());
		
		ini.put(SECT_MASS, KEY_MASS_TOOL, this.getMassName());
		ini.put(SECT_MASS, KEY_MASS_VERSION, this.getMassVersion());
		ini.put(SECT_MASS, KEY_MASS_KMIN, this.getMassKmin());
		ini.put(SECT_MASS, KEY_MASS_KMAX, this.getMassKmax());
		
		ini.put(SECT_IMPROVER, KEY_IMPROVER_ITERATIONS, this.getImpIterations());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_SCAFFOLDING_NAME, this.getImpScfName());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_SCAFFOLDING_VERSION, this.getImpScfVersion());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_DEGAP_NAME, this.getImpDegapName());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_DEGAP_VERSION, this.getImpDegapVersion());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_DEDUP, this.getImpDedup());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_CLIP, this.getImpClip());
		ini.put(SECT_IMPROVER, KEY_IMPROVER_CLIP_MINLEN, this.getImpClipMinLen());
		
		ini.store();
	}
	
	protected void setQtFromIniSect(Section iniSection) {
		this.setQtName(iniSection.get(KEY_QT_TOOL));
		this.setQtVersion(iniSection.get(KEY_QT_VERSION));
		this.setQtThreshold(Double.parseDouble(iniSection.get(KEY_QT_THRESHOLD)));
		this.setQtMinLen(Integer.parseInt(iniSection.get(KEY_QT_MINLEN)));
	}
	
	protected void setMassFromIniSect(Section iniSection) {
		this.setMassName(iniSection.get(KEY_MASS_TOOL));
		this.setMassVersion(iniSection.get(KEY_MASS_VERSION));
		this.setMassKmin(Integer.parseInt(iniSection.get(KEY_MASS_KMIN)));
		this.setMassKmax(Integer.parseInt(iniSection.get(KEY_MASS_KMAX)));
	}
	
	protected void setImpFromIniSect(Section iniSection) {
		this.setImpIterations(Integer.parseInt(iniSection.get(KEY_IMPROVER_ITERATIONS)));
		this.setImpScfName(iniSection.get(KEY_IMPROVER_SCAFFOLDING_NAME));
		this.setImpScfVersion(iniSection.get(KEY_IMPROVER_SCAFFOLDING_VERSION));
		this.setImpDegapName(iniSection.get(KEY_IMPROVER_DEGAP_NAME));
		this.setImpDegapVersion(iniSection.get(KEY_IMPROVER_DEGAP_VERSION));
		this.setImpDedup(Boolean.parseBoolean(iniSection.get(KEY_IMPROVER_DEDUP)));
		this.setImpClip(Boolean.parseBoolean(iniSection.get(KEY_IMPROVER_CLIP)));
		this.setImpClipMinLen(Integer.parseInt(iniSection.get(KEY_IMPROVER_CLIP_MINLEN)));
	}


	public String getRampartVersion() {
		return rampartVersion;
	}

	public void setRampartVersion(String rampartVersion) {
		this.rampartVersion = rampartVersion;
	}

	public String getQtName() {
		return qtName;
	}

	public void setQtName(String qtName) {
		this.qtName = qtName;
	}

	public String getQtVersion() {
		return qtVersion;
	}

	public void setQtVersion(String qtVersion) {
		this.qtVersion = qtVersion;
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

	public String getMassName() {
		return massName;
	}

	public void setMassName(String massName) {
		this.massName = massName;
	}

	public String getMassVersion() {
		return massVersion;
	}

	public void setMassVersion(String massVersion) {
		this.massVersion = massVersion;
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

	public Integer getImpIterations() {
		return impIterations;
	}

	public void setImpIterations(Integer impIterations) {
		this.impIterations = impIterations;
	}

	public String getImpScfName() {
		return impScfName;
	}

	public void setImpScfName(String impScfName) {
		this.impScfName = impScfName;
	}

	public String getImpScfVersion() {
		return impScfVersion;
	}

	public void setImpScfVersion(String impScfVersion) {
		this.impScfVersion = impScfVersion;
	}

	public String getImpDegapName() {
		return impDegapName;
	}

	public void setImpDegapName(String impDegapName) {
		this.impDegapName = impDegapName;
	}

	public String getImpDegapVersion() {
		return impDegapVersion;
	}

	public void setImpDegapVersion(String impDegapVersion) {
		this.impDegapVersion = impDegapVersion;
	}

	public Boolean getImpDedup() {
		return impDedup;
	}

	public void setImpDedup(Boolean impDedup) {
		this.impDedup = impDedup;
	}

	public Boolean getImpClip() {
		return impClip;
	}

	public void setImpClip(Boolean impClip) {
		this.impClip = impClip;
	}

	public Integer getImpClipMinLen() {
		return impClipMinLen;
	}

	public void setImpClipMinLen(Integer impClipMinLen) {
		this.impClipMinLen = impClipMinLen;
	}

	
}
