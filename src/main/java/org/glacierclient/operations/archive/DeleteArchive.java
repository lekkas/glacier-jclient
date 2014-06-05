/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierclient.operations.archive;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierclient.operations.GlacierOperation;
import org.glacierclient.operations.cache.model.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;

/**
 * Delete archive.
 */
public class DeleteArchive extends GlacierOperation {

  private final Logger log = LoggerFactory.getLogger(DeleteArchive.class);
  
  public DeleteArchive(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    String archiveId = argOpts.getString("delete");
    String vaultName = argOpts.getString("vault");

    try {
      deleteArchive(vaultName, archiveId);

      /*
       * Remove entry from the cache
       */
      LocalCache.loadCache().deleteArchiveInfo(vaultName, archiveId);
      log.info("Deleted archive with id " + archiveId);
    } catch (AmazonServiceException ex) {
      log.error("AmazonServiceException: " + ex.getMessage());
    } catch (AmazonClientException ex) {
      log.error("AmazonClientException: " + ex.getMessage());
    }
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("archive") &&
        argOpts.getString("delete") != null;
  }

  /**
   * Delete archive from vault.
   * 
   * @param vaultName
   * @param archiveId
   */
  public void deleteArchive(String vaultName, String archiveId) {
    AmazonGlacierClient client = getAWSClient();
    DeleteArchiveRequest deleteRequest = new DeleteArchiveRequest()
      .withVaultName(vaultName)
      .withArchiveId(archiveId);
    client.deleteArchive(deleteRequest);
  }
}
