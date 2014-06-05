/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacialbackup.operations.archive;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.operations.GlacierOperation;

/**
 * TODO: Delete archive glacier operation.
 */
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
