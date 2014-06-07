/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.cache;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GenericOperation;
import org.glacierjclient.operations.cache.model.LocalCache;

/**
 * Display cache operation.
 */
public class DisplayCache extends GenericOperation {

  public DisplayCache(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    LocalCache cache = LocalCache.loadCache();
    System.out.println("--- Vaults --- ");
    cache.prettyPrintVaults();
    System.out.println("--- In progress uploads --- ");
    cache.prettyPrintInProgressUploads();
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("cache");
  }
}
