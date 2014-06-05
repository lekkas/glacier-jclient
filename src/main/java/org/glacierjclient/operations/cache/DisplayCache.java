/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierjclient.operations.cache;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GenericOperation;
import org.glacierjclient.operations.cache.model.LocalCache;

/*
 * Display cache information
 */
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
