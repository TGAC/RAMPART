package uk.ac.tgac.rampart.conan.process.lsf;

public class LsfWaitCondition {

	public enum ExitStatus {
		ENDED("ended"),
		DONE("done");
		
		private String cmd;
		
		private ExitStatus(String cmd) {
			this.cmd = cmd;
		}
		
		public String getCommand() {
			return this.cmd;
		}
	}
	
	private ExitStatus exitStatus;
	private String condition;
	
	public LsfWaitCondition(ExitStatus exitStatus, String condition) {
		super();
		this.exitStatus = exitStatus;
		this.condition = condition;
	}

	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	public String getCondition() {
		return condition;
	}
	
	@Override
	public String toString() {
		return "-w " + this.exitStatus.getCommand() + "(" + this.condition + ")";
	}
	
}
