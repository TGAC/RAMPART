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
package uk.ac.tgac.rampart.core.dao;

import java.util.List;

import uk.ac.tgac.rampart.core.data.AssemblyStats;
import uk.ac.tgac.rampart.core.data.ImproverStats;
import uk.ac.tgac.rampart.core.data.MassStats;

public interface AssemblyStatsDao {

	MassStats getMassStats(Long id);
	
	ImproverStats getImproverStats(Long id);
	
	List<AssemblyStats> getAllAssemblyStats();
	
	List<MassStats> getAllMassStats();
	
	List<ImproverStats> getAllImproverStats();
	
	List<MassStats> getMassStatsForJob(Long jobId);
	
	List<ImproverStats> getImproverStatsForJob(Long jobId);
	
	MassStats getBestAssembly(Long jobId);
	
	long count();
	
	void persist(MassStats massStats);
	
	void persist(ImproverStats improverStats);
	
	void persistMassStatsList(List<MassStats> massStatsList);
	
	void persistImproverStatsList(List<ImproverStats> improverStatsList);
}
