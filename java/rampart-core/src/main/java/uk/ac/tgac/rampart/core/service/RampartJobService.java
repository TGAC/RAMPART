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
package uk.ac.tgac.rampart.core.service;

import com.itextpdf.text.DocumentException;
import org.apache.velocity.VelocityContext;
import uk.ac.tgac.rampart.core.data.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface RampartJobService {

	void seperatePlots(File in, File outDir, String filenamePrefix) throws IOException, DocumentException;
	
	RampartConfiguration loadConfiguration(File in) throws IOException;
	
	List<AssemblyStats> getAssemblyStats(File in) throws IOException;

	AssemblyStats getWeightings(File in) throws IOException;
	
	void calcReadStats(List<Library> libs) throws IOException;
	
	VelocityContext buildContext(RampartJobFileStructure jobFS) throws IOException;
	
	RampartSettings determineSettings(RampartJobFileStructure job) throws IOException;
	
	void persistContext(final VelocityContext context);
}
