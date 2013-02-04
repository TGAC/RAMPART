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
package uk.ac.tgac.rampart.conan.tool.module.qt;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.DefaultEnvironment;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.service.impl.DefaultProcessExecutionService;
import uk.ac.tgac.rampart.conan.tool.module.RampartProcess;
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmer;
import uk.ac.tgac.rampart.conan.tool.process.qt.QualityTrimmerFactory;
import uk.ac.tgac.rampart.core.data.Library;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
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


    private QTArgs args;

    public QTProcess() {
        this(new QTArgs()) ;
    }

    public QTProcess(QTArgs args) {
        this.args = args;
    }

    @Override
    public void execute(Environment env) throws ProcessExecutionException, InterruptedException, IOException, CommandExecutionException {

        String qtType = this.args.getConfig().getQTTool();
        List<Library> libs = this.args.getConfig().getLibs();

        for(Library lib : libs) {

            QualityTrimmer qt = QualityTrimmerFactory.create(qtType, lib);

            this.processExecutionService.execute(qt, env);
        }

        //TODO Wait for all quality trimmers to complete

    }


    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {

        this.args.setFromArgMap(parameters);

        Environment env = new DefaultEnvironment();

        try {
            this.execute(env);
        } catch (IOException e) {
            throw new ProcessExecutionException(-1, e);
        } catch (CommandExecutionException e) {
            throw new ProcessExecutionException(-1, e);
        }

        return true;
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
