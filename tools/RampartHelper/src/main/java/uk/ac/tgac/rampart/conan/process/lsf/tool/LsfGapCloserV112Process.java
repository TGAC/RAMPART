package uk.ac.tgac.rampart.conan.process.lsf.tool;

import java.util.Collection;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.tool.GapCloserV112Params;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

@ServiceProvider
public class LsfGapCloserV112Process extends AbstractRampartLsfProcess {

	private static final long serialVersionUID = -7311765587559938252L;
	
	private static final String CMD = "GapCloser";
	
	private GapCloserV112Params params;
	
	public LsfGapCloserV112Process(GapCloserV112Params params) {
		this.params = params;
	}
	
	@Override
	protected String getLoadToolCommand() {
		return ToolCommandLoader.getInstance().getLoadToolCommand(ToolCommandLoader.GAPCLOSER_1_12);
	}

	@Override
	protected String getComponentName() {
		return "Degap";
	}

	@Override
	protected String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		return CMD + " " + parameters.toString();
	}

	@Override
	public String getName() {
		return "SOAP GapCloser v1.12 (LSF)";
	}

	@Override
	public Collection<ConanParameter> getParameters() {
		return this.params.getParameters();
	}
}