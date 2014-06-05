/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierjclient.operations.jobs;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * List vault jobs.
 * 
 * This operation lists in-progress and recently failed/succeeded inventory/archive
 * retrieval vault jobs.
 *
 */
public class ListJobs extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(ListJobs.class);
  
  public ListJobs(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    
    try {
      String vaultName = argOpts.getString("vault");
      String listOption = argOpts.getString("list");
      ListJobsResult listJobsResult = listJobs(vaultName, listOption);
      
      log.debug("listJobs() response for '"+vaultName+"' ("+listOption+"): "+
          listJobsResult.toString());
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(listJobsResult, ListJobsResult.class);
      System.out.println(json);
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
   * @param vaultName
   * @param listOption
   */
  public ListJobsResult listJobs(String vaultName, String listOption) {
    AmazonGlacierClient client = getAWSClient();
    String statuscode = listOption.equals("All")?null:listOption;
    ListJobsRequest listJobsRequest = new ListJobsRequest()
        .withVaultName(vaultName)
        .withStatuscode(statuscode);
    ListJobsResult listJobsResult = client.listJobs(listJobsRequest);
    return listJobsResult;
  }
}
