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
package uk.ac.tgac.rampart.conan.service.impl;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;
import uk.ac.tgac.rampart.conan.conanx.env.arch.WaitCondition;
import uk.ac.tgac.rampart.conan.conanx.process.ExtendedConanProcess;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;

import java.net.ConnectException;

@Service
public class DefaultProcessExecutionService implements ProcessExecutionService {

	@Override
    public void execute(ExtendedConanProcess process, Environment env)
            throws InterruptedException, ProcessExecutionException, ConnectException {


    }

    @Override
    public void execute(String command, Environment env) throws InterruptedException, ProcessExecutionException, ConnectException {
        env.submitCommand(command);
    }

    @Override
    public int waitFor(WaitCondition waitCondition, EnvironmentArgs args) throws InterruptedException, ProcessExecutionException, ConnectException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
