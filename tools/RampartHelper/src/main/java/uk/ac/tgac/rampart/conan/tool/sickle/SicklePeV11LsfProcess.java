package uk.ac.tgac.rampart.conan.tool.sickle;

import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

public class SicklePeV11LsfProcess extends AbstractRampartLsfProcess  {

	private static final long serialVersionUID = 7102159667412634823L;

	public static final String NAME = "Sickle_V1.1-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.SICKLE_1_1;
	public static final String COMMAND = "sickle pe";
	public static final String PARAM_PREFIX = "--";
	
	public SicklePeV11LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				SicklePeV11Param.values());
	}

}
