/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.TextHelper;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentChoice;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
 
public class CLIArguments {
  
  private static class CaseInsensitiveStringChoice implements ArgumentChoice {
    
    private Collection<String> values_;
    
    public CaseInsensitiveStringChoice(String... values) {
      values_ = new ArrayList<String>();
      for(String s : values) {
        values_.add(s.toLowerCase());
      }
    }
    
    @Override
    public boolean contains(Object val) {
      if (values_.isEmpty()) {
        // If values is empty, we don't have type information, so
        // just return false.
        return false;
      }
      String first = values_.iterator().next();
      if (first.getClass().equals(val.getClass())) {
          return values_.contains(val.toString().toLowerCase());
      } else {
          throw new IllegalArgumentException(String.format(
                  "type mismatch (Make sure that you specified correct Argument.type()):"
                          + " expected: %s actual: %s",
                  first.getClass().getName(), val.getClass().getName()
          ));
      }
    }

    @Override
    public String textualFormat() {
      return TextHelper.concat(values_, 0, ",", "[", "]");
    }
  }
  
  /*
   * List of AWS endpoint regions
   */
  public static final String[] Endpoints = { 
    "us-east-1", 
    "us-west-1", 
    "us-west-2",
    "eu-west-1",
    "ap-southeast-2",
    "ap-northeast-1"
  };
  
  public static ArgumentParser createArgsParser() {

    ArgumentParser parser = ArgumentParsers.newArgumentParser("GlacierBackup")
        .description("Command line interface for Amazon Glacier");

    Subparsers commands = parser.addSubparsers()
        .title("commands")
        .metavar("COMMAND")
        .help("Description")
        .dest("command_name");

    Subparser vault = commands.addParser("vault").aliases("v").help("Vault operations");
    MutuallyExclusiveGroup vaultOptions = 
        vault.addMutuallyExclusiveGroup("Vault operation options").required(true);
    
    vault.addArgument("-s", "--sync")
        .help("Query AWS server for vault metadata/inventory and cache the results")
        .action(Arguments.storeTrue());
    
    vault.addArgument("--credentials")
        .help("Location of AWS credentials")
        .metavar("<file>");
    
    vault.addArgument("-e","--endpoint")
        .help("Set endpoint "+Arrays.asList(Endpoints).toString())
        .choices(new CaseInsensitiveStringChoice(Endpoints))
        .metavar("<region>")
        .required(true);
    
    vaultOptions.addArgument("-l", "--list")
        .help("List vaults")
        .action(Arguments.storeTrue());
    
    vaultOptions.addArgument("-i", "--inventory")
        .help("Vault inventory")
        .metavar("<name>");
    
    vaultOptions.addArgument("-m", "--meta")
        .help("Vault metadata")
        .metavar("<name>");
    
    vaultOptions.addArgument("--delete")
        .help("Delete vault")
        .metavar("<name>");
    
    vaultOptions.addArgument("-c", "--create")
        .help("Create vault")
        .metavar("<name>");
    
    // vaultOptions.addArgument("vault-name").metavar("<vault name>").nargs(1);
    
    Subparser archive = commands.addParser("archive").aliases("a").help("Archive operations");
    ArgumentGroup archiveOptions = archive.addArgumentGroup("Archive operation options");
    
    archiveOptions.addArgument("-d", "--defrost")
        .help("Request arcive (Takes ~4.5 hrs)")
        .metavar("<archive-id>");
    
    archiveOptions.addArgument("-r", "--retrieve")
        .help("Retrieve archive")
        .metavar("<archive-id>");
    
    archiveOptions.addArgument("--delete")
        .help("Delete archive")
        .metavar("<archive-id>");
    
    archiveOptions.addArgument("-u", "--upload")
        .help("Upload archive")
        .metavar("<file>");

    Subparser jobs = commands.addParser("job").aliases("j").help("Job operations");
    ArgumentGroup jobOptions = jobs.addArgumentGroup("Job operation options");
    
    jobOptions.addArgument("-l", "--list")
        .help("List active jobs")
        .action(Arguments.storeTrue());
    
    jobOptions.addArgument("--abort")
        .help("Abort job")
        .metavar("<job-id>");

    return parser;
  }
}
