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

import java.io.File;
import java.io.IOException;

import uk.ac.tgac.rampart.core.data.AssemblyStats;
import uk.ac.tgac.rampart.core.data.SeqFile;

public interface SequenceStatisticsService {

    /**
     * Calculates and sequence file statistics for the provided sequence file.  Also links the
     * created sequence file statistics object to the provided sequence file for persistence
     * purposes
     * @param in The sequence file to analyseReads
     * @return The statistics related to the sequence file, linked to the sequence file for
     * persistence
     * @throws IOException
     */
	void analyseReads(SeqFile in) throws IOException;

    /**
     * Processes a FastA assembly file and produces size and composition metrics
     * @param in The FastA sequence file to analyse
     * @return The assembly statistics for the FastA file.
     * @throws IOException
     */
    AssemblyStats analyseAssembly(File in) throws IOException;
}
