/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.archive;

import org.glacialbackup.aws.GlacierOperation;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;

import net.sourceforge.argparse4j.inf.Namespace;


public class UploadMultipartArchive extends GlacierOperation {

  public UploadMultipartArchive(Namespace argOpts) {
    super(argOpts);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void exec() {
    System.out.println("Executing "+this.getClass().getName());
    
  }
  
  @Override
  public boolean valid() {
    return false;
  }
  
  
  public static void uploadArchive(AWSCredentials credentials, String endpoint, 
      String vaultName, String archiveFilePath) {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);

  }
  
  public static void initiateMultipartUpload(AmazonGlacierClient client, String vaultName, 
      String description, String partSize) {
    
  }
  
  /**
   * Uploads part of archive.
   * 
   * @param vaultName
   * @param uploadId
   * @param partBytes
   * @param checksum
   * @param contentRange
   * @return the checksum returned by AWS
   */
  public static String uploadPart(AmazonGlacierClient client, String vaultName, String uploadId, 
      byte[] partBytes, String checksum, String contentRange) {
    
    
    return null;
  }
  
  /**
   * Complete multipart upload.
   * 
   * @param client
   * @param vaultName
   * @param checksum
   * @param fileSize
   * @return Location of uploaded archive
   */
  public static void completeMultipartUpload(AmazonGlacierClient client, String vaultName, 
      String checksum, String fileSize) {

  }
  
  public static void resumeMultipartUpload(AmazonGlacierClient client) {
    
  }

  public static void listParts() {
    
  }

}
