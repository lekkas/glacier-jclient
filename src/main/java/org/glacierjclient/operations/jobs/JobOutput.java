/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;

public class JobOutput extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(JobOutput.class);

  public JobOutput(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
  }

  @Override
  public boolean valid() {
    return false;
  }

  /**
   * Get result of submitted job
   * 
   * @param vaultName
   * @param jobId
   *          Job ID
   * @param range
   *          Byte range for archive retrieval. MUST be null when requesting
   *          inventory archives.
   * @return
   */
  public GetJobOutputResult getJobOutput(String vaultName, String jobId, String range) {
    initClient();
    AmazonGlacierClient client = getAWSClient();
    GetJobOutputRequest jobOutputRequest =
        new GetJobOutputRequest().withVaultName(vaultName).withJobId(jobId).withRange(range);

    GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);
    return jobOutputResult;
  }

  public String getJSONInventoryFromJobResult(GetJobOutputResult jobOutputResult)
      throws IOException {

    BufferedReader in = new BufferedReader(new InputStreamReader(jobOutputResult.getBody()));
    StringBuilder buf = new StringBuilder();
    String line = null;
    while ((line = in.readLine()) != null) {
      buf.append(line);
    }
    in.close();
    return buf.toString();
  }
}
