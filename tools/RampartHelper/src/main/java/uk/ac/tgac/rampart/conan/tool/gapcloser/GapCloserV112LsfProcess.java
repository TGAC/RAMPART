package uk.ac.tgac.rampart.conan.tool.gapcloser;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

@ServiceProvider
public class GapCloserV112LsfProcess extends AbstractRampartLsfProcess {


	private static final long serialVersionUID = -875700673055262789L;

	public static final String NAME = "SOAP_GapCloser_v1.12-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.GAPCLOSER_1_12;
	public static final String COMMAND = "GapCloser";
	public static final String PARAM_PREFIX = "-";
	
	public GapCloserV112LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				GapCloserV112Param.values());
	}

}