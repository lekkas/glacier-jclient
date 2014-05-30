/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.jobs.ListJobs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.ListJobsResult;

import net.sourceforge.argparse4j.inf.Namespace;


public class RequestVaultInventory extends GlacierOperation {

  private enum JobResult {
    INPROGRESS,
    SUCCEEDED,
    FAILED,
    NONEXISTANT
  };
  public RequestVaultInventory(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    requestVaultInventory(loadCredentials(argOpts.getString("credentials")),
        getEndpoint(argOpts.getString("endpoint")),
        argOpts.getString("inventory"));
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
            argOpts.getString("inventory") != null;
  }

  public static void requestVaultInventory(AWSCredentials credentials, String endpoint, 
      String vaultName) {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    try {
      /* 
       * 1. Request vault metadata to get vault ARN 
       */
      DescribeVaultResult metadata = RequestVaultMetadata.requestVaultMetadata(credentials, 
          endpoint, vaultName);
      
      /* 
       * 2. Request list of jobs for the specific vault 
       */
      ListJobsResult listJobsResult = ListJobs.listJobs(credentials, endpoint, 
          vaultName, "All");
      
      /*
       * 3. Check if an inventory request has already been submitted
       */

      JobResult jobResult = JobResult.NONEXISTANT;
      String jobId = null;
      List<GlacierJobDescription> jobList = listJobsResult.getJobList();
      for(GlacierJobDescription job : jobList) {
        if (job.getVaultARN().equals(metadata.getVaultARN()) &&
            job.getAction().equals("InventoryRetrieval") &&
            job.getStatusCode().equals("InProgress")) {
          jobResult = JobResult.INPROGRESS;
          jobId = job.getJobId();
          break;
        }
        else if (job.getVaultARN().equals(metadata.getVaultARN()) &&
            job.getAction().equals("InventoryRetrieval") &&
            job.getStatusCode().equals("Succeeded")) {
          jobResult = JobResult.SUCCEEDED;
          jobId = job.getJobId();
          break;
        }
      }
      
      switch(jobResult) {
        case INPROGRESS: {
          log.info("A job is already in progress to retrieve the inventory of '"+vaultName+"' with "+
              "jobId: "+jobId);
          break;
        }
        case SUCCEEDED: {
          log.info("Retrieving inventory for vault '"+vaultName+"':");
          GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest()
          .withVaultName(vaultName)
          .withJobId(jobId);
          GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);
          BufferedReader in = new BufferedReader(new InputStreamReader(jobOutputResult.getBody()));
          StringBuilder buf = new StringBuilder();
          String line = null;
          while((line = in.readLine()) != null) {
            buf.append(line);
          }
          in.close();
          log.debug("Retrieved vault inventory: "+buf.toString());
          break;
        }
        case FAILED: 
        case NONEXISTANT: {
          InitiateJobRequest inventoryJobRequest = 
              new InitiateJobRequest()
                .withVaultName(vaultName)
                .withJobParameters(
                    new JobParameters()
                      .withType("inventory-retrieval")
                );
          InitiateJobResult initJobResult = client.initiateJob(inventoryJobRequest);
          log.debug("requestVaultInventory() response: "+initJobResult.toString());
          log.info("Created archive retrieval job with id "+initJobResult.getJobId());
          break;
        }
      }
    } catch(AmazonServiceException ex) {
      log.error("AmazonServiceException: "+ex.getMessage());
      System.exit(1);
    } catch(AmazonClientException ex) {
      log.error("AmazonClientException: "+ex.getMessage());
      System.exit(1);
    } catch(IOException ex) {
      log.error("IOException: "+ex.getMessage());
    }
    
    
  }
}
