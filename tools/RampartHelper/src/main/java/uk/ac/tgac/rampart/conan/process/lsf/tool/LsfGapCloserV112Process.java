package uk.ac.tgac.rampart.conan.process.lsf.tool;

import java.util.Collection;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;

import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.tool.GapCloserV112Params;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.service.ToolLoaderService;

@ServiceProvider
public class LsfGapCloserV112Process extends AbstractRampartLsfProcess {

	private static final long serialVersionUID = -7311765587559938252L;
	
	@Autowired
	private ToolLoaderService toolLoaderService;
	
	private static final String CMD = "GapCloser";
	
	private GapCloserV112Params params;
	
	public LsfGapCloserV112Process(GapCloserV112Params params) {
		this.params = params;
	}
	
	@Override
	protected String getLoadToolCommand() {
		return 	toolLoaderService.getLoadToolCommand(ToolLoaderService.GAPCLOSER_1_12);
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