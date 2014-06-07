/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.cli;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GenericOperation;
import org.glacierjclient.operations.archive.AbortMultipartUploadArchive;
import org.glacierjclient.operations.archive.DeleteArchive;
import org.glacierjclient.operations.archive.MultipartUploadArchive;
import org.glacierjclient.operations.cache.DisplayCache;
import org.glacierjclient.operations.jobs.ListJobs;
import org.glacierjclient.operations.vault.CreateVault;
import org.glacierjclient.operations.vault.DeleteVault;
import org.glacierjclient.operations.vault.ListVaults;
import org.glacierjclient.operations.vault.RequestVaultInventory;
import org.glacierjclient.operations.vault.RequestVaultMetadata;

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
    operations.add(new DeleteArchive(argOpts));

    /* TODO: Merge Job Operations with vault operations */
    operations.add(new ListJobs(argOpts));

    /* Cache operations */
    operations.add(new DisplayCache(argOpts));

    for (GenericOperation op : operations) {
      if (op.valid()) {
        op.exec();
      }
    }
  }
}
