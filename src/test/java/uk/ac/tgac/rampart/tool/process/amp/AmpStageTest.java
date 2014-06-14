package uk.ac.tgac.rampart.tool.process.amp;

import org.junit.Test;
import uk.ac.tgac.conan.process.asmIO.AssemblyEnhancer;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/10/13
 * Time: 11:57
 * To change this template use File | Settings | File Templates.
 */
public class AmpStageTest {

    @Test
    public void testMakeStage() throws IOException {

        AmpStage.Args args = new AmpStage.Args();
        args.setTool("SSPACE_Basic_V2.0");

        AssemblyEnhancer process = new AmpStage(args).makeStage(args, null);

        assertTrue(process != null);
    }
}
