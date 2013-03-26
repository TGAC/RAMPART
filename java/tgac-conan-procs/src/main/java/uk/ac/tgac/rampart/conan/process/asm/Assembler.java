package uk.ac.tgac.rampart.conan.process.asm;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;

import java.io.File;

public interface Assembler extends ConanProcess {

    AssemblerArgs getArgs();

    boolean makesUnitigs();

    boolean makesContigs();

    boolean makesScaffolds();

    File getUnitigsFile();

    File getContigsFile();

    File getScaffoldsFile();

    boolean usesOpenMpi();
}