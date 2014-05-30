/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import java.util.List;

import org.glacialbackup.aws.GlacierOperation;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;

import net.sourceforge.argparse4j.inf.Namespace;


public class ListVaults extends GlacierOperation {

  public ListVaults(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    requestVaultList(loadCredentials(
      argOpts.getString("credentials")),
      getEndpoint(argOpts.getString("endpoint")));
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
          argOpts.getBoolean("list") == true;
  }
  
  /**
   * This operation requests the list of all vaults owned by the calling user’s account
   * TODO: Add marker support for lists of over 1000 items.
   * 
   * @param credentials
   * @param endpoint
   */
  public static void requestVaultList(AWSCredentials credentials, String endpoint) {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
  
    try {
      ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
      ListVaultsResult listVaultsResult = client.listVaults(listVaultsRequest);
      
      log.debug("requestVaultList() response: "+listVaultsResult.toString());
      List<DescribeVaultOutput> vaultList = listVaultsResult.getVaultList();
      StringBuilder buf = new StringBuilder();
      buf.append("Describing all vaults (vault list):\n");
      for (DescribeVaultOutput vault : vaultList) {
          buf.append("\nCreationDate: " + vault.getCreationDate());
          buf.append("\nLastInventoryDate: " + vault.getLastInventoryDate());
          buf.append("\nNumberOfArchives: " + vault.getNumberOfArchives());
          buf.append("\nSizeInBytes: " + vault.getSizeInBytes());
          buf.append("\nVaultARN: " + vault.getVaultARN()); 
          buf.append("\nVaultName: " + vault.getVaultName());
          buf.append("\n");
      }
      System.out.print(buf.toString());
    } catch(AmazonServiceException ex) {
    	  log.error("AmazonServiceException: "+ex.getMessage());
        System.exit(1);
    } catch(AmazonClientException ex) {
        log.error("AmazonClientException: "+ex.getMessage());
        System.exit(1);
    }
  }
}
