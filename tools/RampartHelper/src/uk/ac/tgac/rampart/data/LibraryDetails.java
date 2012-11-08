package uk.ac.tgac.rampart.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@Entity
@Table(schema="rampart",name="lib_details")
public class LibraryDetails implements Serializable {
	
	private static final long serialVersionUID = 9110367505701278888L;

	public enum Usage {
		
		ASSEMBLY_ONLY,
		SCAFFOLDING_ONLY,
		ASSEMBLY_AND_SCAFFOLDING		
	}
	
	public enum FileType {
		FASTA,
		FASTQ
	}
	
	
	public static final String SECTION_LIB_PREFIX = "LIB";
	
	public static final String KEY_LIB_NAME = "name";
	
	public static final String KEY_LIB_AVG_INSERT_SIZE = "avg_ins_size";
	public static final String KEY_LIB_INSERT_ERROR_TOLERANCE = "ins_err_tolerance";
	public static final String KEY_LIB_USAGE = "usage";
	public static final String KEY_LIB_ORDER = "order";
	
	public static final String KEY_LIB_FILE_TYPE = "type";
	public static final String KEY_LIB_FILE_1 = "file_paired_1";
	public static final String KEY_LIB_FILE_2 = "file_paired_2";
	public static final String KEY_LIB_FILE_SE = "file_single_end";
	
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	@Column(name=KEY_LIB_AVG_INSERT_SIZE)
	private Integer averageInsertSize;
	
	@Column(name=KEY_LIB_INSERT_ERROR_TOLERANCE)
	private Double insertErrorTolerance;
	
	@Enumerated(EnumType.STRING)
	private Usage usage;
	
	private Integer order;
	
	@Enumerated(EnumType.STRING)
	private FileType filetype;
	
	@Column(name=KEY_LIB_FILE_1)
	private String filePaired1;
	
	@Column(name=KEY_LIB_FILE_2)
	private String filePaired2;
	
	@Column(name=KEY_LIB_FILE_SE)
	private String seFile;
	
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAverageInsertSize() {
		return averageInsertSize;
	}

	public void setAverageInsertSize(Integer averageInsertSize) {
		this.averageInsertSize = averageInsertSize;
	}

	public Double getInsertErrorTolerance() {
		return insertErrorTolerance;
	}

	public void setInsertErrorTolerance(Double insertErrorTolerance) {
		this.insertErrorTolerance = insertErrorTolerance;
	}

	public Usage getUsage() {
		return usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public FileType getFiletype() {
		return filetype;
	}

	public void setFiletype(FileType filetype) {
		this.filetype = filetype;
	}

	public String getFilePaired1() {
		return filePaired1;
	}

	public void setFilePaired1(String filePaired1) {
		this.filePaired1 = filePaired1;
	}

	public String getFilePaired2() {
		return filePaired2;
	}

	public void setFilePaired2(String filePaired2) {
		this.filePaired2 = filePaired2;
	}

	public String getSeFile() {
		return seFile;
	}

	public void setSeFile(String seFile) {
		this.seFile = seFile;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[SECTION_LIB_PREFIX" + this.getOrder().toString() + "]\n")
		.append(KEY_LIB_NAME + "=" + this.getName() + "\n")
		.append(KEY_LIB_AVG_INSERT_SIZE + "=" + this.getAverageInsertSize().toString() + "\n")
		.append(KEY_LIB_INSERT_ERROR_TOLERANCE + "=" + this.getInsertErrorTolerance().toString() + "\n")
		.append(KEY_LIB_USAGE + "=" + this.getUsage().toString() + "\n")
		.append(KEY_LIB_FILE_TYPE + "=" + this.getFiletype().toString() + "\n")
		.append(KEY_LIB_FILE_1 + "=" + this.getFilePaired1() + "\n")
		.append(KEY_LIB_FILE_2 + "=" + this.getFilePaired2() + "\n")
		.append(KEY_LIB_FILE_SE + "=" + this.getSeFile() + "\n");
		
		return sb.toString();		
	}
	
	public static LibraryDetails parseIniSection(Section iniSection, int index) {
		
		LibraryDetails ld = new LibraryDetails();
		ld.setId(-1L);
		ld.setName(iniSection.get(KEY_LIB_NAME));
		ld.setAverageInsertSize(Integer.parseInt(iniSection.get(KEY_LIB_AVG_INSERT_SIZE)));
		ld.setInsertErrorTolerance(Double.parseDouble(iniSection.get(KEY_LIB_INSERT_ERROR_TOLERANCE)));
		ld.setUsage(Usage.valueOf(iniSection.get(KEY_LIB_USAGE)));
		ld.setOrder(index);
		ld.setFiletype(FileType.valueOf(iniSection.get(KEY_LIB_FILE_TYPE)));
		ld.setFilePaired1(iniSection.get(KEY_LIB_FILE_1));	
		ld.setFilePaired2(iniSection.get(KEY_LIB_FILE_2));
		ld.setSeFile(iniSection.get(KEY_LIB_FILE_SE));
		return ld;
	}
}


