package uk.ac.tgac.rampart.pipeline.tool.proc.external.asm;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;

import java.io.File;

public interface Assembler extends ConanProcess {

    AbstractAssemblerArgs getArgs();

    void setArgs(AbstractAssemblerArgs args);

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();

    File getUnitigsFile();

    File getContigsFile();

    File getScaffoldsFile();
}