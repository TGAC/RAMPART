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
package uk.ac.tgac.rampart.core.data;

import uk.ac.tgac.rampart.core.utils.StringJoiner;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="assembly_type",discriminatorType=DiscriminatorType.STRING)
@Table(schema="rampart",name="assembly_stats")
@DiscriminatorValue("generic")
public class AssemblyStats implements Serializable {
	
	private static final long serialVersionUID = -1893203475167525362L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="job_id")
	private Job job;
	
	@Column(name="file_path")
	private String filePath;
	
	@Column(name="nb_contigs")
	private Long nbContigs;
	
	@Column(name="nb_bases")
	private Long nbBases;
	
	@Column(name="a_perc")
	private Double aPerc;
	
	@Column(name="c_perc")
	private Double cPerc;
	
	@Column(name="g_perc")
	private Double gPerc;
	
	@Column(name="t_perc")
	private Double tPerc;
	
	@Column(name="n_perc")
	private Double nPerc;

    private Long n80;

	@Column(name="n50")
	private Long n50;

    private Long n20;

    private Long l50;
	
	@Column(name="min_len")
	private Long minLen;
	
	@Column(name="avg_len")
	private Double avgLen;
	
	@Column(name="max_len")
	private Long maxLen;
	
	public AssemblyStats() {}
	public AssemblyStats(String[] stats) {
		this.filePath = stats[0];
		this.nbContigs = Long.parseLong(stats[1]);
		this.aPerc = Double.parseDouble(stats[2]);
		this.cPerc = Double.parseDouble(stats[3]);
		this.gPerc = Double.parseDouble(stats[4]);
		this.tPerc = Double.parseDouble(stats[5]);
		this.nPerc = Double.parseDouble(stats[6]);
		this.nbBases = Long.parseLong(stats[7]);
		this.minLen = Long.parseLong(stats[8]);
		this.maxLen = Long.parseLong(stats[9]);
		this.avgLen = Double.parseDouble(stats[10]);
		this.n50 = Long.parseLong(stats[11]);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Long getNbContigs() {
		return nbContigs;
	}
	public void setNbContigs(Long nbContigs) {
		this.nbContigs = nbContigs;
	}
	public Long getNbBases() {
		return nbBases;
	}
	public void setNbBases(Long nbBases) {
		this.nbBases = nbBases;
	}
	public Double getaPerc() {
		return aPerc;
	}
	public void setaPerc(Double aPerc) {
		this.aPerc = aPerc;
	}
	public Double getcPerc() {
		return cPerc;
	}
	public void setcPerc(Double cPerc) {
		this.cPerc = cPerc;
	}
	public Double getgPerc() {
		return gPerc;
	}
	public void setgPerc(Double gPerc) {
		this.gPerc = gPerc;
	}
	public Double gettPerc() {
		return tPerc;
	}
	public void settPerc(Double tPerc) {
		this.tPerc = tPerc;
	}
	public Double getnPerc() {
		return nPerc;
	}
	public void setnPerc(Double nPerc) {
		this.nPerc = nPerc;
	}
	public Long getN50() {
		return n50;
	}
	public void setN50(Long n50) {
		this.n50 = n50;
	}
	public Long getMinLen() {
		return minLen;
	}
	public void setMinLen(Long minLen) {
		this.minLen = minLen;
	}
	public Double getAvgLen() {
		return avgLen;
	}
	public void setAvgLen(Double avgLen) {
		this.avgLen = avgLen;
	}
	public Long getMaxLen() {
		return maxLen;
	}
	public void setMaxLen(Long maxLen) {
		this.maxLen = maxLen;
	}

    public Long getN80() {
        return n80;
    }

    public void setN80(Long n80) {
        this.n80 = n80;
    }

    public Long getN20() {
        return n20;
    }

    public void setN20(Long n20) {
        this.n20 = n20;
    }

    public Long getL50() {
        return l50;
    }

    public void setL50(Long l50) {
        this.l50 = l50;
    }

    public static String getStatsFileHeader() {
        return "file|nbcontigs|a.pc|c.pc|g.pc|t.pc|n.pc|total|minlen|maxlen|avglen|n80|n50|n20|l50";
    }

    public String toStatsFileString() {

        StringJoiner sj = new StringJoiner("|");

        sj.add(this.getFilePath());
        sj.add(this.getNbContigs());
        sj.add(this.getaPerc());
        sj.add(this.getcPerc());
        sj.add(this.getgPerc());
        sj.add(this.gettPerc());
        sj.add(this.getnPerc());
        sj.add(this.getNbBases());
        sj.add(this.getMinLen());
        sj.add(this.getAvgLen());
        sj.add(this.getMaxLen());
        sj.add(this.getN80());
        sj.add(this.getN50());
        sj.add(this.getN20());
        sj.add(this.getL50());

        return sj.toString();
    }
}
