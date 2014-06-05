/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.operations.cache;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.cache.model.LocalCache;
import org.glacialbackup.operations.GenericOperation;

public class DisplayCache extends GenericOperation {

  public DisplayCache(Namespace argOpts) {
    super(argOpts);
  }

  public void exec() {
    System.out.println("--- Vaults --- ");
    LocalCache.loadCache().prettyPrintVaults();
    System.out.println("--- In progress uploads --- ");
    LocalCache.loadCache().prettyPrintInProgressUploads();
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("cache");
  }
}
