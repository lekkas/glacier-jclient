/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import org.glacialbackup.aws.GlacierOperation;

import net.sourceforge.argparse4j.inf.Namespace;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;

public class CreateVault extends GlacierOperation {

  public CreateVault(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    createVault(loadCredentials(argOpts.getString("credentials")),
        getEndpoint(argOpts.getString("endpoint")),
        argOpts.getString("create"));
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
  public static void createVault(AWSCredentials credentials, String endpoint, String vaultName) {

    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    try {
      CreateVaultRequest request = new CreateVaultRequest().withVaultName(vaultName);
      CreateVaultResult result = client.createVault(request);
      log.debug("createVault() response: "+result.toString());
      log.info("Vault created successfully: " + result.getLocation());
    } catch(AmazonServiceException ex) {
      log.error("AmazonServiceException: "+ex.getMessage());
      System.exit(1);
    } catch(AmazonClientException ex) {
      log.error("AmazonClientException: "+ex.getMessage());
      System.exit(1);
    }
  }
}
