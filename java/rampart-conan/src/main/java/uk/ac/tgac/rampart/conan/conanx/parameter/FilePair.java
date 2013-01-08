package uk.ac.tgac.rampart.conan.conanx.parameter;

import java.io.File;

public class FilePair {

	private File file1;
	private File file2;
	
	public FilePair() {
		this(null, null);
	}
	
	public FilePair(File file1, File file2) {
		this.file1 = file1;
		this.file2 = file2;
	}
	
	public File getFile1() {
		return file1;
	}
	public void setFile1(File file1) {
		this.file1 = file1;
	}
	public File getFile2() {
		return file2;
	}
	public void setFile2(File file2) {
		this.file2 = file2;
	}
	
	public String toString() {
		return this.file1.getPath() + " " + this.file2.getPath();
	}
	
}
