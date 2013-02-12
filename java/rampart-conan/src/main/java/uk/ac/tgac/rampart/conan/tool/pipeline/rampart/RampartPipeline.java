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
package uk.ac.tgac.rampart.conan.tool.pipeline.rampart;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.tgac.rampart.conan.tool.pipeline.amp.AmpPipeline;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.multi.MultiMassParams;
import uk.ac.tgac.rampart.conan.tool.proc.internal.mass.multi.MultiMassProcess;
import uk.ac.tgac.rampart.conan.tool.proc.internal.qt.QTParams;
import uk.ac.tgac.rampart.conan.tool.proc.internal.qt.QTProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class RampartPipeline implements ConanPipeline {

    @Override
    public String getName() {
        return "RAMPART";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ConanUser getCreator() {
        return null;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isDaemonized() {
        return false;
    }

    @Override
    public List<ConanProcess> getProcesses() {

        List<ConanProcess> list = new ArrayList<ConanProcess>();

        list.add(new QTProcess());
        list.add(new MultiMassProcess());
        list.addAll(new AmpPipeline().getProcesses());

        return list;
    }

    @Override
    public List<ConanParameter> getAllRequiredParameters() {
        List<ConanParameter> params = new ArrayList<ConanParameter>();

        params.addAll(new QTParams().getConanParameters());
        params.addAll(new MultiMassParams().getConanParameters());
        params.addAll(new AmpPipeline().getAllRequiredParameters());
        //params.addAll(new AnalysisParams().getConanParameters());
        //params.addAll(new ReportProcess().getConanParameters());

        return params;
    }
}
