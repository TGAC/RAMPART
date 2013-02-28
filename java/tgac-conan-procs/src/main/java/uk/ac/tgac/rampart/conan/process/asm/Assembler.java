package uk.ac.tgac.rampart.conan.process.asm;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;

import java.io.File;

public interface Assembler extends ConanProcess {

    AssemblerArgs getArgs();

    void setArgs(AssemblerArgs args);

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();

    File getUnitigsFile();

    File getContigsFile();

    File getScaffoldsFile();
}