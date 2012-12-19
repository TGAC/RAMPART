package uk.ac.tgac.rampart.conan.parameter;

public class GEArgs {

	private String jobName;
	private String queueName;
	private String projectName;
	private int threads;
	private int memoryMB;
	
	public GEArgs() {
		this("", "", "", 1, 0);
	}
	
	public GEArgs(String jobName, String queueName, String projectName,
			int threads, int memoryMB) {
		this.jobName = jobName;
		this.queueName = queueName;
		this.projectName = projectName;
		this.threads = threads;
		this.memoryMB = memoryMB;
	}
	
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
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
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
	
	public GEArgs copy() {
		GEArgs copy = new GEArgs();
		
		copy.jobName = this.jobName;
		copy.queueName = this.queueName;
		copy.projectName = this.projectName;
		copy.memoryMB = this.memoryMB;
		copy.threads = this.threads;
		
		return copy;
	}
}
