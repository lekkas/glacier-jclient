/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierclient.operations.jobs;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierclient.operations.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  public static Logger log = LoggerFactory.getLogger(InitiateJob.class);

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
  
  public InitiateJobResult initiateJob(String vaultName, InitJobType jobType) {
    
    AmazonGlacierClient client = getAWSClient();
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
