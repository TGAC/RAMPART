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
package uk.ac.tgac.rampart.conan.tool.module.amp;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class AmpPipeline implements ConanPipeline {

    private List<AmpTask> tasks;


    public List<AmpTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<AmpTask> tasks) {
        this.tasks = tasks;
    }

    public void addTasks(String[] tasks) {
        for(String task : tasks) {
            if (task.contains(" ")) {
                String[] parts = task.split(" ");
                String taskType = parts[0];
                String taskName = parts[1];

                this.tasks.add(AmpFactory.createAmpTask(taskType, taskName));
            }
            else {
                this.tasks.add(AmpFactory.createAmpTask(task));
            }
        }
    }


    @Override
    public String getName() {
        return "Assembly iMProver (AMP)";
    }

    @Override
    public ConanUser getCreator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPrivate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDaemonized() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ConanProcess> getProcesses() {

        // Not sure why this is necessary... should be polymorphic
        List<ConanProcess> processes = new ArrayList<ConanProcess>();

        for(AmpTask task : this.tasks) {
            processes.add(task);
        }

        return processes;
    }

    @Override
    public List<ConanParameter> getAllRequiredParameters() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
