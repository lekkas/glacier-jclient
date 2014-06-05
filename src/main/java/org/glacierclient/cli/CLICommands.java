/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierclient.cli;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierclient.operations.GenericOperation;
import org.glacierclient.operations.archive.AbortMultipartUploadArchive;
import org.glacierclient.operations.archive.MultipartUploadArchive;
import org.glacierclient.operations.cache.DisplayCache;
import org.glacierclient.operations.jobs.ListJobs;
import org.glacierclient.operations.vault.CreateVault;
import org.glacierclient.operations.vault.DeleteVault;
import org.glacierclient.operations.vault.ListVaults;
import org.glacierclient.operations.vault.RequestVaultInventory;
import org.glacierclient.operations.vault.RequestVaultMetadata;

public class CLICommands {

  public static void findAndExec(Namespace argOpts) {
      List<GenericOperation> operations = new ArrayList<GenericOperation>();
      
      /* Vault Operations */
      operations.add(new CreateVault(argOpts));
      operations.add(new DeleteVault(argOpts));
      operations.add(new RequestVaultInventory(argOpts));
      operations.add(new RequestVaultMetadata(argOpts));
      operations.add(new ListVaults(argOpts));
      
      /* Archive Operations */
      operations.add(new MultipartUploadArchive(argOpts));
      operations.add(new AbortMultipartUploadArchive(argOpts));
      
      /* TODO: Merge Job Operations with vault operations */
      operations.add(new ListJobs(argOpts));
      
      /* Cache operations */
      operations.add(new DisplayCache(argOpts));
      
      
      for(GenericOperation op : operations) 
        if(op.valid())
          op.exec();
  }
}
