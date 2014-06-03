/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.archive;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.glacialbackup.aws.GlacierOperation;
import org.glacialbackup.aws.cache.InProgressUpload;
import org.glacialbackup.aws.cache.LocalCache;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.TreeHashGenerator;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.ListPartsRequest;
import com.amazonaws.services.glacier.model.ListPartsResult;
import com.amazonaws.services.glacier.model.PartListElement;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
import com.amazonaws.util.BinaryUtils;

import net.sourceforge.argparse4j.inf.Namespace;


public class MultipartUploadArchive extends GlacierOperation {

  public static String partSize = "1048576"; // 1 MB.
  
  public MultipartUploadArchive(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    try {
      String filePath = argOpts.getString("upload");
      String vaultName = argOpts.getString("vault");
      String endpoint = getEndpoint(argOpts.getString("endpoint"));
      String description = argOpts.getString("description");
      
      if (filePath.startsWith("~" + File.separator)) { 
        filePath = System.getProperty("user.home") + filePath.substring(1);
      }
      
      if(description == null) {
        description = (new File(filePath)).getName();
      }

      AWSCredentials credentials = loadCredentials(argOpts.getString("credentials"));
      AmazonGlacierClient client = new AmazonGlacierClient(credentials);
      client.setEndpoint(endpoint);

      String uploadId = LocalCache.loadCache().getInProgressUpload(vaultName, filePath);
      uploadArchive(credentials, endpoint, vaultName, filePath, description, 
          Long.parseLong(partSize), uploadId);
    } catch(FileNotFoundException ex) {
      log.error("FileNotFoundException: "+ex.getMessage());
      System.exit(1);
    } catch(AmazonServiceException ex) {
      log.error("AmazonServiceException: "+ex.getMessage());
    } catch(AmazonClientException ex) {
      log.error("AmazonClientException: "+ex.getMessage());
    } catch(IOException ex) {
      log.error("IOExceptioin: "+ex.getMessage());
    }
  }
  
  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("archive") && 
        argOpts.getString("upload") != null;
  }
  
  /**
   * Upload archive to Amazon Glacier. All archives are uploaded using the multipart operation. 
   * 
   * @param credentials
   * @param endpoint
   * @param vaultName
   * @param archiveFilePath
   * @param description Archive description. If null the description will match the file name.
   * @param partSize
   * @param uploadId Set to null for new archives. If not null the operation tries to resume
   * the specified multipart upload.
   */
  public static void uploadArchive(AWSCredentials credentials, String endpoint, 
      String vaultName, String archiveFilePath, String description, 
      long partSize, String uploadId) throws FileNotFoundException, IOException {
    
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);
    client.setEndpoint(endpoint);
    
    File file = new File(archiveFilePath);
    RandomAccessFile in = new RandomAccessFile(file, "r");
    String descr = (description!=null)?description:file.getName();
   
    if(uploadId == null) { /* New Upload */ 
      uploadId = initiateMultipartUpload(client, vaultName, descr, partSize);
      
      /*
       * Cache it
       */
      InProgressUpload inProgressUpload = new InProgressUpload(archiveFilePath, 
          uploadId, vaultName);
      LocalCache.loadCache().addInProgressUpload(inProgressUpload);
      
      String archiveChecksum = uploadParts(client, vaultName, 0, partSize, null, uploadId, in);
      completeMultipartUpload(client, vaultName, archiveChecksum, uploadId, in.length());
      LocalCache.loadCache().deleteInProgressUpload(vaultName, uploadId);
    }
    else { /* Resuming Upload*/
      resumeMultipartUpload(client, vaultName, uploadId, in);
    }
  }
  
  /**
   * Initiates multipart upload.
   * 
   * @param client
   * @param vaultName
   * @param description
   * @param partSize
   * @return uploadId on success, null on failure
   */
  public static String initiateMultipartUpload(AmazonGlacierClient client, String vaultName, 
      String description, long partSize) {
    
    InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest()
      .withVaultName(vaultName)
      .withArchiveDescription(description)
      .withPartSize(String.valueOf(partSize));            

    InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
    return result.getUploadId();
  }
  
  /**
   * Uploads part of archive.
   * 
   * TODO: Handle recoverable errors
   * 
   * @param vaultName
   * @param uploadId
   * @param partBytes
   * @param checksum
   * @param contentRange
   * @return the checksum returned by AWS, null on error
   */
  public static String uploadPart(AmazonGlacierClient client, String vaultName, String uploadId, 
      byte[] partBytes, String checksum, String contentRangeRFC2616) {
    
    UploadMultipartPartRequest partRequest = new UploadMultipartPartRequest()
        .withVaultName(vaultName)
        .withChecksum(checksum)
        .withRange(contentRangeRFC2616)
        .withUploadId(uploadId)
        .withBody(new ByteArrayInputStream(partBytes));
    
    UploadMultipartPartResult partResult = client.uploadMultipartPart(partRequest);
    log.debug(contentRangeRFC2616+" uploaded successfully. Local checksum: "+checksum+" , remote" +
    		" checksum: "+partResult.getChecksum());
    
    return partResult.getChecksum();
  }
  
  /**
   * Upload all parts of archive.
   * 
   * @param client
   * @param vaultName
   * @param startContentRange The first byte of the first part to upload
   * @param partSize
   * @param binaryChecksums
   * @param uploadId
   * @param in
   * @return checksum of the whole archive
   * @throws IOException
   */
  public static String uploadParts(AmazonGlacierClient client, String vaultName, 
      long startContentRange, long partSize, List<byte[]> binaryChecksums, 
      String uploadId, RandomAccessFile in) throws IOException {
    
    if(binaryChecksums == null) {
      binaryChecksums = new LinkedList<byte[]>();
    }
    
    int numOfRetries = 5;
    int sleepMillis = 10000;
    int totalParts = (int)Math.ceil((double)(in.length() - 1L)/(double)partSize);
    int currentPart = (int)Math.floor((double)(startContentRange)/(double)partSize) + 1;
    
    while(startContentRange < in.length()) {
      byte[] part = readContentRangeFromFile(in, startContentRange, partSize);
      long endContentRange = startContentRange + part.length - 1L;
      
      log.info("Uploading part "+currentPart+"/"+totalParts+" : ("+
          startContentRange+"-"+endContentRange+")");
      
      String checksum = TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(part));
      byte[] binaryChecksum = BinaryUtils.fromHex(checksum);
      binaryChecksums.add(binaryChecksum);
      String contentRangeRFC2616 = String.format("bytes %s-%s/*", 
          Long.toString(startContentRange), Long.toString(endContentRange));
      
      String amznChecksum = uploadPart(client, vaultName, uploadId, part, 
          checksum, contentRangeRFC2616);
      
      while ( amznChecksum == null && numOfRetries > 0) {
        log.info("Retrying to upload "+contentRangeRFC2616);
        try {
          Thread.sleep(sleepMillis);
        } catch(InterruptedException ex) {
          log.info("InterruptedException: "+ex.getMessage());
        }
        amznChecksum = uploadPart(client, vaultName, uploadId, part, 
            checksum, contentRangeRFC2616);
        numOfRetries--;
      }
      
      if(amznChecksum == null) {
        log.error("Failed to upload "+contentRangeRFC2616+". Aborting upload, please see log file" +
        		" for more information");
        System.exit(1);
      }
      startContentRange = startContentRange + partSize;
      currentPart++;
    }
    String checksum = TreeHashGenerator.calculateTreeHash(binaryChecksums);
    return checksum;
  }
  
  /**
   * Read content range from file
   * 
   * @param in a RandomAccessFile 
   * @param contentRangeStart 
   * @param partSize number of bytes to read from file
   * @param buffer byte[] buffer to store read bytes
   * @return byte[] array on success, null on failure
   */
  private static byte[] readContentRangeFromFile(RandomAccessFile in, 
    long contentRangeStart, long partSize) throws IOException {
    byte[] buf = new byte[(int)partSize];
    
    in.seek(contentRangeStart);
    int bytesRead = in.read(buf, 0, (int)partSize);
    return Arrays.copyOf(buf, bytesRead);
  }
  
  /**
   * Complete multipart upload.
   * 
   * @param client
   * @param vaultName
   * @param checksum
   * @param uploadId
   * @param fileSize
   */
  public static void completeMultipartUpload(AmazonGlacierClient client, String vaultName, 
      String checksum, String uploadId, long fileSize) {

      CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest()
        .withVaultName(vaultName)
        .withUploadId(uploadId)
        .withChecksum(checksum)
        .withArchiveSize(String.valueOf(fileSize));
      
      CompleteMultipartUploadResult compResult = client.completeMultipartUpload(compRequest);
      log.info("Archive was successfully created: "+compResult.getLocation());
  }
  
  public static void resumeMultipartUpload(AmazonGlacierClient client, String vaultName, 
      String uploadId, RandomAccessFile in) throws IOException {

    log.info("Resuming upload operation.");
    MultipartUploadStatus multiPartUploadStatus = listParts(client, vaultName, uploadId);

    long partSize = multiPartUploadStatus.getPartSizeInBytes();
    long start = 0;
    long end = start + partSize - 1;

    /*
     * Retrieve binary checksums of uploaded parts
     */
    List<byte[]> binaryChecksums = new LinkedList<byte[]>();
    for(PartListElement p : multiPartUploadStatus.getUploadedParts()) {
      String range = String.format("%s-%s", Long.toString(start), Long.toString(end));
      if(!range.equals(p.getRangeInBytes())) {
        log.info("TODO: Missing part recovery. Check log for details.");
        System.exit(1);
      }
      byte[] binaryChecksum = BinaryUtils.fromHex(p.getSHA256TreeHash());
      binaryChecksums.add(binaryChecksum);
      
      start = start + partSize;
      end = start + partSize - 1;
    }
    
    /*
     * Find last uploaded part and resume from there.
     */
    int listSize = multiPartUploadStatus.getUploadedParts().size();
    String lastContentRange = multiPartUploadStatus
        .getUploadedParts()
        .get(listSize-1)
        .getRangeInBytes();
    
    String range[] = lastContentRange.split("-");
    long nextStartContentRange = Long.parseLong(range[1]) + 1L;
    
    
    String checksum = uploadParts(client, vaultName, nextStartContentRange, partSize, 
        binaryChecksums, uploadId, in);
    
    completeMultipartUpload(client, vaultName, checksum, uploadId, in.length());
    LocalCache.loadCache().deleteInProgressUpload(vaultName, uploadId);
  }

  /**
   * Retrieve list of uploaded parts for an in-progress multipart upload.
   * 
   * @param client
   * @param vaultName
   * @param multipartUploadId
   * @return
   */
  public static MultipartUploadStatus listParts(AmazonGlacierClient client, String vaultName,
      String uploadId) {

    ListPartsRequest listPartsRequest; 
    ListPartsResult listPartsResult;
    
    listPartsRequest = new ListPartsRequest()
      .withVaultName(vaultName)
      .withUploadId(uploadId);
    
    listPartsResult = client.listParts(listPartsRequest);
    
    MultipartUploadStatus uploadInfo = new MultipartUploadStatus();
    uploadInfo.setArchiveDescription(listPartsResult.getArchiveDescription());
    uploadInfo.setCreationDate(listPartsResult.getCreationDate());
    uploadInfo.setMultipartUploadId(listPartsResult.getMultipartUploadId());
    uploadInfo.setPartSizeInBytes(listPartsResult.getPartSizeInBytes());
    uploadInfo.setUploadedParts(listPartsResult.getParts());

    /*
     * Handle part lists larger than 1000 items by using markers, as described in the 
     * ListParts API.
     */
    String marker = listPartsResult.getMarker();
    while(marker != null) {
      listPartsRequest = new ListPartsRequest()
      .withVaultName(vaultName)
      .withUploadId(uploadId)
      .withMarker(marker);
    
      listPartsResult = client.listParts(listPartsRequest);
      uploadInfo.getUploadedParts().addAll(listPartsResult.getParts());
      marker = listPartsResult.getMarker();
    }
    log.debug("Retrieved uploaded parts list ("+uploadInfo.getUploadedParts().size()+" parts have" +
    		"been uploaded already)");
    return uploadInfo;
  }
}
