package uk.ac.tgac.rampart.pipeline.tool.proc.external.asm;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;

public interface Assembler extends ConanProcess {

    AssemblerArgs getArgs();

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();
}