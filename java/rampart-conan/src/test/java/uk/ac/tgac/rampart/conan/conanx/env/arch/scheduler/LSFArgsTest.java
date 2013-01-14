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
package uk.ac.tgac.rampart.conan.conanx.env.arch.scheduler;

import static org.junit.Assert.*;

import org.junit.Test;

public class LSFArgsTest {

	@Test
	public void testDefaultArgs() {
		LSFArgs args = new LSFArgs();
		
		String test = args.toString();

		assertTrue(test.isEmpty());
	}

    @Test
    public void testSimpleArgs() {
        LSFArgs args = new LSFArgs();
        args.setJobName("Job1");
        args.setProjectName("ProjectRampart");
        args.setQueueName("production");

        String test = args.toString();

        String correct = "-J Job1 -q production -P ProjectRampart";

        assertTrue(test.equals(correct));
    }

	
	@Test
	public void testFullArgs() {
		
		LSFArgs args = new LSFArgs();
		args.setJobName("Job1");
		args.setProjectName("ProjectRampart");
		args.setQueueName("production");
		args.setThreads(8);
		args.setOpenmpi(true);
		args.setWaitCondition(new LSFWaitCondition(LSFExitStatusType.ENDED, "Job0"));
		args.setMemoryMB(60000);
		
		String test = args.toString();
		
		String correct = "-J Job1 -q production -n 8 -P ProjectRampart -a openmpi -w ended(Job0) -Rrusage[mem=60000]span[ptile=8]";
		
		assertTrue(test.equals(correct));
	}

}
