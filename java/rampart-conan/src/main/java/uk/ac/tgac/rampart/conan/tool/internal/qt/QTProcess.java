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
package uk.ac.tgac.rampart.conan.tool.internal.qt;

import com.jcraft.jsch.JSchException;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.service.impl.DefaultProcessExecutionService;
import uk.ac.tgac.rampart.conan.tool.external.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.external.qt.QualityTrimmerArgs;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SicklePeV11Args;
import uk.ac.tgac.rampart.conan.tool.external.qt.sickle.SickleV11Process;
import uk.ac.tgac.rampart.conan.tool.internal.RampartProcess;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 07/01/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class QTProcess implements ConanProcess, RampartProcess {

    protected ProcessExecutionService processExecutionService = new DefaultProcessExecutionService();


    private QTArgs qt;

    public QTProcess() {
        this(new QTArgs()) ;
    }

    public QTProcess(QTArgs qt) {
        this.qt = qt;
    }

    @Override
    public void execute(Environment env) throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException {

        this.processExecutionService.execute(this.qt.getQualityTrimmer(), env);
    }


    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        return false;
    }

    @Override
    public String getName() {
        return "QT";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new QTParams().getConanParameters();
    }



}
