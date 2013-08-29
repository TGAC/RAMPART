package uk.ac.tgac.rampart.tool.process.mass.single;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 29/08/13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class SingleMassExecutorImplTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void testSingleMassGetBases() throws InterruptedException, ProcessExecutionException, IOException {

        File outputDir = temp.newFolder("testGetBases");

        File fastqFile = FileUtils.toFile(this.getClass().getResource("/tools/mass/LIB1896_R1.r95.fastq"));

        SingleMassExecutor sme = new SingleMassExecutorImpl();
        sme.initialise(new DefaultProcessService(), new DefaultExecutionContext());

        long result = sme.getNbBases(fastqFile, outputDir, "test");

        assertTrue(result == 3501624);
    }

    @Test
    public void testSingleMassGetEntries() throws InterruptedException, ProcessExecutionException, IOException {

        File outputDir = temp.newFolder("testGetBases");

        File fastqFile = FileUtils.toFile(this.getClass().getResource("/tools/mass/LIB1896_R1.r95.fastq"));

        SingleMassExecutor sme = new SingleMassExecutorImpl();
        sme.initialise(new DefaultProcessService(), new DefaultExecutionContext());

        long result = sme.getNbEntries(fastqFile, outputDir, "test");

        assertTrue(result == 23037);
    }
}
