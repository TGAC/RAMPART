/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.conan.tool.internal.mass;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.conanx.env.arch.ExitStatusType;
import uk.ac.tgac.rampart.conan.conanx.env.arch.WaitCondition;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.service.impl.DefaultProcessExecutionService;
import uk.ac.tgac.rampart.conan.tool.DeBrujinAssembler;
import uk.ac.tgac.rampart.conan.tool.PerlHelper;
import uk.ac.tgac.rampart.core.utils.StringJoiner;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Map;

public class MassProcess implements ConanProcess {

    protected ProcessExecutionService processExecutionService = new DefaultProcessExecutionService();

    private MassArgs args;
	
	public MassProcess() {
		this(new MassArgs());
	}

    public MassProcess(MassArgs args) {
        this.args = args;
    }


	protected int getFirstValidKmer(int kmin) {
		
		if (kmin <= 11)
			return 11;
		
		String kminStr = String.valueOf(kmin);
		if (kminStr.charAt(kminStr.length()-1) == '1') {
			return kmin;
		}
		else {
			char tensDigit = kminStr.charAt(kminStr.length()-2);
			
			int tens = (Integer.parseInt(String.valueOf(tensDigit)) + 1) * 10;
			
			int rest = 0;
			
			if (kminStr.length() > 2) {
				String restStr = kminStr.substring(0, kminStr.length()-3) + "00";
				rest = Integer.parseInt(restStr);
			}
			
			return rest + tens + 1;
		}
	}

    /**
     * Retrieves the next k-mer value in the sequence
     * @param kmer The current k-mer value
     * @return The next kmer value
     */
	protected int nextKmer(int kmer) {
		
		int mod1 = (kmer - 1) % 10;
		int mod2 = (kmer - 5) % 10;
		
		if (mod1 == 0) {
			return kmer + 4;
		}
		else if (mod2 == 0) {
			return kmer + 6;
		}
		else {
			throw new IllegalArgumentException("Kmer values have somehow got out of step!!");
		}
	}

    /**
     * Determines whether or not the supplied k-mer value is valid
     * @param kmer The k-mer value to validate
     * @return True if valid, false otherwise
     */
	public boolean validKmer(int kmer) {
		
		int mod1 = (kmer - 1) % 10;
		int mod2 = (kmer - 5) % 10;

		return (mod1 == 0 || mod2 == 0 ) ? true : false;
	}


	
	
	protected void createSupportDirectories() {
		
		DeBrujinAssembler assembler = this.args.getAssembler();

		// Create directory for links to assembled contigs
		if (assembler.makesUnitigs()) {
            this.args.getUnitigsDir().mkdir();
        }

        if (assembler.makesContigs()) {
            this.args.getContigsDir().mkdir();
        }
		
		// Create dir for scaffold links if this assembler creates them
		if (this.args.getAssembler().makesScaffolds()) {
			this.args.getScaffoldsDir().mkdir();
		}
		
		// Create directory for logs
		this.args.getLogsDir().mkdir();
	}

    /**
     * Dispatches assembly jobs to the specfied environments
     * @param env The environment to dispatch jobs too
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
	public void dispatchJobs(Environment env) throws IOException, IllegalArgumentException,
		ProcessExecutionException, InterruptedException {

		// Check the range looks reasonable
		this.args.validateKmers();

        // Create a copy of the environment info (we're going to modify this)
		Environment envCopy = env.copy();

        // Set mem required to 60GB if no memory requested
        if (envCopy.getEnvironmentArgs().getMemoryMB() == 0) {
            envCopy.getEnvironmentArgs().setMemoryMB(60000);
        }

        // Set num threads to 8 if none specified
        if (envCopy.getEnvironmentArgs().getThreads() == 0) {
            envCopy.getEnvironmentArgs().setThreads(8);
        }

        // If we're running on a grid engine then make sure the assembly jobs run in the background
        if (envCopy.getArchitecture().isGridEngine()) {
            envCopy.getEnvironmentArgs().setBackgroundTask(true);
        }

        // Input the libraries to the assembler
        if (this.args.getAssembler().getArgs().getLibraries() == null) {
            this.args.getAssembler().getArgs().setLibraries(this.args.getLibs());
        }

		// Create any required directories for this job
        createSupportDirectories();

        // Dispatch an assembly job for each requested kmer
		for(int k = getFirstValidKmer(this.args.getKmin()); k <= this.args.getKmax(); nextKmer(k)) {
			
			File kDir = new File(this.args.getOutputDir(), String.valueOf(k));
						
			// Make the output directory for this child job (delete the directory if it exists)
			if (kDir.exists()) {
				FileUtils.deleteDirectory(kDir);	
			}			
			kDir.mkdir();
			
			// Modify the ProcessArgs kmer value
			this.args.getAssembler().getArgs().setKmer(k);

            // Modify the environment jobname
            envCopy.getEnvironmentArgs().setJobName(this.args.getJobPrefix() + "-k" + k);

            // Create process
            this.processExecutionService.execute(this.args.getAssembler(), env);
        }

        // Wait for all assembly jobs to finish if they are running as background tasks.
        if (env.getEnvironmentArgs().isBackgroundTask()) {

            WaitCondition waitCondition = env.getArchitecture().createWaitCondition(ExitStatusType.COMPLETED_SUCCESS, this.args.getJobPrefix());

            envCopy.getArchitecture().waitFor(waitCondition, envCopy.getEnvironmentArgs());
        }
		
		this.dispatchStatsJob(envCopy);
		
		// Not sure if this is required in a java env
		//this.log();
	}
	
	
	
	protected String buildStatCmdLine(File statDir) {
		
		StringJoiner mgpArgs = new StringJoiner(" ");

        mgpArgs.add(PerlHelper.MASS_GP.getPath());
        mgpArgs.add("--grid_engine NONE");
        mgpArgs.add("--input " + statDir.getPath());
        mgpArgs.add("--output " + statDir.getPath());
        //mgpArgs.add(this.isVerbose(), "", "--verbose");

		return mgpArgs.toString();
	}
	
	
	protected void dispatchStatsJob(Environment env) throws InterruptedException, ProcessExecutionException, ConnectException {
		
		// Alter the environment for this job
        env.getEnvironmentArgs().setJobName(this.args.getJobPrefix() + "-stats");
        env.getEnvironmentArgs().setThreads(0);
        env.getEnvironmentArgs().setMemoryMB(0);
        env.getEnvironmentArgs().setBackgroundTask(false);
		
		StringJoiner statCommands = new StringJoiner("; ");

        statCommands.add(buildStatCmdLine(this.args.getUnitigsDir()));
        statCommands.add(buildStatCmdLine(this.args.getContigsDir()));
		statCommands.add(buildStatCmdLine(this.args.getScaffoldsDir()));

        // Create process
        this.processExecutionService.execute(statCommands.toString(), env);
	}

    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, IllegalArgumentException, InterruptedException {

        //this.args.setFromParameterValuePairs(parameters);

        //Environment env = new DefaultEnvironment();

        /*try {
            this.dispatchJobs(env);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return false;
    }

    @Override
    public String getName() {
        return "MASS";
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return new MassParams().getConanParameters();
    }
}
