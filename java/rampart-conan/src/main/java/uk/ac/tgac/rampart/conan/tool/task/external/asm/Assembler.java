package uk.ac.tgac.rampart.conan.tool.task.external.asm;

import uk.ac.tgac.rampart.conan.conanx.exec.task.ConanExternalTask;

public interface Assembler extends ConanExternalTask {

	AssemblerArgs getArgs();

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();
}