/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Abstract AWS Glacier operation. I had an inner argument on whether to initialize some generic
 * things here (i.e. credentials loading and client initialization) but decided that it would be
 * cleaner design to include all operations of the AWS request in each implemented 
 */
public abstract class GlacierOperation {
  
  protected final Namespace argOpts;
  private static final String DEFAULT_CREDENTIALS_PATH = 
      System.getProperty("user.home") + "/.aws/aws.properties";
  
  public static Logger log = LoggerFactory.getLogger(GlacierOperation.class);
  
  public GlacierOperation(Namespace argOpts) {
      this.argOpts = argOpts;
  }

  /**
   * Load credentials from file
   * @param location file path. Pass null to load credentials from default path
   * @return AWSCredentials object 
   */
  public static AWSCredentials loadCredentials(String location) {
    File credentialsFile;
    AWSCredentials credentials = null;

    if(location == null)
      credentialsFile = new File(DEFAULT_CREDENTIALS_PATH);
    else
      credentialsFile = new File(location);
    
    try {
      credentials = new PropertiesCredentials(credentialsFile);
    } catch(IOException ex) {
      log.error("Could not load credentials from "+credentialsFile.toString());
      System.exit(1);
    } catch(IllegalArgumentException ex) {
    	log.error(ex.getMessage());
    	System.exit(1);
    }
    
    return credentials;
  }
  
  /**
   * Get endpoint from region code
   * @param region Region code
   * @return Endpoint
   */
  public static String getEndpoint(String region) {
    return "https://glacier."+region.toLowerCase()+".amazonaws.com";
  }
  
  public abstract void exec();
  public abstract boolean valid();
}
