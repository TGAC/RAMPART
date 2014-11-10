package uk.ac.tgac.rampart;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
import uk.ac.tgac.rampart.stage.RampartStageList;

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
public class RampartConfigTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void testConfigLoad() throws IOException {

        File cfgFile = FileUtils.toFile(this.getClass().getResource("/config/test_rampart_config.xml"));
        File outDir = temp.newFolder("configTest");
        String jobPrefix = "configTestJob";

        RampartConfig args = new RampartConfig(cfgFile, outDir, jobPrefix, RampartStageList.parse("ALL"), null, null, true);

        Assert.assertTrue(true);
    }

    @Test
    public void setupTest() throws IOException, TaskExecutionException, InterruptedException {

        File testFile = FileUtils.toFile(RampartConfigTest.class.getResource("/config/rampart_config_1.xml"));

        /*RampartCLI rampart = new RampartCLI();
        rampart.setJobConfig(testFile);
        rampart.setSkipChecks(true);

        rampart.initialise();

        String kr0 = rampart.getArgs().getMassArgs().getMassJobArgList().get(0).getKmerRange().toString();   */



        //rampart.execute();

        assertTrue(true);
    }
}
