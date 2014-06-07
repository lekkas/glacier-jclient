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
    throw new UnsupportedOperationException();
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
   *          Byte range for archive retrieval. Must be null when requesting
   *          vault inventories.
   * @return {@link GetJobOutputResult} object. The result body will contain
   *         either the inventory of a vault or archive part bytes, depending on
   *         the job.
   */
  public GetJobOutputResult getJobOutput(String vaultName, String jobId, String range) {
    AmazonGlacierClient client = getAWSClient();
    GetJobOutputRequest jobOutputRequest =
        new GetJobOutputRequest().withVaultName(vaultName).withJobId(jobId).withRange(range);

    GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);
    return jobOutputResult;
  }

  /**
   * 
   * @param jobOutputResult
   *          {@link GetJobOutputResult} object.
   * @return JSON string representing the returned vault inventory
   * @throws IOException
   */
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
