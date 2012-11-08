package uk.ac.tgac.rampart.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;


public class RampartLogger extends Logger
{
  private static final Level CONSOLE_LEVEL = Level.WARNING;
  private static final Level FILE_ERROR_LEVEL = Level.SEVERE;
  private static final Level FILE_INFO_LEVEL = Level.INFO;
  private static final Level FILE_DEBUG_LEVEL = Level.ALL;
  
  /* Server error log filename */
  private static final String ERROR_LOG_FILENAME = "rampart_error.log";
  
  /* Server info log filename */
  private static final String INFO_LOG_FILENAME = "rampart_info.log";
  
  /* Server info log filename */
  private static final String DEBUG_LOG_FILENAME = "rampart_debug.log";
  
  /* Log prefix */
  private static final String PREFIX = "RAMPART: ";
  
  /* We only want one */
  public static RampartLogger LOGGER = new RampartLogger();
  
  /**
   * Creates a new instance of the WorkbenchLogger.  This is a standalone logger
   * independent of any parents, including the root logger.
   */
  private RampartLogger()
  {
    this( new File( "." ), false );
  }
  
  /**
   * Creates a new instance of the WorkbenchLogger.  This is a standalone logger
   * independent of any parents, including the root logger.  Optionally, creates
   * an extra file handler for outputting debug messages.
   */
  private RampartLogger( File logDir, boolean verbose )
  {
    super( RampartLogger.class.getName(), null );
    
    // Make this logger independent of the root logger
    this.setUseParentHandlers( false );
    
    try
    {
      recreateHandlers( logDir, verbose );
    }
    catch(IOException e)
    {
      // Not much we can do if there was any issues opening the log file.  Just output
      // to System.err
      System.err.println( "RAMPART ERROR: Failed to create log files." );
      System.err.println( "RAMPART ERROR: Exception: " + e.toString() );
      System.err.println( "RAMPART ERROR: Resuming." );
    }
    
    this.setLevel( Level.ALL );
  }
  
  @Override
  public void log( LogRecord record )
  {
    record.setMessage( PREFIX + (record.getMessage() == null ? "" : record.getMessage()) );
    super.log( record );
  }
   
  public final void recreateHandlers( File dir, boolean verbose ) throws IOException
  {
    if ( dir == null )
      throw new NullPointerException( "Log Directory must be specified." );
    
    if ( !dir.isDirectory() )
      throw new IOException( "Path specified does not describe a directory: " + dir.getPath() );
    
    if ( !dir.canWrite() )
      throw new IOException( "Can't write to this location: " + dir.getPath() );
    
    File errorLogFile = new File( dir.getPath() + DIR_SEPARATOR + ERROR_LOG_FILENAME );
    File infoLogFile = new File( dir.getPath() + DIR_SEPARATOR + INFO_LOG_FILENAME );
    File debugLogFile = new File( dir.getPath() + DIR_SEPARATOR + DEBUG_LOG_FILENAME );
    
    if ( errorLogFile.exists() && !errorLogFile.canWrite() )
      throw new IOException( "Can't write to error log file: " + errorLogFile.getPath() );
    
    if ( infoLogFile.exists() && !infoLogFile.canWrite() )
      throw new IOException( "Can't write to info log file: " + infoLogFile.getPath() );
    
    if ( debugLogFile.exists() && !debugLogFile.canWrite() )
      throw new IOException( "Can't write to debug log file: " + debugLogFile.getPath() ); 
        
    // Clear any existing handlers
    for(Handler h : this.getHandlers())
    {
      this.removeHandler( h );
    }
    
    // Setup for System.err handler (only works if running the server directly)
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel( verbose ? Level.ALL : CONSOLE_LEVEL );
    this.addHandler( ch );
    
    // Setup file handlers
    SimpleFormatter formatter = new SimpleFormatter();
    FileHandler errorLog = new FileHandler( errorLogFile.getPath() );
    errorLog.setLevel( FILE_ERROR_LEVEL );
    errorLog.setFormatter( formatter );
    
    FileHandler infoLog = new FileHandler( infoLogFile.getPath() );
    infoLog.setLevel( FILE_INFO_LEVEL );
    infoLog.setFormatter( formatter );
    
    this.addHandler( errorLog );
    this.addHandler( infoLog );

    // Also do debug file if required
    if ( verbose )
    {
      FileHandler debugLog = new FileHandler( debugLogFile.getPath() );
      debugLog.setLevel( FILE_DEBUG_LEVEL ); 
      debugLog.setFormatter( formatter );
      this.addHandler( debugLog );    
    }
  }
}