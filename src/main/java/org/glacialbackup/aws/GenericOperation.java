/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws;

import net.sourceforge.argparse4j.inf.Namespace;

public abstract class GenericOperation {
  
  protected final Namespace argOpts;
  
  public GenericOperation(Namespace argOpts) {
    this.argOpts = argOpts;
  }
  
  public abstract void exec();
  public abstract boolean valid();
}
