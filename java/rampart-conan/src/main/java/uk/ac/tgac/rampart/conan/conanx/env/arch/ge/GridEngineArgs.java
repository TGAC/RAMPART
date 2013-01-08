package uk.ac.tgac.rampart.conan.conanx.env.arch.ge;

import uk.ac.tgac.rampart.conan.conanx.env.EnvironmentArgs;

import java.io.File;

public abstract class GridEngineArgs implements EnvironmentArgs {

	private String jobName;
	private String queueName;
	private int threads;
	private int memoryMB;
    private File cmdLineOutputFile;

    protected GridEngineArgs() {
        this.jobName = "";
        this.queueName = "";
        this.threads = 0;
        this.memoryMB = 0;
    }

    protected GridEngineArgs(GridEngineArgs args) {
        this.jobName = args.getJobName();
        this.queueName = args.getQueueName();
        this.threads = args.getThreads();
        this.memoryMB = args.getMemoryMB();
    }

	@Override
	public String getJobName() {
		return jobName;
	}

    @Override
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

    @Override
	public int getThreads() {
		return threads;
	}

    @Override
	public void setThreads(int threads) {
		this.threads = threads;
	}

    @Override
	public int getMemoryMB() {
		return memoryMB;
	}

    @Override
	public void setMemoryMB(int memoryMB) {
		this.memoryMB = memoryMB;
	}

    @Override
    public void setCmdLineOutputFile(File cmdLineOutputFile) {
        this.cmdLineOutputFile = cmdLineOutputFile;
    }

    @Override
    public File getCmdLineOutputFile() {
        return this.cmdLineOutputFile;
    }
}
