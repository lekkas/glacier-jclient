/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.cache.LocalCache;

import net.sourceforge.argparse4j.inf.Namespace;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CreateVault extends GlacierOperation {

  public CreateVault(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    try {
      String vaultName = argOpts.getString("create");
      String endpoint = getEndpoint(argOpts.getString("endpoint"));
      AWSCredentials credentials = loadCredentials(argOpts.getString("credentials"));
      AmazonGlacierClient client = new AmazonGlacierClient(credentials);
      client.setEndpoint(endpoint);

      CreateVaultResult result = createVault(client, vaultName);
      
      log.debug("Create vault operation response: "+result.toString());
      log.info("Vault created successfully: " + result.getLocation());
      
      /*
       * Request metadata and add it to the cache
       */
      DescribeVaultResult metaResult = RequestVaultMetadata.requestVaultMetadata(client, vaultName);
    
      log.debug("Vault metadata for '"+argOpts.getString("create")+"': "+result.toString());
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(metaResult);
      LocalCache.loadCache().addVaultInfo(json);
      
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
            argOpts.getString("create") != null;
  }
  
  /**
   * Creates vault with vaultName. This operation is idempotent, you can send the same request 
   * multiple times and it has no further effect after the first time Amazon Glacier creates 
   * the specified vault.
   * 
   * @param credentials
   * @param endpoint
   * @param vaultName
   */
  public static CreateVaultResult createVault(AmazonGlacierClient client, String vaultName) {
    CreateVaultRequest request = new CreateVaultRequest().withVaultName(vaultName);
    CreateVaultResult result = client.createVault(request);
    return result;
  }
}
