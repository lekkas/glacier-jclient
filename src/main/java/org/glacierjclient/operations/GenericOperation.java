/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Generic operation.
 */
public abstract class GenericOperation {

  protected final Namespace argOpts;

  public GenericOperation(Namespace argOpts) {
    this.argOpts = argOpts;
  }

  /**
   * Execute operation.
   */
  public abstract void exec();

  /**
   * Check if the command line arguments <i>argOpts</i> are valid for the
   * specific operation.
   * 
   * @return true if the command line arguments are valid, false otherwise
   */
  public abstract boolean valid();
}
