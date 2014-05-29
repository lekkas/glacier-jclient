/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;

public class RequestVaultMetadata extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(RequestVaultMetadata.class);
  
  public RequestVaultMetadata(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    getVaultMetadata(loadCredentials(
        argOpts.getString("credentials")),
        getEndpoint(argOpts.getString("endpoint")),
        argOpts.getString("meta"));
  }
  
  public static void getVaultMetadata(AWSCredentials credentials, String endpoint, 
      String vaultName) {
    AmazonGlacierClient client;
    client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    try {
      DescribeVaultRequest request = new DescribeVaultRequest().withVaultName(vaultName);
      DescribeVaultResult result = client.describeVault(request);
      log.info("Retrieved vault '"+vaultName+"' metadata: "+result.toString());
      
      System.out.print(
          "\nCreationDate: " + result.getCreationDate() +
          "\nLastInventoryDate: " + result.getLastInventoryDate() +
          "\nNumberOfArchives: " + result.getNumberOfArchives() + 
          "\nSizeInBytes: " + result.getSizeInBytes() + 
          "\nVaultARN: " + result.getVaultARN() + 
          "\nVaultName: " + result.getVaultName() +
          "\n");
      
      /* TODO: Use JSON objects from result.toString() for the intermediate layer between
       * AWS responses and underlying DB.  
       */
      
      /*
       * AmazonServiceException extended Exceptions :
       * 
       * AccessDeniedException
       * ExpiredTokenException
       * InvalidParameterValueException 
       * LimitExceededException 
       * MissingParameterValueException
       * ResourceNotFoundException
       * RequestTimeoutException (TODO: Recoverable)
       * ServiceUnavailableException (TODO: Recoverable)
       * 
       * 
       * Exceptions that are not encapsulated as Java exceptions but are
       * returned as error code in an AmazonServiceException: 
       * 
       * MissingAuthenticationTokenException
       * 
       * 
       * TODO: Check if these exceptions are returned as AmazonServiceEception
       * error codes: 
       * 
       * BadRequest
       * InvalidSignatureException
       * SerializationException
       * ThrottlingException (TODO: Recoverable)
       * UnrecognizedClientException
       * 
       */
    } catch(AmazonServiceException ex) {
      log.error(ex.getMessage());
      System.exit(1);
    }
    
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
            argOpts.getString("meta") != null;
  }
}
