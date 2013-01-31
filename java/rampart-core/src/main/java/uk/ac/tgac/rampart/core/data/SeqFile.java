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

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;

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
	
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="library_id")
	private Library library;
	
	@Column(name="file_path")
	private String filePath;
	
	@Enumerated(EnumType.STRING)
	@Column(name="file_type")
	private FileType fileType;
	
	@Column(name = "base_count")
	private Long baseCount;

	@Column(name = "seq_count")
	private Long seqCount;

	@Column(name = "a_count")
	private Long aCount;

	@Column(name = "c_count")
	private Long cCount;

	@Column(name = "g_count")
	private Long gCount;

	@Column(name = "t_count")
	private Long tCount;

	@Column(name = "n_count")
	private Long nCount;
	
	public SeqFile() {
		this(null, FileType.UNKNOWN);
	}
	
	public SeqFile(String filePath) {
		this(new File(filePath), FileType.determineExtension(new File(filePath)));
	}
	
	public SeqFile(File file) {
		this(file, file != null ? FileType.determineExtension(file) : FileType.UNKNOWN);
	}
	
	public SeqFile(File filePath, FileType fileType) {
		this(filePath, fileType, 0,0,0,0,0,0,0);
	}
	
	public SeqFile(File file, 
			long baseCount, long seqCount, 
			long aCount, long cCount, long gCount, long tCount, long nCount) {
		this(file, file != null ? FileType.determineExtension(file) : FileType.UNKNOWN, 
				baseCount,seqCount,
				aCount,cCount,gCount,tCount,nCount);
	}
	
	public SeqFile(File file, FileType fileType, 
			long baseCount, long seqCount, 
			long aCount, long cCount, long gCount, long tCount, long nCount) {
		this.filePath = file != null ? file.getPath() : null;
		this.fileType = fileType;
		this.baseCount = baseCount;
		this.seqCount = seqCount;
		this.aCount = aCount;
		this.cCount = cCount;
		this.gCount = gCount;
		this.tCount = tCount;
		this.nCount = nCount;
	}
	
	public void addSequencePart(String part) {
		long length = part.length();
		this.baseCount += length;

		for (int i = 0; i < length; i++) {

			char c = part.charAt(i);

			switch (c) {
			case 'A':
				this.aCount++;
				break;
			case 'C':
				this.cCount++;
				break;
			case 'G':
				this.gCount++;
				break;
			case 'T':
				this.tCount++;
				break;
			case 'N':
				this.nCount++;
				break;
			}
		}
	}

	public void addFullSequence(String seq) {

		addSequencePart(seq);
		this.seqCount++;
	}
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
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
	


	public Long getBaseCount() {
		return baseCount;
	}
	
	public void setBaseCount(Long baseCount) {
		this.baseCount = baseCount;
	}

	public Long getSeqCount() {
		return seqCount;
	}
	
	public void setSeqCount(Long seqCount) {
		this.seqCount = seqCount;
	}



	public Long getACount() {
		return aCount;
	}
	
	public void setACount(Long aCount) {
		this.aCount = aCount;
	}

	public Long getCCount() {
		return cCount;
	}
	
	public void setCCount(Long cCount) {
		this.cCount = cCount;
	}

	public Long getGCount() {
		return gCount;
	}
	
	public void setGCount(Long gCount) {
		this.gCount = gCount;
	}

	public Long getTCount() {
		return tCount;
	}
	
	public void setTCount(Long tCount) {
		this.tCount = tCount;
	}

	public Long getNCount() {
		return nCount;
	}
	
	public void setNCount(Long nCount) {
		this.nCount = nCount;
	}

	
	
	public void incSeqCount() {
		this.seqCount++;
	}

	public long getATCount() {
		long atCount = this.getACount() + this.getTCount();
		return atCount;
	}

	public long getGCCount() {
		long gcCount = this.getGCount() + this.getCCount();
		return gcCount;
	}

	public long getATGCCount() {
		long atgcCount = this.getGCCount() + this.getATCount();
		return atgcCount;
	}

	public double getGCContent() {
		double gcContent = ((double) this.getGCCount() / (double) this.getATGCCount()) * 100.0;
		return gcContent;
	}

	public double getATGCRatio() {
		double atgcRatio = (double) this.getATCount() / (double) this.getGCCount();
		return atgcRatio;
	}

	public double getNRatio() {
		double nRatio = (double) this.getNCount() / (double) this.getBaseCount();
		return nRatio;
	}

	public double getCoverage(long genomeSize) {
		double coverage = this.getBaseCount() / genomeSize;
		return coverage;
	}

	public double getAverageReadLength() {
		double avgRdLen = (double) this.getBaseCount() / (double) this.getSeqCount();
		return avgRdLen;
	}
	
	

	@Override
	public String toString() {
		return "Sequence Statistics for file: " + filePath + "\n" + "Base Count : "
				+ baseCount + "\n" + "Sequence Count: " + seqCount + "\n" + "A Count: " + aCount + "\n" + "C Count: "
				+ cCount + "\n" + "G Count: " + gCount + "\n" + "T Count: " + tCount + "\n" + "N Count: " + nCount
				+ "\n" + "AT Count: " + getATCount() + "\n" + "GC Count: " + getGCCount() + "\n" + "ATGC Count: "
				+ getATGCCount() + "\n" + "GC%: " + getGCContent() + "\n" + "ATGC Ratio: " + getATGCRatio() + "\n"
				+ "N Ratio: " + getNRatio() + "\n";
	}
}
