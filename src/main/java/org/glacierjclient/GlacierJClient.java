/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient;

import java.io.IOException;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.cli.CLIArguments;
import org.glacierjclient.cli.CLICommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlacierJClient {

  public static Logger log = LoggerFactory.getLogger(GlacierJClient.class);

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