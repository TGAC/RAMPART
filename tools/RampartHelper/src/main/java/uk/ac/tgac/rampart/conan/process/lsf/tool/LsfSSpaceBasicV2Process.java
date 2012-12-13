package uk.ac.tgac.rampart.conan.process.lsf.tool;

import java.util.Collection;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;

import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.tool.SSpaceBasicV2Params;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.service.ToolLoaderService;

@ServiceProvider
public class LsfSSpaceBasicV2Process extends AbstractRampartLsfProcess {

	private static final long serialVersionUID = -7311765587559938252L;
	
	@Autowired
	private ToolLoaderService toolLoaderService;
	
	private static final String CMD = "SSPACE_Basic_v2.0.pl";
	
	private SSpaceBasicV2Params params;
	

	public LsfSSpaceBasicV2Process(SSpaceBasicV2Params params) {
		this.params = params;
	}
	
	@Override
	protected String getLoadToolCommand() {
		return 	toolLoaderService.getLoadToolCommand(ToolLoaderService.PERL_5_16_1) + "; " +
				toolLoaderService.getLoadToolCommand(ToolLoaderService.SSPACE_BASIC_2_0);
	}

	@Override
	protected String getComponentName() {
		return "Scaffolder";
	}

	@Override
	protected String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		return CMD + " " + parameters.toString();
	}

	@Override
	public String getName() {
		return "SSPACE Basic v2.0 (LSF)";
	}

	@Override
	public Collection<ConanParameter> getParameters() {
		return this.params.getParameters();
	}
}