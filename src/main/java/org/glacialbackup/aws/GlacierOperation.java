/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacialbackup.aws;

import java.io.File;
import java.io.IOException;

import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

/**
 * AWS Glacier operation.
 * This class
 */
public abstract class GlacierOperation extends GenericOperation {

  /**
   * Default location of AWS credentials.
   */
  private static final String DEFAULT_CREDENTIALS_PATH =
      System.getProperty("user.home") + "/.aws/aws.properties";

  public static Logger log = LoggerFactory.getLogger(GlacierOperation.class);

  public GlacierOperation(Namespace argOpts) {
    super(argOpts);
  }

  /**
   * Load AWS credentials from file.
   * 
   * @param location File path. Pass null to load credentials from DEFAULT_CREDENTIALS_PATH
   * @return {@link AWSCredentials} object
   */
  public static AWSCredentials loadCredentials(String location) {
    File credentialsFile;
    AWSCredentials credentials = null;

    if(location == null) {
      credentialsFile = new File(DEFAULT_CREDENTIALS_PATH);
    } else {
      credentialsFile = new File(location);
    }

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
   * Create endpoint url from region code
   * 
   * @param region Region code
   * @return Endpoint AWS Glacier endpoint url
   */
  public static String getEndpoint(String region) {
    return "https://glacier."+region.toLowerCase()+".amazonaws.com";
  }
}
