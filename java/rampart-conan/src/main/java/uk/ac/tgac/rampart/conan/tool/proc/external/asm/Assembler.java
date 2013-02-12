package uk.ac.tgac.rampart.conan.tool.proc.external.asm;

import uk.ac.tgac.rampart.conan.conanx.exec.process.ConanXProcess;

public interface Assembler extends ConanXProcess {

    AssemblerArgs getArgs();

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();
}