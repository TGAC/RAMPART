package uk.ac.tgac.rampart.conan.tool.sspace;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

@ServiceProvider
public class SSpaceBasicV2LsfProcess extends AbstractRampartLsfProcess {
	
	private static final long serialVersionUID = -9137766834408006360L;
	
	public static final String NAME = "SSPACE_Basic_v2.0-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.SSPACE_BASIC_2_0;
	public static final String COMMAND = "SSPACE_Basic_v2.0.pl";
	public static final String PARAM_PREFIX = "-";
	
	public SSpaceBasicV2LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				SSpaceBasicV2Param.values());
	}	
	
	@Override
	public String getLoadToolCommand() {
		return 	ToolCommandLoader.getInstance().getLoadToolCommand(ToolCommandLoader.PERL_5_16_1) + "; " +
				ToolCommandLoader.getInstance().getLoadToolCommand(ToolCommandLoader.SSPACE_BASIC_2_0);
	}

}