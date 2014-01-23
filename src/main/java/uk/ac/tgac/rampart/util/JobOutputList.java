package uk.ac.tgac.rampart.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
public class JobOutputList extends ArrayList<JobOutput> {

    public List<Integer> getJobIds() {

        List<Integer> jobIds = new ArrayList<>();
        for(JobOutput jo : this) {
            jobIds.add(jo.getJobId());
        }

        return jobIds;
    }

    public List<File> getFiles() {

        List<File> jobIds = new ArrayList<>();
        for(JobOutput jo : this) {
            jobIds.add(jo.getOutputFile());
        }

        return jobIds;
    }
}
