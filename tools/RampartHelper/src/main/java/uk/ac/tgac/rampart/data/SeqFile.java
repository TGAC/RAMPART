package uk.ac.tgac.rampart.data;

import java.io.File;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema="rampart",name="seq_file")
public class SeqFile implements Serializable {

	private static final long serialVersionUID = -3958558031764287299L;

	public enum FileType {
		FASTA,
		FASTQ,
		UNKNOWN;
		
		public static FileType determineExtension(File file) {
			
			String filename = file.getName();			
			String extension = filename.substring(filename.lastIndexOf('.') + 1);
			return determineExtension(extension);
		}
		
		public static FileType determineExtension(String extension) {
			if (	extension.equalsIgnoreCase("fq") || 
					extension.equalsIgnoreCase("fastq")) {
				return FileType.FASTQ;
			}
			else if (	extension.equalsIgnoreCase("fa") ||
						extension.equalsIgnoreCase("fasta")) {
				return FileType.FASTA;
			}
			else {
				return FileType.UNKNOWN; 
			}
		}
	}
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column(name="file_path")
	private String filePath;
	
	@Enumerated(EnumType.STRING)
	@Column(name="file_type")
	private FileType fileType;
	
	public SeqFile() {
		this(null, FileType.UNKNOWN);
	}
	
	public SeqFile(String filePath) {
		this(new File(filePath), FileType.determineExtension(new File(filePath)));
	}
	
	public SeqFile(File file) {
		this(file, FileType.determineExtension(file));
	}
	
	public SeqFile(File filePath, FileType fileType) {
		this.filePath = filePath.getPath();
		this.fileType = fileType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public File getFile() {
		return new File(filePath);
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
}
