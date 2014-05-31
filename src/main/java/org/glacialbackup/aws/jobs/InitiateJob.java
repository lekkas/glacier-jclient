/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.jobs;

import net.sourceforge.argparse4j.inf.Namespace;
import org.glacialbackup.aws.GlacierOperation;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;

/**
 * The 'createJob' operation is not invoked directly from the command line; it is only wrapped
 * in a 'GlacierOperation' object to be consistent with the rest of the operations.
 *  
 */
public class InitiateJob extends GlacierOperation {

  public static enum InitJobType {
    INVENTORY_RETRIEVAL {
      public String toString() { return "inventory-retrieval"; }
    },
    ARCHIVE_RETRIEVAL {
      public String toString() { return "archive-retrieval"; }
    }
  }
  
  public InitiateJob(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    log.info("Execution of initiateJob() from its wrapper class is not supported");
  }

  @Override
  public boolean valid() {
    return false;
  }
  
  public static InitiateJobResult initiateJob(AWSCredentials credentials, String endpoint, 
      String vaultName, InitJobType jobType) {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    InitiateJobRequest inventoryJobRequest = 
        new InitiateJobRequest()
          .withVaultName(vaultName)
          .withJobParameters(
              new JobParameters()
                .withType(jobType.toString())
          );
    InitiateJobResult initJobResult = client.initiateJob(inventoryJobRequest);
    return initJobResult;
  }
}
