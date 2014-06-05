/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierclient.operations;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Generic operation. This class acts as a wrapper of the actual AWS Glacier operations.
 * 
 */
public abstract class GenericOperation {

  protected final Namespace argOpts;

  public GenericOperation(Namespace argOpts) {
    this.argOpts = argOpts;
  }

  /**
   * Execute operation. This function
   */
  public abstract void exec();

  /**
   * Check if the command line arguments <i>argOpts</i> are valid for the specific operation.
   * 
   * @return true if the command line arguments are valid, false otherwise
   */
  public abstract boolean valid();
}
