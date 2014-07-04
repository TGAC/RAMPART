/*
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
 */

package uk.ac.tgac.rampart.tool.process;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;

import java.io.File;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 29/10/13
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */
public class MockedConanProcess {

    @Mock
    protected ExecutionContext ec;

    @Mock
    protected ConanExecutorService conanExecutorService;

    @Mock
    protected ConanProcessService conanProcessService;

    public MockedConanProcess() {
        MockitoAnnotations.initMocks(this);

        when(conanExecutorService.getConanProcessService()).thenReturn(conanProcessService);
        when(conanExecutorService.getExecutionContext()).thenReturn(ec);
        when(ec.getLocality()).thenReturn(new Local());
        when(ec.usingScheduler()).thenReturn(false);
        when(ec.copy()).thenReturn(ec);
        doNothing().when(ec).setContext(anyString(), anyBoolean(), (File) anyObject());
    }

    @Before
    public void setUp() throws Exception {

    }


}
