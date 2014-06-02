/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.cache.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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
    try {
      String vaultName = argOpts.getString("delete");
      AWSCredentials credentials = loadCredentials(argOpts.getString("credentials"));
      String endpoint = getEndpoint(argOpts.getString("endpoint"));
      AmazonGlacierClient client = new AmazonGlacierClient(credentials);
      client.setEndpoint(endpoint);
      
      deleteVault(client, vaultName);
      
      log.info("Deleted vault '" + vaultName+"'");
      
      /*
       * Remove vault from cache
       */
      LocalCache.loadCache().deleteVaultInfo(vaultName);
      
    } catch(AmazonServiceException ex) {
      log.error("AmazonServiceException: "+ex.getMessage());
      System.exit(1);
    } catch(AmazonClientException ex) {
      log.error("AmazonClientException: "+ex.getMessage());
      System.exit(1);
    }
    
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
            argOpts.getString("delete") != null;
  }

  /**
   * This operation deletes a vault. Amazon Glacier will delete a vault only if there are no 
   * archives in the vault as per the last inventory and there have been no writes to the vault 
   * since the last inventory. If either of these conditions is not satisfied, the vault deletion 
   * fails (that is, the vault is not removed) and Amazon Glacier returns an error.
   * 
   * @param credentials
   * @param endpoint
   * @param vaultName
   */
  public static void deleteVault(AmazonGlacierClient client, String vaultName) {
    DeleteVaultRequest request = new DeleteVaultRequest().withVaultName(vaultName);
    client.deleteVault(request);
  }
}
