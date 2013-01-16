package uk.ac.tgac.rampart.conan.tool;

import uk.ac.tgac.rampart.conan.conanx.process.ExtendedConanProcess;
import uk.ac.tgac.rampart.conan.tool.args.DeBrujinAssemblerArgs;

public interface DeBrujinAssembler extends ExtendedConanProcess {

	DeBrujinAssemblerArgs getArgs();

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();
}