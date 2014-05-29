/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.archive;

import org.glacialbackup.aws.GlacierOperation;

import net.sourceforge.argparse4j.inf.Namespace;


public class DeleteArchive extends GlacierOperation {

  public DeleteArchive(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {

  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("archive") && 
        argOpts.getString("delete") != null;
  }

}
