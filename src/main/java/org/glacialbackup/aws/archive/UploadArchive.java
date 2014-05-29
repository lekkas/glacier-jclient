/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.archive;

import org.glacialbackup.aws.GlacierOperation;

import net.sourceforge.argparse4j.inf.Namespace;


public class UploadArchive extends GlacierOperation {

  public UploadArchive(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    System.out.println("Executing "+this.getClass().getName());
    
  }

  @Override
  public boolean valid() {
    return false;
  }

}
