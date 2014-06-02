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
import org.glacialbackup.aws.cache.LocalCache;
import org.glacialbackup.aws.jobs.GetJobOutput;
import org.glacialbackup.aws.jobs.InitiateJob;
import org.glacialbackup.aws.jobs.InitiateJob.InitJobType;
import org.glacialbackup.aws.jobs.ListJobs;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

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
       * Request list of all inventory request jobs for the specific vault 
       */
      ListJobsResult listJobsResult = ListJobs.listJobs(credentials, endpoint, 
          vaultName, "All");

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
       * TODO: Override this with a --force flag
       */
      if(inProgressJobs.size() == 0 && succeededJobs.size() == 0) {
        InitiateJobResult initJobResult = InitiateJob.initiateJob(credentials, endpoint, 
              vaultName, InitJobType.INVENTORY_RETRIEVAL);
        
        log.info("Created "+InitJobType.INVENTORY_RETRIEVAL+" job for vault '"+vaultName+"' " +
            "with jobId "+initJobResult.getJobId());
        log.debug("requestVaultInventory() response: "+initJobResult.toString());
      }
      
      /*
       * Log information regarding in-progress jobs
       */
      if(inProgressJobs.size() > 0) {
        sortJobListByCreationDateAsc(inProgressJobs);
        
        String jobCreationDate = inProgressJobs.get(0).getCreationDate(); // oldest job
        DateTimeZone zone = DateTimeZone.UTC;
        DateTime start = new DateTime(jobCreationDate, zone);
        DateTime end = new DateTime(zone);
        Interval interval = new Interval(start, end);
        long hours = interval.toDuration().getStandardHours();
        long minutes = interval.toDuration().getStandardMinutes() % 60;

        log.info("There are already "+inProgressJobs.size()+" inventory retrieval job(s)"+
            " in progress for vault '"+vaultName+"'. Most recent job was submitted "+hours+
            " hour(s), "+minutes+" minute(s) ago.");

        /*
         * Log all in-progress jobs
         */
        for(GlacierJobDescription job: inProgressJobs) {
          log.debug("In-Progress inventory retrieval job for vault '"+vaultName+"': "+
              job.toString());
        }
      }

      /*
       * Log failed jobs
       */
      if(failedJobs.size() > 0) {
        for(GlacierJobDescription job: failedJobs) {
          log.debug("Failed inventory retrieval job for vault '"+vaultName+"': "+
              job.toString());
        }
      }
      
      /*
       * Get most recent job (if any), cache it and present it to the user.
       */
      if(succeededJobs.size() > 0) {
        sortJobListByCompletionDateDesc(succeededJobs);
        
        GlacierJobDescription succeededJob = succeededJobs.remove(0);
        log.info("Retrieving inventory for completed job: "+succeededJob.toString());
        GetJobOutputResult jobOutputResult = GetJobOutput.getJobOutput(credentials, endpoint, 
            vaultName, succeededJob.getJobId(), null);
        
        String jsonInventory = GetJobOutput.getJSONInventoryFromJobResult(jobOutputResult);
        log.debug("Retrieved vault inventory: "+jsonInventory);
        
        LocalCache.loadCache().addInventory(jsonInventory);
        System.out.println(jsonInventory);

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
  
  private static void sortJobListByCreationDateAsc(List<GlacierJobDescription> list) {
    Collections.sort(list, new Comparator<GlacierJobDescription>() {

      @Override
      public int compare(GlacierJobDescription o1, GlacierJobDescription o2) {
        return o1.getCreationDate().compareTo(o2.getCreationDate());
      }
    });
}
}
