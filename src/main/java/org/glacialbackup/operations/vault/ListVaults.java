/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.operations.vault;

import org.glacialbackup.cache.model.LocalCache;
import org.glacialbackup.operations.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sourceforge.argparse4j.inf.Namespace;


public class ListVaults extends GlacierOperation {

  private final Logger log = LoggerFactory.getLogger(ListVaults.class);
  
  public ListVaults(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    try {
      ListVaultsResult listVaultsResult = requestVaultList();
      
      log.debug("requestVaultList() response: "+listVaultsResult.toString());
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(listVaultsResult.getVaultList());
      LocalCache.loadCache().addVaultInfoList(json);
      
      System.out.println(json);
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
          argOpts.getBoolean("list") == true;
  }
  
  /**
   * This operation requests the list of all vaults owned by the calling user’s account
   * TODO: Add marker support for lists of over 1000 items.
   */
  public ListVaultsResult requestVaultList() {
    AmazonGlacierClient client = getAWSClient();
    ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
    ListVaultsResult listVaultsResult = client.listVaults(listVaultsRequest);
    return listVaultsResult;
  }
}
