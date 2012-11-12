package uk.ac.tgac.rampart.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

public class RampartConfiguration {

	public static final String SECTION_JOB_DETAILS = "JOB";
	public static final String SECTION_LIB_PREFIX = "LIB";
	
	
	private Job jobDetails;
	private List<Library> libDetails;
	
	public RampartConfiguration(File config) throws InvalidFileFormatException, IOException {
		loadFile(config);
	}
	
	public void loadFile(File config) throws InvalidFileFormatException, IOException {
		Ini ini = new Ini(config);
		
		this.jobDetails = Job.parseIniSection(ini.get(SECTION_JOB_DETAILS));
		
		this.libDetails = new ArrayList<Library>();
		for(Map.Entry<String,Section> e : ini.entrySet()) {
			if (e.getKey().startsWith(SECTION_LIB_PREFIX)) {
				int index = Integer.parseInt(e.getKey().substring(SECTION_LIB_PREFIX.length()));
				Library ld = Library.parseIniSection(e.getValue(), index);
				this.libDetails.add(ld);
			}
		}
		
	}
	
	public void saveFile(File outFile) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(this.jobDetails.toString());
		for(Library ld : this.libDetails) {
			sb.append(ld.toString());
		}
		
		
		FileUtils.writeStringToFile(outFile, sb.toString());
	}
	
}
