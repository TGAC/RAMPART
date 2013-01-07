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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.conan.env.arch.ge.GridEngineArgs;
import uk.ac.tgac.rampart.conan.tool.DeBrujinAssembler;
import uk.ac.tgac.rampart.conan.tool.args.DeBrujinAssemblerArgs;

public class Mass {

	public static final int KMER_MIN = 11;
	public static final int KMER_MAX = 125;
	
	
	private DeBrujinAssembler assembler;
	private DeBrujinAssemblerArgs assemblerArgs;
	private GridEngineArgs geArgs;
	private File config;
	private int kmin;
	private int kmax;
	private String jobPrefix;
	private File outputDir;
	
	// Generated args
	private File contigsDir;
	private File scaffoldsDir;
	private File logsDir;
	
	public Mass() {
		this.assembler = null;
		this.assemblerArgs = null;
		this.geArgs = null;
		this.config = null;
		this.kmin = 41;
		this.kmax = 95;
		this.jobPrefix = "";
		this.outputDir = null;
	}
	
	public GridEngineArgs getGeArgs() {
		return geArgs;
	}

	public void setGeArgs(GridEngineArgs geArgs) {
		this.geArgs = geArgs;
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
	
	public boolean validKmer(int kmer) {
		
		int mod1 = (kmer - 1) % 10;
		int mod2 = (kmer - 5) % 10;

		return (mod1 == 0 || mod2 == 0 ) ? true : false;
	}
	
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
	

	public void dispatchJobs() throws IOException, IllegalArgumentException, 
		ProcessExecutionException, InterruptedException {

		// Check the range looks reasonable
		//TODO This logic isn't bullet proof... we can still nudge the minKmer above the maxKmer 
		validateKmers(this.kmin, this.kmax);
		
		createSupportDirectories();
		
		for(int k = getFirstValidKmer(this.kmin); k <= this.kmax; nextKmer(k)) {
			
			File kDir = new File(this.outputDir, String.valueOf(k));
						
			// Make the output directory for this child job (delete the directory if it exists)
			if (kDir.exists()) {
				FileUtils.deleteDirectory(kDir);	
			}			
			kDir.mkdir();
			
			// Create customer DeBrujinAssemblerArgs for this kmer
			DeBrujinAssemblerArgs kAssemblerArgs = this.assemblerArgs.copy();
			kAssemblerArgs.setKmer(k);
			
			// May not want to run on a grid engine (although that's probably unlikely)
			// so check before creating some GE arguments.
			GridEngineArgs kGeArgs = null;
			if (this.geArgs != null) {
				kGeArgs = this.geArgs.copy();
				kGeArgs.setJobName(this.jobPrefix + "-k" + k);
			}
			
			// Execute the assembler with this kmer value
			this.assembler.execute(kAssemblerArgs, kGeArgs, kDir);	
		}
		
		this.dispatchStatsJob();
		
		// Not sure if this is required in a java env
		//this.log();
	}
	
	
	
	protected String buildStatCmdLine(File statDir) {
		
		/*my @mgp_args = grep {$_} (
			$MASS_GP_PATH,
			"--grid_engine NONE",
			$qst->isVerboseAsParam(),
			"--input " .  $stat_dir,
			"--output " . $stat_dir
		);

		my $mgp_cmd_line = join " ", @mgp_args;*/

        List<String> mgpArgs = new ArrayList<String>();

        mgpArgs.add(PerlHelper.MASS_GP.getPath());
        mgpArgs.add("--grid_engine NONE");
        mgpArgs.add("--input " + statDir.getPath());
        mgpArgs.add("--output " + statDir.getPath());

        if (this.isVerbose()) {

        }

		return "";
	}
	
	
	protected void dispatchStatsJob() {
		
		GridEngineArgs geStatJobArgs = null;
		
		if (this.geArgs != null) {
			geStatJobArgs = this.geArgs.copy();
		} 
		
		List<String> statCommands = new ArrayList<String>();
		
		statCommands.add(buildStatCmdLine(this.contigsDir));
		
		if (scaffoldsDir != null) {
			statCommands.add(buildStatCmdLine(this.scaffoldsDir));
		}
		
		String statCommand = StringUtils.join(statCommands, "; ");
		
		// Submit the job with 	geStatJobArgs	and statCommand

	}

}
