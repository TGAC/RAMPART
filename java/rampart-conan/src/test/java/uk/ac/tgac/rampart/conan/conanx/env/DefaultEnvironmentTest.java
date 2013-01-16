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
package uk.ac.tgac.rampart.conan.conanx.env;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.tgac.rampart.conan.conanx.env.arch.Architecture;
import uk.ac.tgac.rampart.conan.conanx.env.arch.Single;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Local;
import uk.ac.tgac.rampart.conan.conanx.env.locality.Locality;

import java.io.File;

/**
 * User: maplesod
 * Date: 08/01/13
 * Time: 14:12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class DefaultEnvironmentTest {

    private static Logger log = LoggerFactory.getLogger(DefaultEnvironmentTest.class);

    @Test
    public void testSubmitCommand() throws Exception {

        EnvironmentArgs envArgs = new DefaultEnvironmentArgs();
        envArgs.setCmdLineOutputFile(new File("~/test/rampart-conan/output.log"));

        Environment env = new DefaultEnvironment(new Local(), new Single(), envArgs);

        env.submitCommand("ls . > ~/rampart_output.log");

        log.info("Finished");
    }
}
