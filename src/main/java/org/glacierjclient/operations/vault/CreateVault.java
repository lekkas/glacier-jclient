/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.vault;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.glacierjclient.operations.cache.model.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Create vault operation.
 */
public class CreateVault extends GlacierOperation {

  private final Logger log = LoggerFactory.getLogger(CreateVault.class);

  public CreateVault(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    try {
      initClient();
      String vaultName = argOpts.getString("create");
      CreateVaultResult result = createVault(vaultName);

      log.debug("Create vault operation response: "+result.toString());
      log.info("Vault created successfully: " + result.getLocation());

      /*
       * Request metadata and add it to the cache
       */
      RequestVaultMetadata metaOperation = new RequestVaultMetadata(argOpts);
      DescribeVaultResult metaResult = metaOperation.requestVaultMetadata(vaultName);

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
   * @param vaultName
   */
  public CreateVaultResult createVault(String vaultName) {
    AmazonGlacierClient client = getAWSClient();
    CreateVaultRequest request = new CreateVaultRequest().withVaultName(vaultName);
    CreateVaultResult result = client.createVault(request);
    return result;
  }
}
