package uk.ac.tgac.rampart.conan.tool.process.asm;

import uk.ac.tgac.rampart.conan.conanx.process.ExtendedConanProcess;

public interface Assembler extends ExtendedConanProcess {

	AssemblerArgs getArgs();

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();
}