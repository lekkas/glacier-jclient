/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.cli;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.vault.CreateVault;
import org.glacialbackup.aws.vault.DeleteVault;
import org.glacialbackup.aws.vault.RequestVaultInventory;
import org.glacialbackup.aws.vault.RequestVaultMetadata;
import org.glacialbackup.aws.vault.ListVaults;

public class CLICommands {

  public static void findAndExec(Namespace argOpts) {
      List<GlacierOperation> operations = new ArrayList<GlacierOperation>();
      operations.add(new CreateVault(argOpts));
      operations.add(new DeleteVault(argOpts));
      operations.add(new RequestVaultInventory(argOpts));
      operations.add(new RequestVaultMetadata(argOpts));
      operations.add(new ListVaults(argOpts));
      
      for(GlacierOperation op : operations) 
        if(op.valid())
          op.exec();
  }
}
