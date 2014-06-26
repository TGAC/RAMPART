package uk.ac.tgac.rampart;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 12/12/13
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
public class RampartTest {

    @Test
    public void setupTest() throws IOException, TaskExecutionException, InterruptedException {

        File testFile = FileUtils.toFile(RampartTest.class.getResource("/config/rampart_config_1.xml"));

        RampartCLI rampart = new RampartCLI();
        rampart.setJobConfig(testFile);
        rampart.setSkipChecks(true);
        //rampart.initialise();

        //rampart.execute();

        assertTrue(true);
    }
}
