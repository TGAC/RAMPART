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
package uk.ac.tgac.rampart.conan.tool.internal;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.conanx.env.Environment;
import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;
import uk.ac.tgac.rampart.conan.service.ProcessExecutionService;
import uk.ac.tgac.rampart.conan.tool.DeBrujinAssembler;
import uk.ac.tgac.rampart.conan.tool.args.DeBrujinAssemblerArgs;
import uk.ac.tgac.rampart.core.utils.StringJoiner;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

public class Mass {

    // Constants
	public static final int KMER_MIN = 11;
	public static final int KMER_MAX = 125;

    @Autowired
    private ProcessExecutionService processExecutionService;

    // Class vars
	private DeBrujinAssembler assembler;
	private DeBrujinAssemblerArgs assemblerArgs;
	private File config;
	private int kmin;
	private int kmax;
	private String jobPrefix;
	private File outputDir;
	
	// Generated vars
	private File contigsDir;
	private File scaffoldsDir;
	private File logsDir;
	
	public Mass() {
		this.assembler = null;
		this.assemblerArgs = null;
		this.config = null;
		this.kmin = 41;
		this.kmax = 95;
		this.jobPrefix = "";
		this.outputDir = null;
	}


	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
		
		this.contigsDir = new File(this.outputDir, "contigs");
		this.scaffoldsDir = new File(this.outputDir, "scaffolds");
		this.logsDir = new File(this.outputDir, "logs");
	}

	public void setAssemblerArgs(DeBrujinAssemblerArgs assemblerArgs) {
		this.assemblerArgs = assemblerArgs;
	}

	public DeBrujinAssemblerArgs getAssemblerArgs() {
		return assemblerArgs;
	}

	public File getConfig() {
		return config;
	}

	public void setConfig(File config) {
		this.config = config;
	}

	public int getKmin() {
		return kmin;
	}

	public void setKmin(int kmin) {
		this.kmin = kmin;
	}

	public int getKmax() {
		return kmax;
	}

	public void setKmax(int kmax) {
		this.kmax = kmax;
	}
	
	public DeBrujinAssembler getAssembler() {
		return assembler;
	}

	public void setAssembler(DeBrujinAssembler assembler) {
		this.assembler = assembler;
	}

	public String getJobPrefix() {
		return jobPrefix;
	}

	public void setJobPrefix(String jobPrefix) {
		this.jobPrefix = jobPrefix;
	}
	
	public File getContigsDir() {
		return this.contigsDir;
	}
	
	public File getScaffoldsDir() {
		return new File(this.outputDir, "scaffolds");
	}
	
	public File getLogsDir() {
		return new File(this.outputDir, "logs");
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

    /**
     * Determines whether or not the supplied kmer range is valid.  Throws an exception if not.
     * @param kmin The bottom end of the k-mer range
     * @param kmax The top end of the k-mer range
     */
	public void validateKmers(int kmin, int kmax) {
		
		if (kmin < KMER_MIN || kmax < KMER_MIN)
			throw new IllegalArgumentException("K-mer values must be >= " + KMER_MIN + "nt");
		
		if (kmin > KMER_MAX || kmax > KMER_MAX)
			throw new IllegalArgumentException("K-mer values must be <= " + KMER_MAX + "nt");
		
		if (kmin > kmax)
			throw new IllegalArgumentException("Error: Min K-mer value must be <= Max K-mer value");
		
		// This test isn't required... we just make a best effort between the range provided.
		//if (!validKmer(kmin) || !validKmer(kmax))
		//	throw new IllegalArgumentException("Error: K-mer min and K-mer max both must end with a '1' or a '5'.  e.g. 41 or 95.");
	}
	
	
	protected void createSupportDirectories() {
		
		// Create directory for links to assembled contigs
		this.contigsDir.mkdir();
		
		// Create dir for scaffold links if this assembler creates them
		if (this.assembler.makesScaffolds()) {
			this.scaffoldsDir.mkdir();
		}
		
		// Create directory for logs
		this.logsDir.mkdir();
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
		//TODO This logic isn't bullet proof... we can still nudge the minKmer above the maxKmer 
		validateKmers(this.kmin, this.kmax);

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

		// Create any required directories for this job
        createSupportDirectories();

        // Dispatch an assembly job for each requested kmer
		for(int k = getFirstValidKmer(this.kmin); k <= this.kmax; nextKmer(k)) {
			
			File kDir = new File(this.outputDir, String.valueOf(k));
						
			// Make the output directory for this child job (delete the directory if it exists)
			if (kDir.exists()) {
				FileUtils.deleteDirectory(kDir);	
			}			
			kDir.mkdir();
			
			// Modify the ProcessArgs kmer value
			DeBrujinAssemblerArgs kAssemblerArgs = this.assemblerArgs.copy();
			kAssemblerArgs.setKmer(k);

            // Modify the environment jobname
            envCopy.getEnvironmentArgs().setJobName(this.jobPrefix + "-k" + k);

            // Create process
            this.processExecutionService.execute(this.assembler, env);
        }
		
		this.dispatchStatsJob(envCopy);
		
		// Not sure if this is required in a java env
		//this.log();
	}
	
	
	
	protected String buildStatCmdLine(File statDir) {
		
		StringJoiner mgpArgs = new StringJoiner(" ");

       // mgpArgs.add(PerlHelper.MASS_GP.getPath());
        mgpArgs.add("--grid_engine NONE");
        mgpArgs.add("--input " + statDir.getPath());
        mgpArgs.add("--output " + statDir.getPath());
        //mgpArgs.add(this.isVerbose(), "", "--verbose");

		return mgpArgs.toString();
	}
	
	
	protected void dispatchStatsJob(Environment env) throws InterruptedException, ProcessExecutionException, ConnectException {
		
		// Alter the environment for this job
        env.getEnvironmentArgs().setJobName(this.jobPrefix + "-stats");
        env.getEnvironmentArgs().setThreads(0);
        env.getEnvironmentArgs().setMemoryMB(0);
		
		StringJoiner statCommands = new StringJoiner("; ");

        statCommands.add(buildStatCmdLine(this.contigsDir));
		statCommands.add(this.scaffoldsDir != null, "", buildStatCmdLine(this.scaffoldsDir));

        // Create process
        this.processExecutionService.execute(statCommands.toString(), env);
	}

}
