/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.jobs;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;

/**
 * Initiate job operation.
 */
public class InitiateJob extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(InitiateJob.class);

  public static enum InitJobType {
    INVENTORY_RETRIEVAL {
      @Override
      public String toString() {
        return "inventory-retrieval";
      }
    },
    ARCHIVE_RETRIEVAL {
      @Override
      public String toString() {
        return "archive-retrieval";
      }
    }
  }

  public InitiateJob(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean valid() {
    return false;
  }

  /**
   * Create vault job.
   * 
   * @param vaultName
   * @param jobType
   *          {@link InitJobType} value
   * 
   * @return {@link InitiateJobResult} object.
   */
  public InitiateJobResult initiateJob(String vaultName, InitJobType jobType) {
    AmazonGlacierClient client = getAWSClient();
    InitiateJobRequest inventoryJobRequest =
        new InitiateJobRequest().withVaultName(vaultName).withJobParameters(
            new JobParameters().withType(jobType.toString()));
    InitiateJobResult initJobResult = client.initiateJob(inventoryJobRequest);
    return initJobResult;
  }
}
