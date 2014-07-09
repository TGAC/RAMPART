package uk.ac.tgac.rampart.stage;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.stage.Finalise;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 20/11/13
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public class FinaliseTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void test1() throws InterruptedException, ProcessExecutionException, IOException {

        File outputDir = temp.newFolder("test1");

        File test1File = FileUtils.toFile(this.getClass().getResource("/tools/finalise/test1.fa"));

        Finalise.Args args = new Finalise.Args();
        args.setInputFile(test1File);
        args.setOutputDir(outputDir);
        args.setMinN(5);
        args.setOutputPrefix("TGAC_TS_V1");
        args.setCompress(false);

        Finalise process = new Finalise(null, args);

        boolean result = process.execute(new DefaultExecutionContext());

        assertTrue(result);
        //assertTrue(new File(outputDir, "TGAC_TS_V1.tar.gz").exists());
    }
}
