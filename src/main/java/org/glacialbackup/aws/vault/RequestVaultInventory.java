/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.jobs.GetJobOutput;
import org.glacialbackup.aws.jobs.InitiateJob;
import org.glacialbackup.aws.jobs.InitiateJob.InitJobType;
import org.glacialbackup.aws.jobs.ListJobs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.ListJobsResult;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * TODO: Print the information in a pretty way for the user
 * TODO: Inform user of ETA from the creationDate field of the oldest submitted job
 */
public class RequestVaultInventory extends GlacierOperation {

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
       * 1. Request list of jobs for the specific vault 
       */
      ListJobsResult listJobsResult = ListJobs.listJobs(credentials, endpoint, 
          vaultName, "All");
      
      /*
       * 2. Check if the status of any pending inventory requests.
       */
      List<GlacierJobDescription> jobList = listJobsResult.getJobList();
      List<GlacierJobDescription> succeededJobs = new ArrayList<GlacierJobDescription>();
      List<GlacierJobDescription> inProgressJobs = new ArrayList<GlacierJobDescription>();
      List<GlacierJobDescription> failedJobs = new ArrayList<GlacierJobDescription>();
      
      for(GlacierJobDescription job : jobList) {
        /* Ignore archive retrieval jobs */
        if(!job.getAction().equals("InventoryRetrieval"))
          continue;
        
        if (job.getStatusCode().equals("Succeeded")) { 
          succeededJobs.add(job);
        }
        else if (job.getStatusCode().equals("InProgress")) {
          inProgressJobs.add(job);
        }
        else {
          failedJobs.add(job);
        }
      }
      
      /*
       * Create a new job if there are neither in progress nor completed jobs.
       */
      if(inProgressJobs.size() == 0 && succeededJobs.size() == 0) {
        InitiateJobResult initJobResult = InitiateJob.initiateJob(credentials, endpoint, 
              vaultName, InitJobType.INVENTORY_RETRIEVAL);
        
        log.info("Created "+InitJobType.INVENTORY_RETRIEVAL+" job for vault '"+vaultName+"' " +
            "with jobId "+initJobResult.getJobId());
        log.debug("requestVaultInventory() response: "+initJobResult.toString());
      }
      
      if(inProgressJobs.size() > 0) {
        int count = inProgressJobs.size();
        log.info("There "+(count==1?"is":"are")+" already "+count+" inventory retrieval job"
        +(count==1?"":"s")+" in progress for vault '"+vaultName+"'");
        
        for(GlacierJobDescription job: inProgressJobs) {
          log.debug("In-Progress inventory retrieval job for vault '"+vaultName+"': "+
              job.toString());
        }
      }
      
      /*
       * TODO: Do we need this information printed to the user? 
       */
      if(failedJobs.size() > 0) {
        int count = failedJobs.size();
        log.info(count+" inventory retrieval job"+(count==1?"":"s")+" failed for vault " +
        		"'"+vaultName+"'. Please check the logfile for more details.");
        
        for(GlacierJobDescription job: failedJobs) {
          log.debug("Failed inventory retrieval job for vault '"+vaultName+"': "+
              job.toString());
        }
      }
      
      if(succeededJobs.size() > 0) {
        /*
         * Get most recent job and present it to the user
         */
        sortJobListByCompletionDateDesc(succeededJobs);
        
        GlacierJobDescription succeededJob = succeededJobs.remove(0);
        log.info("Retrieving inventory for completed job: "+succeededJob.toString());
        GetJobOutputResult jobOutputResult = GetJobOutput.getJobOutput(credentials, endpoint, 
            vaultName, succeededJob.getJobId(), null);
        String jsonInventory = GetJobOutput.getJSONInventoryFromJobResult(jobOutputResult);
        
        log.info("Retrieved vault inventory: "+jsonInventory);
        
        /*
         * Log older succeeded jobs
         */
        for(GlacierJobDescription oldSucceededJob : succeededJobs) {
          log.debug("Retrieving inventory for older job: +"+oldSucceededJob.toString());
          GetJobOutputResult oldJobOutputResult = GetJobOutput.getJobOutput(credentials, endpoint, 
              vaultName, oldSucceededJob.getJobId(), null);
          
          String oldJsonInventory = GetJobOutput.getJSONInventoryFromJobResult(oldJobOutputResult);
          log.debug("Retrieved vault inventory from older job: "+oldJsonInventory);
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
      System.exit(1);
    }
  }
  
  private static void sortJobListByCompletionDateDesc(List<GlacierJobDescription> list) {
      Collections.sort(list, new Comparator<GlacierJobDescription>() {

        @Override
        public int compare(GlacierJobDescription o1, GlacierJobDescription o2) {
          /*
           * ISO 8601 date format , yay! 
           */
          return o1.getCompletionDate().compareTo(o2.getCompletionDate());
        }
      });
      Collections.reverse(list);
  }
}
