package uk.ac.tgac.rampart.frontend.cli;
import java.net.URISyntaxException;

import org.junit.Test;


public class RampartTest {

	@Test
	public void test() throws URISyntaxException {
		
		Rampart.main(new String[] {
			"--config",
			"rampart.cfg"
		});
		
	}

}
