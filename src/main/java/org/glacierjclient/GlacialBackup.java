/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierjclient;

import java.io.IOException;

import org.glacierjclient.cli.CLIArguments;
import org.glacierjclient.cli.CLICommands;
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
      log.debug(ns.toString());
      CLICommands.findAndExec(ns);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
  }
}