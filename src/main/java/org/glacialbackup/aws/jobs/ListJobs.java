/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.jobs;

import java.util.List;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;

public class ListJobs extends GlacierOperation {

  public ListJobs(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    
    try {
      String listOption = argOpts.getString("list");
      String vaultName = argOpts.getString("vault");
      
      ListJobsResult listJobsResult = listJobs(loadCredentials(argOpts.getString("credentials")),
          getEndpoint(argOpts.getString("endpoint")),
          argOpts.getString("vault"),
          argOpts.getString("list"));
      
      log.debug("listJobs() response for '"+vaultName+"' ("+listOption+"): "+
          listJobsResult.toString());
      
      List<GlacierJobDescription> jobList = listJobsResult.getJobList();
      StringBuilder buf = new StringBuilder();
      for(GlacierJobDescription job : jobList) {
        buf.append("\nJobId: "+job.getJobId());
        buf.append("\nAction: "+job.getAction());
        buf.append("\nStatusCode: "+job.getStatusCode());
        buf.append("\nStatusMessage: "+job.getStatusMessage());
        buf.append("\nCompleted: "+job.isCompleted());
        buf.append("\nCreationDate: "+job.getCreationDate());
        buf.append("\nCompletionDate: "+job.getCompletionDate());
        buf.append("\nJobDescription: "+job.getJobDescription());

        buf.append("\nArchiveId: "+job.getArchiveId());
        buf.append("\nArchiveSizeInBytes: "+job.getArchiveSizeInBytes());
        buf.append("\nArchiveSHA256TreeHash: "+job.getArchiveSHA256TreeHash());
        
        buf.append("\nInventorySizeInBytes: "+job.getInventorySizeInBytes());
        buf.append("\nRetrievalByteRange: "+job.getRetrievalByteRange());
        buf.append("\nSHA256TreeHash: "+job.getSHA256TreeHash());
        buf.append("\nSNSTopic: "+job.getSNSTopic());
        buf.append("\nVaultARN: "+job.getVaultARN());
        buf.append("\n");
      }
      System.out.print(buf.toString());
      if(jobList.size() == 0)
        log.info("No jobs found for vault '"+vaultName+"' ("+listOption+")");
      
    } catch(AmazonServiceException ex) {
      log.error("AmazonServiceException: "+ex.getMessage());
      System.exit(1);
    } catch(AmazonClientException ex) {
      log.error("AmazonClientException: "+ex.getMessage());
      System.exit(1);
    }
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("job") && 
        argOpts.getString("list") != null &&
        argOpts.getString("vault") != null;
  }

  /**
   * This operation lists jobs for a vault including jobs that are in-progress and jobs that have 
   * recently finished.
   * 
   * To retrieve an archive or retrieve a vault inventory from Amazon Glacier, you first initiate a 
   * job, and after the job completes, you download the data. For an archive retrieval, the output 
   * is the archive data, and for an inventory retrieval, it is the inventory list. The List Job 
   * operation returns a list of these jobs sorted by job initiation time.
   * 
   * The List Jobs operation supports pagination. By default, this operation returns up to 1,000 
   * jobs in the response.  You should always check the response marker field for a marker at 
   * which to continue the list (TODO); if there are no more items the marker field is null.
   * To return a list of jobs that begins at a specific job, set the marker request parameter to 
   * the value you obtained from a previous List Jobs request. You can also limit the number of 
   * jobs returned in the response by specifying the limit parameter in the request.
   * 
   * Additionally, you can filter the jobs list returned by specifying an optional statuscode 
   * (InProgress, Succeeded, or Failed) and completed (true, false) parameter. The statuscode 
   * allows you to specify that only jobs that match a specified status are returned. The completed 
   * parameter allows you to specify that only jobs in specific completion state are returned.
   * 
   * @param credentials
   * @param endpoint
   * @param vaultName
   */
  public static ListJobsResult listJobs(AWSCredentials credentials, String endpoint, String vaultName,
      String listOption) {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);

    String statuscode = listOption.equals("All")?null:listOption;
    ListJobsRequest listJobsRequest = new ListJobsRequest()
        .withVaultName(vaultName)
        .withStatuscode(statuscode);
    ListJobsResult listJobsResult = client.listJobs(listJobsRequest);
    
    return listJobsResult;
  }
}
