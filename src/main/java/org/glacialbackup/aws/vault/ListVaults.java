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
    listVaults(loadCredentials(
        argOpts.getString("credentials")),
        getEndpoint(argOpts.getString("endpoint")));
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
            argOpts.getBoolean("list") == true;
  }
  
  public static void listVaults(AWSCredentials credentials, String endpoint) {
    log.info("listVaults()");
    
    AmazonGlacierClient client;
    client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    try {
    	ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
        ListVaultsResult listVaultsResult = client.listVaults(listVaultsRequest);
        
        log.debug("Retrieved vault list: "+listVaultsResult.toString());
        List<DescribeVaultOutput> vaultList = listVaultsResult.getVaultList();
        System.out.println("Describing all vaults (vault list):");
        for (DescribeVaultOutput vault : vaultList) {
            System.out.println(
                    "\nCreationDate: " + vault.getCreationDate() +
                    "\nLastInventoryDate: " + vault.getLastInventoryDate() +
                    "\nNumberOfArchives: " + vault.getNumberOfArchives() + 
                    "\nSizeInBytes: " + vault.getSizeInBytes() + 
                    "\nVaultARN: " + vault.getVaultARN() + 
                    "\nVaultName: " + vault.getVaultName());
        }
    } catch(AmazonServiceException ex) {
    	log.error(ex.getMessage());
        System.exit(1);
    } catch(AmazonClientException ex) {
        log.error("AmazonClientException: "+ex.getMessage());
        System.exit(1);
    }
  }
}
