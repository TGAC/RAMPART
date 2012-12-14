package uk.ac.tgac.rampart.conan.process.lsf.tool;

import java.util.Collection;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.parameter.tool.AbyssV134Params;
import uk.ac.tgac.rampart.conan.process.lsf.AbstractRampartLsfProcess;
import uk.ac.tgac.rampart.util.ToolCommandLoader;

@ServiceProvider
public class LsfAbyssV134Process extends AbstractRampartLsfProcess {

	private static final long serialVersionUID = -7311765587559938252L;
	
	private static final String CMD = "abyss-pe";
	
	private AbyssV134Params params;
	
	public LsfAbyssV134Process(AbyssV134Params params) {
		
		this.setMemoryRequired(60000);
		this.setThreads(params.getThreads());
		
		this.params = params;
	}
	
	@Override
	protected String getLoadToolCommand() {
		return ToolCommandLoader.getInstance().getLoadToolCommand(ToolCommandLoader.ABYSS_1_3_4);
	}

	@Override
	protected String getComponentName() {
		return "MASS";
	}

	@Override
	protected String getToolCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		
		StringBuilder sb = new StringBuilder();
		sb.append(CMD);
		sb.append(" ");		
		for(Map.Entry<ConanParameter, String> param : parameters.entrySet()) {
			sb.append(param.getKey());
			sb.append("=");
			sb.append(param.getValue());
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}

	@Override
	public String getName() {
		return "AbyssV1.3.4_LSF";
	}

	@Override
	public Collection<ConanParameter> getParameters() {
		return this.params.getParameters();
	}

}