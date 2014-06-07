/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.vault;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.glacierjclient.operations.cache.model.LocalCache;
import org.glacierjclient.operations.jobs.InitiateJob;
import org.glacierjclient.operations.jobs.InitiateJob.InitJobType;
import org.glacierjclient.operations.jobs.JobOutput;
import org.glacierjclient.operations.jobs.ListJobs;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.ListJobsResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * Request vault inventory operation.
 */
public class RequestVaultInventory extends GlacierOperation {

  public static Logger log = LoggerFactory.getLogger(RequestVaultInventory.class);

  public RequestVaultInventory(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    try {
      String vaultName = argOpts.getString("inventory");
      requestVaultInventory(vaultName);
    } catch (AmazonServiceException ex) {
      log.error("AmazonServiceException: " + ex.getMessage());
      System.exit(1);
    } catch (AmazonClientException ex) {
      log.error("AmazonClientException: " + ex.getMessage());
      System.exit(1);
    } catch (IOException ex) {
      log.error("IOException: " + ex.getMessage());
      System.exit(1);
    }
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault")
        && argOpts.getString("inventory") != null;
  }

  /**
   * 
   * 
   * @param vaultName
   * @throws IOException
   */
  public void requestVaultInventory(String vaultName) throws IOException {

    /*
     * Get the list of all inventory requests for the specific vault
     */
    ListJobs listJobsOperation = new ListJobs(argOpts);
    ListJobsResult listJobsResult = listJobsOperation.listJobs(vaultName, "All");

    List<GlacierJobDescription> jobList = listJobsResult.getJobList();
    List<GlacierJobDescription> succeededJobs = new ArrayList<GlacierJobDescription>();
    List<GlacierJobDescription> inProgressJobs = new ArrayList<GlacierJobDescription>();
    List<GlacierJobDescription> failedJobs = new ArrayList<GlacierJobDescription>();

    for (GlacierJobDescription job : jobList) {
      /* Ignore archive retrieval jobs */
      if (!job.getAction().equals("InventoryRetrieval")) {
        continue;
      }

      if (job.getStatusCode().equals("Succeeded")) {
        succeededJobs.add(job);
      } else if (job.getStatusCode().equals("InProgress")) {
        inProgressJobs.add(job);
      } else { /* Failed */
        failedJobs.add(job);
      }
    }

    /*
     * Create a new job if there are neither in progress nor completed jobs.
     * TODO: Override this with a --force flag to create a new job anyway.
     */
    if (inProgressJobs.size() == 0 && succeededJobs.size() == 0) {
      InitiateJob initJob = new InitiateJob(argOpts);
      InitiateJobResult initJobResult =
          initJob.initiateJob(vaultName, InitJobType.INVENTORY_RETRIEVAL);

      log.info("Created " + InitJobType.INVENTORY_RETRIEVAL + " job for vault '" + vaultName + "' "
          + "with jobId " + initJobResult.getJobId());
      log.debug("requestVaultInventory() response: " + initJobResult.toString());
    }

    /*
     * Log information regarding in-progress jobs
     */
    if (inProgressJobs.size() > 0) {
      sortJobListByCreationDateAsc(inProgressJobs);

      String jobCreationDate = inProgressJobs.get(0).getCreationDate(); // oldest
      // job
      DateTimeZone zone = DateTimeZone.UTC;
      DateTime start = new DateTime(jobCreationDate, zone);
      DateTime end = new DateTime(zone);
      Interval interval = new Interval(start, end);
      long hours = interval.toDuration().getStandardHours();
      long minutes = interval.toDuration().getStandardMinutes() % 60;

      log.info("There are already " + inProgressJobs.size() + " inventory retrieval job(s)"
          + " in progress for vault '" + vaultName + "'. The oldest job was submitted " + hours
          + " hour(s), " + minutes + " minute(s) ago. Inventories can be retrieved ~4 hours after"
          + " a job has been submitted.");

      /*
       * Log all in-progress jobs
       */
      for (GlacierJobDescription job : inProgressJobs) {
        log.debug("In-Progress inventory retrieval job for vault '" + vaultName + "': "
            + job.toString());
      }
    }

    /*
     * Log failed jobs
     */
    if (failedJobs.size() > 0) {
      for (GlacierJobDescription job : failedJobs) {
        log.debug("Failed inventory retrieval job for vault '" + vaultName + "': " + job.toString());
      }
    }

    /*
     * Get most recent job (if any), cache it and present it to the user.
     */
    if (succeededJobs.size() > 0) {
      sortJobListByCompletionDateDesc(succeededJobs);

      /*
       * Get most recent succeeded job
       */
      GlacierJobDescription succeededJob = succeededJobs.remove(0);
      log.debug("Retrieving inventory for completed job: " + succeededJob.toString());
      JobOutput jobOut = new JobOutput(argOpts);
      GetJobOutputResult jobOutputResult =
          jobOut.getJobOutput(vaultName, succeededJob.getJobId(), null);

      String jsonInventory = jobOut.getJSONInventoryFromJobResult(jobOutputResult);
      log.debug("Retrieved vault inventory: " + jsonInventory);

      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonParser jp = new JsonParser();
      String prettyJson = gson.toJson(jp.parse(jsonInventory));
      System.out.println(prettyJson);

      /*
       * Cache inventory
       */
      LocalCache.loadCache().addInventory(jsonInventory);

      /*
       * Log older succeeded jobs
       */
      for (GlacierJobDescription oldSucceededJob : succeededJobs) {
        log.debug("Retrieving inventory for older job: +" + oldSucceededJob.toString());
        GetJobOutputResult oldJobOutputResult =
            jobOut.getJobOutput(vaultName, oldSucceededJob.getJobId(), null);
        String oldJsonInventory = jobOut.getJSONInventoryFromJobResult(oldJobOutputResult);
        log.debug("Retrieved vault inventory from older job: " + oldJsonInventory);
      }
    }
  }

  private void sortJobListByCompletionDateDesc(List<GlacierJobDescription> list) {
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

  private void sortJobListByCreationDateAsc(List<GlacierJobDescription> list) {
    Collections.sort(list, new Comparator<GlacierJobDescription>() {

      @Override
      public int compare(GlacierJobDescription o1, GlacierJobDescription o2) {
        return o1.getCreationDate().compareTo(o2.getCreationDate());
      }
    });
  }
}
