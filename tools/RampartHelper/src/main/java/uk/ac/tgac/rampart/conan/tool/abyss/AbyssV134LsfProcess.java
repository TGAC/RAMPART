package uk.ac.tgac.rampart.conan.tool.abyss;

import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

@ServiceProvider
public class AbyssV134LsfProcess extends AbstractRampartLsfProcess {

	private static final long serialVersionUID = -7311765587559938252L;
	
	public static final String NAME = "Abyss_V1.3.4-LSF";
	public static final String COMPONENT_NAME = ToolCommandLoader.ABYSS_1_3_4;
	public static final String COMMAND = "abyss-pe";
	public static final String PARAM_PREFIX = "";
	
	public AbyssV134LsfProcess() {
		super(NAME, COMPONENT_NAME, COMMAND, PARAM_PREFIX, 
				AbyssV134Param.values());
		
		this.setMemoryRequired(60000);
		this.setThreads(8);
	}
	
	@Override
	public String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		StringBuilder sb = new StringBuilder();
		sb.append(COMMAND);
		sb.append(" ");		
		for(Map.Entry<ConanParameter, String> param : parameters.entrySet()) {
			sb.append(param.getKey());
			sb.append("=");
			sb.append(param.getValue());
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}
}