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
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RequestVaultMetadata extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(RequestVaultMetadata.class);

  public RequestVaultMetadata(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    try {
      initClient();
      String vaultName = argOpts.getString("meta");
      DescribeVaultResult result = requestVaultMetadata(vaultName);

      log.debug("Vault metadata for '"+vaultName+"': "+result.toString());
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(result);
      LocalCache.loadCache().addVaultInfo(json);
      System.out.println(json);
    } catch(AmazonServiceException ex) {
      log.error("AmazonServiceException: "+ex.getMessage());
      System.exit(1);
    } catch(AmazonClientException ex) {
      log.error("AmazonClientException: "+ex.getMessage());
      System.exit(1);
    }
  }

  /**
   * This operation returns information about a vault, including the vault Amazon Resource Name
   * (ARN), the date the vault was created, the number of archives contained within the vault, and
   * the total size of all the archives in the vault. The number of archives and their total size
   * are as of the last vault inventory Amazon Glacier generated (see Working with Vaults in Amazon
   * Glacier). Amazon Glacier generates vault inventories approximately daily. This means that if
   * you add or remove an archive from a vault, and then immediately send a Describe Vault request,
   * the response might not reflect the changes.
   * 
   * @param vaultName
   */
  public DescribeVaultResult requestVaultMetadata(String vaultName) {
    AmazonGlacierClient client = getAWSClient();
    DescribeVaultRequest request = new DescribeVaultRequest().withVaultName(vaultName);
    DescribeVaultResult result = client.describeVault(request);
    return result;
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") &&
        argOpts.getString("meta") != null;
  }
}
