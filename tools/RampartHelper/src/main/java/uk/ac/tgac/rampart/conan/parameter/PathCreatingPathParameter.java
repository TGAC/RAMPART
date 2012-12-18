package uk.ac.tgac.rampart.conan.parameter;

import java.io.File;
import java.io.IOException;

import org.springframework.util.StringUtils;

/**
 * @author Dan Mapleson
 */
public class PathCreatingPathParameter extends DefaultConanParameter implements
		Optionable {

	private static final long serialVersionUID = -4219816838322594970L;
	
	public PathCreatingPathParameter(String name) {
		this(name, name, false);
	}

	public PathCreatingPathParameter(String name, String description,
			boolean isOptional) {
		super(name, description, false, isOptional, false);
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

}