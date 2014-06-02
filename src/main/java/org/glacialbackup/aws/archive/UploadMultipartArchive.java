/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.archive;

import org.glacialbackup.aws.GlacierOperation;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

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
  
  public static void initiateMultipartUpload() {
    
  }
  
  public static void uploadPart() {
    
  }
  
  public static void completeMultipartUpload() {
    
  }
  
  public static void resumeMultipartUpload(AmazonGlacierClient client) {
    
  }
  

}
