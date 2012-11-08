package uk.ac.tgac.rampart.data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(schema="rampart",name="seqop")
public class SequenceOperation {

	@Id
	private Long id;
	
	private Long jiraSeqopId;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="seqdev_id")
	private SequenceDevice seqDevice;
	
	private String libraryId;
	private Integer insertSize;
	private Integer readLength;
	private String barcode;
	
	
	public Long getJiraSeqopId() {
		return jiraSeqopId;
	}

	public void setJiraSeqopId(Long jiraSeqopId) {
		this.jiraSeqopId = jiraSeqopId;
	}

	public SequenceDevice getSeqDevice() {
		return seqDevice;
	}

	public void setSeqDevice(SequenceDevice seqDevice) {
		this.seqDevice = seqDevice;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(String libraryId) {
		this.libraryId = libraryId;
	}

	public Integer getInsertSize() {
		return insertSize;
	}

	public void setInsertSize(Integer insertSize) {
		this.insertSize = insertSize;
	}

	public Integer getReadLength() {
		return readLength;
	}

	public void setReadLength(Integer readLength) {
		this.readLength = readLength;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
	

}
