/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.cli;

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

  private static class CaseSensitiveStringChoice implements ArgumentChoice {
    private Collection<String> values_;

    public CaseSensitiveStringChoice(String... values) {
      values_ = new ArrayList<String>();
      for(String s : values) {
        values_.add(s);
      }
    }

    @Override
    public boolean contains(Object val) {
      if (values_.isEmpty()) {
        return false;
      }
      String first = values_.iterator().next();
      if (first.getClass().equals(val.getClass())) {
        return values_.contains(val.toString());
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
  public static final String[] Regions = {
    "us-east-1",
    "us-west-1",
    "us-west-2",
    "eu-west-1",
    "ap-southeast-2",
    "ap-northeast-1"
  };

  public static final String[] Job_List_Options = {
    "All",
    "InProgress",
    "Succeeded",
    "Failed",
  };

  public static ArgumentParser createArgsParser() {

    ArgumentParser parser = ArgumentParsers.newArgumentParser("GlacierBackup")
        .description("Command line interface for Amazon Glacier");

    parser.addArgument("--region")
    .help("Set region "+Arrays.asList(Regions).toString())
    .choices(new CaseInsensitiveStringChoice(Regions))
    .metavar("<region>")
    .setDefault("eu-west-1");

    parser.addArgument("--credentials")
    .help("Location of AWS credentials")
    .metavar("<file>");

    Subparsers commands = parser.addSubparsers()
        .title("commands")
        .metavar("COMMAND")
        .help("Description")
        .dest("command_name");

    Subparser vault = commands.addParser("vault").aliases("v").help("Vault operations");
    MutuallyExclusiveGroup vaultOperations =
        vault.addMutuallyExclusiveGroup("vault operations").required(true);
    /*
    requiredVaultArgs.addArgument("-e","--endpoint")
    .help("Set endpoint "+Arrays.asList(Endpoints).toString())
    .choices(new CaseInsensitiveStringChoice(Endpoints))
    .metavar("<region>")
    .required(true);
     */
    vaultOperations.addArgument("-l", "--list")
    .help("List vaults")
    .action(Arguments.storeTrue());

    vaultOperations.addArgument("-i", "--inventory")
    .help("Retrieve vault inventory")
    .metavar("<name>");

    vaultOperations.addArgument("-m", "--meta")
    .help("Retrieve vault metadata")
    .metavar("<name>");

    vaultOperations.addArgument("-c", "--create")
    .help("Create vault")
    .metavar("<name>");

    vaultOperations.addArgument("--delete")
    .help("Delete vault")
    .metavar("<name>");

    Subparser archive = commands.addParser("archive").aliases("a").help("Archive operations");
    ArgumentGroup requiredArchiveArgs = archive.addArgumentGroup("required arguments");
    MutuallyExclusiveGroup archiveOperations =
        archive.addMutuallyExclusiveGroup("archive operations");
    /*
    archive.addArgument("--credentials")
    .help("Location of AWS credentials")
    .metavar("<file>");
     */

    archive.addArgument("--description")
    .help("Description of archive (default: File name)")
    .metavar("<description>");

    /*
    requiredArchiveArgs.addArgument("-e","--endpoint")
    .help("Set endpoint "+Arrays.asList(Endpoints).toString())
    .choices(new CaseInsensitiveStringChoice(Endpoints))
    .metavar("<region>")
    .required(true);
     */

    requiredArchiveArgs.addArgument("-v","--vault")
    .help("Vault name for job operations")
    .metavar("<name>")
    .required(true);

    archiveOperations.addArgument("--upload")
    .help("Upload archive")
    .metavar("<file>");

    archiveOperations.addArgument("--abort")
    .help("Abort archive upload operation")
    .metavar("<uploadId>");

    archiveOperations.addArgument("--delete")
    .help("Delete archive")
    .metavar("<archiveId>");

    Subparser jobs = commands.addParser("job").aliases("j").help("Job operations");
    ArgumentGroup requiredJobArgs = jobs.addArgumentGroup("required arguments");
    ArgumentGroup jobOperations = jobs.addArgumentGroup("job operations");

    /*
    jobs.addArgument("--credentials")
    .help("Location of AWS credentials")
    .metavar("<file>");

    requiredJobArgs.addArgument("-e","--endpoint")
    .help("Set endpoint "+Arrays.asList(Endpoints).toString())
    .choices(new CaseInsensitiveStringChoice(Endpoints))
    .metavar("<region>")
    .required(true);
     */

    requiredJobArgs.addArgument("-v","--vault")
    .help("Vault name for job operations")
    .metavar("<name>")
    .required(true);

    jobOperations.addArgument("-l", "--list")
    .help("List jobs "+Arrays.asList(Job_List_Options).toString())
    .choices(new CaseSensitiveStringChoice(Job_List_Options))
    .metavar("<selection>");

    @SuppressWarnings("unused")
    Subparser cache = commands.addParser("cache")
    .aliases("c")
    .help("Display cache")
    .defaultHelp(false);

    return parser;
  }
}
