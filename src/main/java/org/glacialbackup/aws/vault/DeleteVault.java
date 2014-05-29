/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DeleteVaultRequest;

public class DeleteVault extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(DeleteVault.class);
  
  public DeleteVault(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    deleteVault(loadCredentials(
        argOpts.getString("credentials")),
        getEndpoint(argOpts.getString("endpoint")),
        argOpts.getString("delete"));
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
            argOpts.getString("delete") != null;
  }

  public static void deleteVault(AWSCredentials credentials, String endpoint, String vaultName) {
    log.info("deleteVault()");
    
    AmazonGlacierClient client;
    client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    DeleteVaultRequest request = new DeleteVaultRequest().withVaultName(vaultName);
    client.deleteVault(request);
    System.out.println("Deleted vault: " + vaultName);
  }
  
}
