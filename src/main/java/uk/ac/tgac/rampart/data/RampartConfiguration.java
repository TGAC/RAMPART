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
package uk.ac.tgac.rampart.data;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import uk.ac.tgac.conan.core.data.Job;
import uk.ac.tgac.conan.core.data.Library;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RampartConfiguration implements Serializable {

	public static final String SECTION_JOB_DETAILS = "JOB";
	public static final String SECTION_LIB_PREFIX = "LIB";
    public static final String SECTION_MECQ = "MECQ";
    public static final String SECTION_MASS = "MASS";
    public static final String SECTION_AMP = "AMP";

    private Job job;
	private List<Library> libs;
    private Section mecqSettings;
    private Section massSettings;
    private Section ampSettings;

    private File file;
	
	public RampartConfiguration() {
        this.job = new Job();
        this.libs = new ArrayList<Library>();
        this.mecqSettings = null;
        this.massSettings = null;
        this.ampSettings = null;

        this.file = null;
    }

    protected String createToolSection(String sectionHeader, Section tool) {

        if (tool == null)
            return "";

        StringBuilder sb = new StringBuilder();

        sb.append("[").append(sectionHeader).append("]\n");
        for(Map.Entry<String, String> entry : tool.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Loads a rampart configuration file from disk and stores the contents in this object.
     * @param configFile The rampart configuration file to load
     * @throws IOException Thrown if there was any problems loading the file.
     */
    public void load(File configFile) throws IOException {
        Ini ini = new Ini(configFile);

        this.file = configFile;

        this.setJob(Job.parseIniSection(ini.get(SECTION_JOB_DETAILS)));

        List<Library> libs = new ArrayList<Library>();

        for(Map.Entry<String,Section> e : ini.entrySet()) {
            if (e.getKey().startsWith(SECTION_LIB_PREFIX)) {
                int index = Integer.parseInt(e.getKey().substring(SECTION_LIB_PREFIX.length()));
                libs.add(Library.parseIniSection(e.getValue(), index));
            }
        }

        this.setLibs(libs);

        this.setMecqSettings(ini.get(SECTION_MECQ));
        this.setMassSettings(ini.get(SECTION_MASS));
        this.setAmpSettings(ini.get(SECTION_AMP));
    }

    /**
     * Saves this object to disk at the specified location
     * @param configFile The location to save this config file.
     * @throws IOException Thrown if there were any problems saving to disk
     */
	public void save(File configFile) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(this.job.toString());
		for(Library ld : this.libs) {
			sb.append(ld.toString());
		}

        sb.append(this.createToolSection(SECTION_MECQ, this.mecqSettings));
        sb.append(this.createToolSection(SECTION_MASS, this.massSettings));
        sb.append(this.createToolSection(SECTION_AMP, this.ampSettings));

		FileUtils.writeStringToFile(configFile, sb.toString());
	}

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public List<Library> getLibs() {
        return libs;
    }

    public void setLibs(List<Library> libs) {
        this.libs = libs;
    }

    public Section getMecqSettings() {
        return mecqSettings;
    }

    public void setMecqSettings(Section mecqSettings) {
        this.mecqSettings = mecqSettings;
    }

    public Section getMassSettings() {
        return massSettings;
    }

    public void setMassSettings(Section massSettings) {
        this.massSettings = massSettings;
    }

    public Section getAmpSettings() {
        return ampSettings;
    }

    public void setAmpSettings(Section ampSettings) {
        this.ampSettings = ampSettings;
    }

    public File getFile() {
        return file;
    }

    public static List<RampartConfiguration> parseList(String list, boolean load) throws IOException {

        List<RampartConfiguration> configs = new ArrayList<RampartConfiguration>();

        String[] parts = list.split(",");

        for(String part : parts) {
            if (load){
                configs.add(RampartConfiguration.loadFile(new File(part.trim())));
            }
            else {
                configs.add(new RampartConfiguration());
            }
        }

        return configs;
    }

    public static List<RampartConfiguration> createList(List<File> configFiles, boolean load) throws IOException {

        List<RampartConfiguration> configs = new ArrayList<RampartConfiguration>();

        for(File configFile : configFiles) {
            if (load){
                configs.add(RampartConfiguration.loadFile(configFile));
            }
            else {
                configs.add(new RampartConfiguration());
            }
        }

        return configs;
    }

    public static RampartConfiguration loadFile(File file) throws IOException {
        RampartConfiguration config = new RampartConfiguration();
        config.load(file);
        return config;
    }

}
