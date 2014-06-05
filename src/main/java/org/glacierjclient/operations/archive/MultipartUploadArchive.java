/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.archive;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.glacierjclient.operations.cache.model.InProgressUpload;
import org.glacierjclient.operations.cache.model.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.amazonaws.services.glacier.model.RequestTimeoutException;
import com.amazonaws.services.glacier.model.ServiceUnavailableException;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
import com.amazonaws.util.BinaryUtils;
import com.google.gson.Gson;

/**
 * Multipart upload.
 * 
 * All archives are uploaded using the multipart API regardless of their size.
 * 
 * TODO: Check 10.000 parts upload limit.
 */
public class MultipartUploadArchive extends GlacierOperation {

  private final Logger log = LoggerFactory.getLogger(MultipartUploadArchive.class);

  public static long defaultPartSize = 1024L * 1024L; // 1 MB.

  public MultipartUploadArchive(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    try {
      String archiveFilePath = argOpts.getString("upload");
      String vaultName = argOpts.getString("vault");
      String endpoint = getEndpoint(argOpts.getString("endpoint"));
      String description = argOpts.getString("description");

      if (description == null) {
        description = (new File(archiveFilePath)).getName();
      }

      if (archiveFilePath.startsWith("~" + File.separator)) {
        archiveFilePath = System.getProperty("user.home") + archiveFilePath.substring(1);
      }
      File file = new File(archiveFilePath);
      RandomAccessFile in = new RandomAccessFile(file, "r");
      AWSCredentials credentials = loadCredentials(argOpts.getString("credentials"));
      AmazonGlacierClient client = new AmazonGlacierClient(credentials);
      client.setEndpoint(endpoint);
      String uploadId = LocalCache.loadCache().getInProgressUpload(vaultName, archiveFilePath);

      /*
       * New upload operation
       */
      if (uploadId == null) {
        uploadId = initiateMultipartUpload(vaultName, description, defaultPartSize);

        /*
         * Cache the new multipart upload
         */
        InProgressUpload inProgressUpload =
            new InProgressUpload(archiveFilePath, uploadId, vaultName);
        LocalCache.loadCache().addInProgressUpload(inProgressUpload);

        String archiveChecksum = uploadParts(vaultName, 0, defaultPartSize, null, uploadId, in);
        completeMultipartUpload(vaultName, archiveChecksum, uploadId, in.length());

        /*
         * Resuming upload operation
         */
      } else {
        resumeMultipartUpload(vaultName, uploadId, in);
      }

      /*
       * Remove cached upload information from cache when finished
       */
      LocalCache.loadCache().deleteInProgressUpload(vaultName, uploadId);
    } catch (FileNotFoundException ex) {
      log.error("FileNotFoundException: " + ex.getMessage());
      System.exit(1);
    } catch (AmazonServiceException ex) {
      log.error("AmazonServiceException: " + ex.getMessage());
      System.exit(1);
    } catch (AmazonClientException ex) {
      log.error("AmazonClientException: " + ex.getMessage());
      System.exit(1);
    } catch (IOException ex) {
      log.error("IOExceptioin: " + ex.getMessage());
      System.exit(1);
    }
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("archive")
        && argOpts.getString("upload") != null;
  }

  /**
   * Initiates multipart upload.
   * 
   * @param vaultName
   * @param description
   * @param partSize
   * @return uploadId
   */
  public String initiateMultipartUpload(String vaultName, String description, long partSize) {
    AmazonGlacierClient client = getAWSClient();
    InitiateMultipartUploadRequest request =
        new InitiateMultipartUploadRequest().withVaultName(vaultName).withArchiveDescription(
            description).withPartSize(String.valueOf(partSize));

    InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
    String uploadId = result.getUploadId();
    log.debug("Multipart upload initiated successfully with uploadId: " + uploadId);
    return uploadId;
  }

