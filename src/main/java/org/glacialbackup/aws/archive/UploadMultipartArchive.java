/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.archive;

import org.glacialbackup.aws.GlacierOperation;

import net.sourceforge.argparse4j.inf.Namespace;


public class UploadMultipartArchive extends GlacierOperation {

  public UploadMultipartArchive(Namespace argOpts) {
    super(argOpts);
    // TODO Auto-generated constructor stub
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
