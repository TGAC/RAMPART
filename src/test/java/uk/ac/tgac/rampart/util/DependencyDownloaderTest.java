package uk.ac.tgac.rampart.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 25/10/13
 * Time: 13:25
 * To change this template use File | Settings | File Templates.
 */
public class DependencyDownloaderTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Test
    public void testDownload() throws IOException {

        /*File target = new File(temp.newFolder("downloads"), "tgac_logo_single.png");

        new DependencyDownloader().downloadFromUrl(
                new URL("http://www.tgac.ac.uk/v2images/tgac_logo_single.png"),
                target);

        assertTrue(target.exists()); */
    }
}
