package uk.ac.tgac.rampart.tool.process.amp;

import org.junit.Test;
import uk.ac.tgac.conan.process.asmIO.AbstractAssemblyIOProcess;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/10/13
 * Time: 11:57
 * To change this template use File | Settings | File Templates.
 */
public class AmpStageProcessTest {

    @Test
    public void testMakeStage() throws IOException {

        AmpStageArgs args = new AmpStageArgs();
        args.setTool("SSPACE_Basic_V2.0");

        AbstractAssemblyIOProcess process = new AmpStageProcess(args).makeStage(args);

        assertTrue(process != null);
    }
}
