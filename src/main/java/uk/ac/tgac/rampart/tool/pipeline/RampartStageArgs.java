package uk.ac.tgac.rampart.tool.pipeline;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 09/12/13
 * Time: 11:37
 * To change this template use File | Settings | File Templates.
 */
public interface RampartStageArgs extends ProcessArgs {

    public List<ConanProcess> getExternalProcesses();
}
