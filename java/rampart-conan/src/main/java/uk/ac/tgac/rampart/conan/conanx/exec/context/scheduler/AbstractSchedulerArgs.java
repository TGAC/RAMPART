package uk.ac.tgac.rampart.conan.conanx.exec.context.scheduler;

import java.io.File;

public abstract class AbstractSchedulerArgs implements SchedulerArgs {

    private String jobName;
    private String queueName;
    private int threads;
    private int memoryMB;
    private File monitorFile;
    private int monitorInterval;
    private boolean backgroundTask;

    protected AbstractSchedulerArgs() {
        this.jobName = "";
        this.queueName = "";
        this.threads = 0;
        this.memoryMB = 0;
        this.backgroundTask = false;
        this.monitorFile = null;
        this.monitorInterval = 15;
    }

    protected AbstractSchedulerArgs(AbstractSchedulerArgs args) {
        this.jobName = args.getJobName();
        this.queueName = args.getQueueName();
        this.threads = args.getThreads();
        this.memoryMB = args.getMemoryMB();
        this.backgroundTask = args.isBackgroundTask();
        this.monitorFile = args.getMonitorFile();
        this.monitorInterval = args.getMonitorInterval();
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
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
    public void setMonitorFile(File monitorFile) {
        this.monitorFile = monitorFile;
    }

    @Override
    public File getMonitorFile() {
        return this.monitorFile;
    }

    @Override
    public int getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(int monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    @Override
    public boolean isBackgroundTask() {
        return this.backgroundTask;
    }

    @Override
    public void setBackgroundTask(boolean state) {
        this.backgroundTask = state;
    }
}
