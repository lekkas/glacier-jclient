/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;

public class GetJobOutput extends GlacierOperation {

  public GetJobOutput(Namespace argOpts) {
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
  
  /**
   * Get result of submitted job
   * 
   * @param credentials AWS credentials
   * @param endpoint AWS Glacier endpoint 
   * @param vaultName The target vault
   * @param jobId Job ID
   * @param range Byte range for archive retrieval. MUST be null when requesting inventory
   * archives.
   * @return
   */
  public static GetJobOutputResult getJobOutput(AWSCredentials credentials, String endpoint, 
      String vaultName, String jobId, String range) {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest()
        .withVaultName(vaultName)
        .withJobId(jobId)
        .withRange(range);

    GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);
    return jobOutputResult;
  }
  
  public static String getInventoryFromJobResult(GetJobOutputResult jobOutputResult) 
      throws IOException {
    
    BufferedReader in = new BufferedReader(new InputStreamReader(jobOutputResult.getBody()));
    StringBuilder buf = new StringBuilder();
    String line = null;
    while((line = in.readLine()) != null) {
      buf.append(line);
    }
    in.close();
    return buf.toString();
  }
}
