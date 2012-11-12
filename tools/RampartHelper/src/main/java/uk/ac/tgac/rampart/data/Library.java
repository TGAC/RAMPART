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

import org.ini4j.Profile.Section;

@Entity
@Table(schema="rampart",name="library")
public class Library implements Serializable {
	
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
	
	public enum Dataset {
		RAW,
		QT
	}
	
	
	public static final String SECTION_PREFIX = "LIB";
	
	public static final String KEY_NAME = "name";
	public static final String KEY_DATASET = "dataset";
	
	public static final String KEY_AVG_INSERT_SIZE = "avg_insert_size";
	public static final String KEY_INSERT_ERROR_TOLERANCE = "insert_err_tolerance";
	public static final String KEY_READ_LENGTH = "read_length";
	public static final String KEY_USAGE = "usage";
	public static final String KEY_ORDER = "order";
	
	public static final String KEY_FILE_TYPE = "file_type";
	public static final String KEY_FILE_1 = "file_paired_1";
	public static final String KEY_FILE_2 = "file_paired_2";
	public static final String KEY_FILE_SE = "file_single_end";
	
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	@Enumerated(EnumType.STRING)
	private Dataset dataset;
	
	@Column(name=KEY_AVG_INSERT_SIZE)
	private Integer averageInsertSize;
	
	@Column(name=KEY_INSERT_ERROR_TOLERANCE)
	private Double insertErrorTolerance;
	
	@Column(name=KEY_READ_LENGTH)
	private Integer readLength;
	
	@Enumerated(EnumType.STRING)
	private Usage usage;
	
	private Integer order;
	
	@Enumerated(EnumType.STRING)
	@Column(name=KEY_FILE_TYPE)
	private FileType fileType;
	
	@Column(name=KEY_FILE_1)
	private String filePaired1;
	
	@Column(name=KEY_FILE_2)
	private String filePaired2;
	
	@Column(name=KEY_FILE_SE)
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
	
	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
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
	
	public Integer getReadLength() {
		return readLength;
	}

	public void setReadLength(Integer readLength) {
		this.readLength = readLength;
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
		return fileType;
	}

	public void setFiletype(FileType filetype) {
		this.fileType = filetype;
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
		.append(KEY_NAME + "=" + this.getName() + "\n")
		.append(KEY_DATASET + "=" + this.getDataset().toString() + "\n")
		.append(KEY_AVG_INSERT_SIZE + "=" + this.getAverageInsertSize().toString() + "\n")
		.append(KEY_INSERT_ERROR_TOLERANCE + "=" + this.getInsertErrorTolerance().toString() + "\n")
		.append(KEY_READ_LENGTH + "=" + this.getReadLength() + "\n")
		.append(KEY_USAGE + "=" + this.getUsage().toString() + "\n")
		.append(KEY_FILE_TYPE + "=" + this.getFiletype().toString() + "\n")
		.append(KEY_FILE_1 + "=" + this.getFilePaired1() + "\n")
		.append(KEY_FILE_2 + "=" + this.getFilePaired2() + "\n")
		.append(KEY_FILE_SE + "=" + this.getSeFile() + "\n");
		
		return sb.toString();
	}
	
	public static Library parseIniSection(Section iniSection, int index) {
		
		Library ld = new Library();
		ld.setId(-1L);
		ld.setName(iniSection.get(KEY_NAME));
		ld.setDataset(Dataset.valueOf(iniSection.get(KEY_DATASET)));
		ld.setAverageInsertSize(Integer.parseInt(iniSection.get(KEY_AVG_INSERT_SIZE)));
		ld.setInsertErrorTolerance(Double.parseDouble(iniSection.get(KEY_INSERT_ERROR_TOLERANCE)));
		ld.setReadLength(Integer.parseInt(iniSection.get(KEY_READ_LENGTH)));
		ld.setUsage(Usage.valueOf(iniSection.get(KEY_USAGE)));
		ld.setOrder(index);
		ld.setFiletype(FileType.valueOf(iniSection.get(KEY_FILE_TYPE)));
		ld.setFilePaired1(iniSection.get(KEY_FILE_1));	
		ld.setFilePaired2(iniSection.get(KEY_FILE_2));
		ld.setSeFile(iniSection.get(KEY_FILE_SE));
		return ld;
	}
}


