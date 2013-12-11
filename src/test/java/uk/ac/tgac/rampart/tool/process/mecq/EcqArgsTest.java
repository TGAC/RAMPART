package uk.ac.tgac.rampart.tool.process.mecq;

import org.junit.Test;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.AbstractErrorCorrector;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 15/10/13
 * Time: 10:00
 * To change this template use File | Settings | File Templates.
 */
public class EcqArgsTest {

    @Test
    public void testGetOutputFiles() {

        EcqArgs args = new EcqArgs();

        args.setTool("SICKLE_V1.1");
        args.setOutputDir(new File("test/"));

        Library lib = new Library();
        lib.setFiles(new File("file1.fl"), new File("file2.fl"));

        AbstractErrorCorrector ec = new MecqProcess().makeErrorCorrector(args, lib, new File("."));

        List<File> files = args.getOutputFiles(ec);

        assertTrue(files.size() == 3);
    }
}
