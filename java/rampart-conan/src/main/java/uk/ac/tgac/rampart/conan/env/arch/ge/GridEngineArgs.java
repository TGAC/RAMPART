package uk.ac.tgac.rampart.conan.env.arch.ge;

import uk.ac.tgac.rampart.conan.env.EnvironmentArgs;

public abstract class GridEngineArgs implements EnvironmentArgs {

	private String jobName = "";
	private String queueName = "";
	private int threads = 1;
	private int memoryMB = 0;
	
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public int getMemoryMB() {
		return memoryMB;
	}
	public void setMemoryMB(int memoryMB) {
		this.memoryMB = memoryMB;
	}

    public abstract GridEngineArgs copy();

}
