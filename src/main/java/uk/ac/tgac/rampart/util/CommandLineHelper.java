package uk.ac.tgac.rampart.util;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 25/10/13
 * Time: 13:41
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineHelper {

    public static void printHelp(OutputStream outputStream, String title, String description, Options options) {
        final PrintWriter writer = new PrintWriter(outputStream);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                100,
                title,
                description,
                options,
                3,
                3,
                "Created by The Genome Analysis Centre (TGAC), Norwich, UK\n" +
                "Contact: daniel.mapleson@tgac.ac.uk",
                true);
        writer.flush();
    }
}
