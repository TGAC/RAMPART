package uk.ac.tgac.rampart.data;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("improver")
public class ImproverStats extends AssemblyStats implements Comparable<ImproverStats>, Serializable {
	
	private static final long serialVersionUID = 3838911367218298049L;
	
	private Integer stage;
	
	public ImproverStats() {}
	
	public ImproverStats(String[] stats) {
		super(Arrays.copyOfRange(stats, 1, 13));
		this.stage = Integer.parseInt(stats[0]);		
	}
	
	public Integer getStage() {
		return stage;
	}
	public void setStage(Integer stage) {
		this.stage = stage;
	}

	@Override
	public int compareTo(ImproverStats o) {
		return this.stage.compareTo(o.getStage());
	}
}
