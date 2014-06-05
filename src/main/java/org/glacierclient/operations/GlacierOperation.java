/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierclient.operations;

import java.io.File;
import java.io.IOException;

import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

/**
 * AWS Glacier operation.
 * This class
 */
public abstract class GlacierOperation extends GenericOperation {
  
  private final Logger log = LoggerFactory.getLogger(GlacierOperation.class);

  /**
   * Default location of AWS credentials.
   */
  private static final String DEFAULT_CREDENTIALS_PATH =
      System.getProperty("user.home") + "/.aws/aws.properties";

  
  private AmazonGlacierClient client;
  
  public GlacierOperation(Namespace argOpts) {
    super(argOpts);
    AWSCredentials credentials = loadCredentials(argOpts.getString("credentials"));
    String endpoint = getEndpoint(argOpts.getString("endpoint"));
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
  }

  /**
   * Load AWS credentials from file.
   * 
   * @param location File path. Pass null to load credentials from DEFAULT_CREDENTIALS_PATH
   * @return {@link AWSCredentials} object
   */
  public AWSCredentials loadCredentials(String location) {
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
  
  public AmazonGlacierClient getAWSClient() {
    return client;
  }
}
