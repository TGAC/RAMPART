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
package uk.ac.tgac.rampart.tool.process.mass;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorArgs;
import uk.ac.tgac.rampart.tool.process.mass.selector.MassSelectorProcess;
import uk.ac.tgac.rampart.tool.process.mass.selector.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassArgs;
import uk.ac.tgac.rampart.tool.process.mass.single.SingleMassProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
@Service
public class MassExecutorImpl extends RampartExecutorImpl implements MassExecutor {

    private List<Integer> jobIds;

    public MassExecutorImpl() {
        this.jobIds = new ArrayList<>();
    }

    @Override
    public void executeSingleMass(SingleMassArgs singleMassArgs)
            throws InterruptedException, ProcessExecutionException {

        SingleMassProcess singleMassProcess = new SingleMassProcess(singleMassArgs);
        singleMassProcess.setConanProcessService(this.conanProcessService);
        singleMassProcess.execute(this.executionContext);
        this.jobIds = singleMassProcess.getJobIds();
    }

    @Override
    public AssemblyStatsTable compileSingleMassResults(SingleMassArgs singleMassArgs)
            throws IOException {

        SingleMassProcess singleMassProcess = new SingleMassProcess(singleMassArgs);
        return singleMassProcess.compileResults(singleMassArgs);
    }

    @Override
    public List<Integer> getJobIds() {
        return this.jobIds;
    }

    @Override
    public void executeMassSelector(MassSelectorArgs massSelectorArgs)
            throws InterruptedException, ProcessExecutionException {

        MassSelectorProcess massSelectorProcess = new MassSelectorProcess(massSelectorArgs);
        massSelectorProcess.setConanProcessService(this.conanProcessService);
        massSelectorProcess.execute(this.executionContext);
    }
}
