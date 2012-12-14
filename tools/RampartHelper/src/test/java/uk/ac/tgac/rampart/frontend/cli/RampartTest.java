package uk.ac.tgac.rampart.frontend.cli;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.tgac.rampart.frontend.cli.Rampart;


public class RampartTest {

	@Test
	public void test() {
		
		Rampart.main(new String[] {
			"--config",
			"rampart.cfg"
		});
		
	}

}
