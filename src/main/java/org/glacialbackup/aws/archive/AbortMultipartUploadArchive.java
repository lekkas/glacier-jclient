/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacialbackup.aws.archive;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.cache.LocalCache;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.AbortMultipartUploadRequest;

/**
 * Abort multipart upload operation wrapper.
 */
public class AbortMultipartUploadArchive extends GlacierOperation {

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
      abortOperation(client, vaultName, uploadId);

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
   * @param client
   * @param vaultName
   * @param uploadId
   */
  public static void abortOperation(AmazonGlacierClient client, String vaultName, String uploadId) {

    AbortMultipartUploadRequest abortMultipartUploadRequest =
        new AbortMultipartUploadRequest().withVaultName(vaultName).withUploadId(uploadId);

    client.abortMultipartUpload(abortMultipartUploadRequest);
  }
}
