package uk.ac.tgac.rampart.conan.parameter;

import org.springframework.util.StringUtils;
import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

import java.io.File;
import java.io.IOException;

/**
 * @author Rob Davey
 */
public class PathCreatingPathParameter extends AbstractConanParameter implements
		Optionable {

	private static final long serialVersionUID = -4219816838322594970L;
	private boolean optional = false;

	public PathCreatingPathParameter(String name) {
		super(name);
	}

	public PathCreatingPathParameter(String name, boolean isBoolean) {
		super(name, isBoolean);
	}

	public PathCreatingPathParameter(String name, String description) {
		super(name, description);
	}

	public PathCreatingPathParameter(String name, String description,
			boolean isBoolean) {
		super(name, description, isBoolean);
	}

	@Override
	public boolean validateParameterValue(String value) {
		File f = new File(value);
		try {
			if (!StringUtils.containsWhitespace(value) && !value.contains("~")) {
				if (!f.exists()) {
					if (f.isDirectory()) {
						return (f.mkdirs());
					} else {
						if (!f.getParentFile().exists())
							f.getParentFile().mkdirs();
						return f.createNewFile() && f.delete();
					}
				}
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
}