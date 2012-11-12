package uk.ac.tgac.rampart.util;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

public class Tools {

	 public static String getStackTrace(Throwable t)
	  {
	    StringBuilder sb = new StringBuilder();
	    
	    for(StackTraceElement e : t.getStackTrace())
	    {
	      sb.append( e.toString() ).append( String.valueOf(LINE_SEPARATOR) );
	    }
	    
	    return sb.toString();
	  }
}
