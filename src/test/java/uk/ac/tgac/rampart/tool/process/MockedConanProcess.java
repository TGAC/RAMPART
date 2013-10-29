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
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;

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
    protected ConanProcessService conanProcessService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


}
