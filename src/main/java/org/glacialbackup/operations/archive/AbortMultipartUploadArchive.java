/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacialbackup.operations.archive;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.operations.GlacierOperation;
import org.glacialbackup.operations.cache.model.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.AbortMultipartUploadRequest;

/**
 * Abort multipart upload.
 */
public class AbortMultipartUploadArchive extends GlacierOperation {
  
  private final Logger log = LoggerFactory.getLogger(AbortMultipartUploadArchive.class);

  public AbortMultipartUploadArchive(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    String uploadId = argOpts.getString("abort");
    String vaultName = argOpts.getString("vault");
    String endpoint = getEndpoint(argOpts.getString("endpoint"));
    AWSCredentials credentials = loadCredentials(argOpts.getString("credentials"));
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);

    try {
      abortUpload(vaultName, uploadId);

      /*
       * Remove entry from the cache
       */
      LocalCache.loadCache().deleteInProgressUpload(vaultName, uploadId);

      log.info("Aborted multipart upload with id " + uploadId);
    } catch (AmazonServiceException ex) {
      log.error("AmazonServiceException: " + ex.getMessage());
    } catch (AmazonClientException ex) {
      log.error("AmazonClientException: " + ex.getMessage());
    }
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("archive")
        && argOpts.getString("abort") != null;
  }

  /**
   * Multipart upload abort.
   * 
   * @param vaultName
   * @param uploadId
   */
  public void abortUpload(String vaultName, String uploadId) {
    AmazonGlacierClient client = getAWSClient();
    AbortMultipartUploadRequest abortMultipartUploadRequest =
        new AbortMultipartUploadRequest().withVaultName(vaultName).withUploadId(uploadId);

    client.abortMultipartUpload(abortMultipartUploadRequest);
  }
}
