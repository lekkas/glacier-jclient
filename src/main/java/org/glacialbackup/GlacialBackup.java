/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup;

import java.io.IOException;
import org.glacialbackup.aws.cache.LocalCache;
import org.glacialbackup.cli.CLIArguments;
import org.glacialbackup.cli.CLICommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class GlacialBackup {

  public static Logger log = LoggerFactory.getLogger(GlacialBackup.class);
  
  public static void main(String[] args) throws IOException, InterruptedException {
    ArgumentParser parser = CLIArguments.createArgsParser();
    Namespace ns;
    try {
      ns = parser.parseArgs(args);
      System.out.println(ns);
      CLICommands.findAndExec(ns);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
    // System.out.println("---- CACHE ----");
    // LocalCache.loadCache().prettyPrintVaults();
  }
}