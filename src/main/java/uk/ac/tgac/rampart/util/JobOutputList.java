/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
public class JobOutputList extends ArrayList<JobOutput> {

    public List<Integer> getJobIds() {

        List<Integer> jobIds = new ArrayList<>();
        for(JobOutput jo : this) {
            jobIds.add(jo.getJobId());
        }

        return jobIds;
    }

    public List<File> getFiles() {

        List<File> jobIds = new ArrayList<>();
        for(JobOutput jo : this) {
            jobIds.add(jo.getOutputFile());
        }

        return jobIds;
    }
}
