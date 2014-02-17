package uk.ac.tgac.rampart.util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 21/01/14
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class JobOutputMap extends HashMap<String, Set<File>> {

    public void updateTracker(String group, File newFile) {

        Set<File> fileSet = null;

        if (this.containsKey(group)) {
            fileSet = this.get(group);
        }
        else {
            this.put(group, new HashSet<File>());
            fileSet = this.get(group);
        }

        fileSet.add(newFile);
    }

    public void combine(JobOutputMap other) {

        for(Map.Entry<String, Set<File>> entry : other.entrySet()) {

            if(this.containsKey(entry.getKey())) {
                Set<File> fileSet = this.get(entry.getKey());

                for(File f : entry.getValue()) {
                    if (!fileSet.contains(f)) {
                        fileSet.add(f);
                    }
                }
            }
            else {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