  /**
   * Uploads part of archive.
   * 
   * @param vaultName
   * @param uploadId
   * @param partBytes
   * @param checksum
   * @param contentRangeRFC2616
   * @return the checksum of the uploaded part, as returned by AWS
   */
  public String uploadPart(String vaultName, String uploadId, byte[] partBytes, String checksum,
      String contentRangeRFC2616) {

    AmazonGlacierClient client = getAWSClient();
    UploadMultipartPartRequest partRequest =
        new UploadMultipartPartRequest().withVaultName(vaultName).withChecksum(checksum).withRange(
            contentRangeRFC2616).withUploadId(uploadId).withBody(
                new ByteArrayInputStream(partBytes));

    UploadMultipartPartResult partResult = client.uploadMultipartPart(partRequest);
    String awsChecksum = partResult.getChecksum();
    log.debug(contentRangeRFC2616 + " uploaded successfully. Local checksum: " + checksum
        + " , remote" + " checksum: " + awsChecksum);
    return awsChecksum;
  }

  /**
   * Upload all parts of archive.
   * 
   * @param vaultName
   * @param startContentRange
   *          The first byte of the first part to upload
   * @param partSize
   * @param binaryChecksums
   *          List of binary checksums from all previously uploaded parts
   *          (resuming upload operation). Pass null if no parts have been
   *          previously uploaded (new upload operation)
   * @param uploadId
   * @param in
   *          {@link RandomAccessFile} object representing the archive
   * @return checksum of the whole archive
   * @throws IOException
   */
  public String uploadParts(String vaultName, long startContentRange, long partSize,
      List<byte[]> binaryChecksums, String uploadId, RandomAccessFile in) throws IOException {
    int numOfRetries = 5;
    int sleepMillis = 10000;
    int totalParts = (int) Math.ceil((double) (in.length() - 1L) / (double) partSize);
    int currentPart = (int) Math.floor((double) (startContentRange) / (double) partSize) + 1;

    if (binaryChecksums == null) {
      binaryChecksums = new LinkedList<byte[]>();
    }

    while (startContentRange < in.length()) {
      /*
       * Read archive part from file.
       */
      byte[] part = readContentRangeFromFile(in, startContentRange, partSize);
      long endContentRange = startContentRange + part.length - 1L;
      String checksum = TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(part));
      byte[] binaryChecksum = BinaryUtils.fromHex(checksum);
      binaryChecksums.add(binaryChecksum);
      String contentRangeRFC2616 =
          String.format("bytes %s-%s/*", Long.toString(startContentRange), Long
              .toString(endContentRange));

      /*
       * Retry to upload part when we face recoverable exceptions. TODO: Add
       * more recover options here (e.g. network IOExceptions)
       */
      boolean partUploaded = false;
      int tryCount = 1;
      while (!partUploaded && tryCount <= numOfRetries) {
        try {
          log.info("Uploading part " + currentPart + "/" + totalParts + " : (" + startContentRange
              + "-" + endContentRange + ")");
          uploadPart(vaultName, uploadId, part, checksum, contentRangeRFC2616);
          partUploaded = true;
        } catch (RequestTimeoutException ex) {
          log.info("RequestTimeoutException: Retrying to upload part " + currentPart + "/"
              + totalParts + " in " + sleepMillis / 1000 + " seconds. Attempt " + tryCount + "/"
              + numOfRetries);
          tryCount++;
        } catch (ServiceUnavailableException ex) {
          log.info("RequestTimeoutException: Retrying to upload part " + currentPart + "/"
              + totalParts + " in " + sleepMillis / 1000 + " seconds. Attempt " + tryCount + "/"
              + numOfRetries);
          tryCount++;
        }
      }

      if (!partUploaded) {
        log.error("Failed to upload " + contentRangeRFC2616
            + ". Aborting upload, please see log file" + " for more information");
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
   * @param in
   *          {@link RandomAccessFile} object representing archive
   * @param contentRangeStart
   *          offset of first byte to read
   * @param partSize
   *          number of bytes to read from file
   * @return byte[] read bytes array
   */
  private byte[]
      readContentRangeFromFile(RandomAccessFile in, long contentRangeStart, long partSize)
          throws IOException {
    byte[] buf = new byte[(int) partSize];

    in.seek(contentRangeStart);
    int bytesRead = in.read(buf, 0, (int) partSize);
    return Arrays.copyOf(buf, bytesRead);
  }

  /**
   * Complete multipart upload.
   * 
   * @param vaultName
   * @param checksum
   *          Checksum of the whole archive
   * @param uploadId
   * @param fileSize
   */
  public void completeMultipartUpload(String vaultName, String checksum, String uploadId,
      long fileSize) {
    AmazonGlacierClient client = getAWSClient();
    CompleteMultipartUploadRequest compRequest =
        new CompleteMultipartUploadRequest().withVaultName(vaultName).withUploadId(uploadId)
        .withChecksum(checksum).withArchiveSize(String.valueOf(fileSize));

    CompleteMultipartUploadResult compResult = client.completeMultipartUpload(compRequest);
    log.info("Archive was successfully created: " + compResult.getLocation());
  }

  /**
   * Resume existing multipart upload
   * 
   * @param vaultName
   * @param uploadId
   * @param {@link RandomAccessFile}
   * @throws IOException
   */
  public void resumeMultipartUpload(String vaultName, String uploadId, RandomAccessFile in)
      throws IOException {
    MultipartUploadStatus multiPartUploadStatus = listParts(vaultName, uploadId);
    log.info("Resuming upload. uploadId: " + uploadId + ", partSize: "
        + multiPartUploadStatus.getPartSizeInBytes());

    long partSize = multiPartUploadStatus.getPartSizeInBytes();
    long start = 0;

    /*
     * Retrieve binary checksums of uploaded parts
     */
    LinkedList<byte[]> binaryChecksums = new LinkedList<byte[]>();
    for (PartListElement p : multiPartUploadStatus.getUploadedParts()) {
      String range[] = p.getRangeInBytes().split("-");
      if (!range[0].equals(Long.toString(start))) {
        log.info("TODO: Missing part recovery. Check log for details.");
        System.exit(1);
      }
      byte[] binaryChecksum = BinaryUtils.fromHex(p.getSHA256TreeHash());
      binaryChecksums.add(binaryChecksum);
      start = start + partSize;
    }

    int listSize = multiPartUploadStatus.getUploadedParts().size();
    long nextPartStartContentRange = listSize * partSize;

    String archiveChecksum =
        uploadParts(vaultName, nextPartStartContentRange, partSize, binaryChecksums, uploadId, in);
    completeMultipartUpload(vaultName, archiveChecksum, uploadId, in.length());
  }

  /**
   * Retrieve list of uploaded parts for an in-progress multipart upload.
   * 
   * @param vaultName
   * @param multipartUploadId
   * @return {@link MultipartUploadStatus} object
   */
  public MultipartUploadStatus listParts(String vaultName, String uploadId) {
    AmazonGlacierClient client = getAWSClient();
    ListPartsRequest listPartsRequest;
    ListPartsResult listPartsResult;

    listPartsRequest = new ListPartsRequest().withVaultName(vaultName).withUploadId(uploadId);

    MultipartUploadStatus uploadInfo = new MultipartUploadStatus();

    listPartsResult = client.listParts(listPartsRequest);
    uploadInfo.setArchiveDescription(listPartsResult.getArchiveDescription());
    uploadInfo.setCreationDate(listPartsResult.getCreationDate());
    uploadInfo.setMultipartUploadId(listPartsResult.getMultipartUploadId());
    uploadInfo.setPartSizeInBytes(listPartsResult.getPartSizeInBytes());
    uploadInfo.setUploadedParts(listPartsResult.getParts());

    /*
     * Handle part lists larger than 1000 items by using markers, as described
     * in the ListParts API.
     */
    String marker = listPartsResult.getMarker();
    while (marker != null) {
      listPartsRequest =
          new ListPartsRequest().withVaultName(vaultName).withUploadId(uploadId).withMarker(marker);

      listPartsResult = client.listParts(listPartsRequest);
      uploadInfo.getUploadedParts().addAll(listPartsResult.getParts());
      marker = listPartsResult.getMarker();
    }
    Gson gson = new Gson();
    String json = gson.toJson(uploadInfo.getUploadedParts());
    log.debug("Uploaded part list: " + json);
    log.debug("Retrieved uploaded parts list (" + uploadInfo.getUploadedParts().size()
        + " parts have" + "been uploaded already)");
    return uploadInfo;
  }
}
