/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierjclient.operations.archive;

import java.util.List;

import com.amazonaws.services.glacier.model.PartListElement;

public class MultipartUploadStatus {

  private String ArchiveDescription;
  private String CreationDate;
  private String Marker;
  private String MultipartUploadId;
  private long PartSizeInBytes;
  private String VaultARN;
  
  private List<PartListElement> uploadedParts;
  
  /**
   * @return the archiveDescription
   */
  public String getArchiveDescription() {
    return ArchiveDescription;
  }

  /**
   * @param archiveDescription the archiveDescription to set
   */
  public void setArchiveDescription(String archiveDescription) {
    ArchiveDescription = archiveDescription;
  }

  /**
   * @return the creationDate
   */
  public String getCreationDate() {
    return CreationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(String creationDate) {
    CreationDate = creationDate;
  }

  /**
   * @return the marker
   */
  public String getMarker() {
    return Marker;
  }

  /**
   * @param marker the marker to set
   */
  public void setMarker(String marker) {
    Marker = marker;
  }

  /**
   * @return the multipartUploadId
   */
  public String getMultipartUploadId() {
    return MultipartUploadId;
  }

  /**
   * @param multipartUploadId the multipartUploadId to set
   */
  public void setMultipartUploadId(String multipartUploadId) {
    MultipartUploadId = multipartUploadId;
  }

  /**
   * @return the partSizeInBytes
   */
  public long getPartSizeInBytes() {
    return PartSizeInBytes;
  }

  /**
   * @param partSizeInBytes the partSizeInBytes to set
   */
  public void setPartSizeInBytes(long partSizeInBytes) {
    PartSizeInBytes = partSizeInBytes;
  }

  /**
   * @return the vaultARN
   */
  public String getVaultARN() {
    return VaultARN;
  }

  /**
   * @param vaultARN the vaultARN to set
   */
  public void setVaultARN(String vaultARN) {
    VaultARN = vaultARN;
  }

  /**
   * @return the parts
   */
  public List<PartListElement> getUploadedParts() {
    return uploadedParts;
  }

  /**
   * @param parts the parts to set
   */
  public void setUploadedParts(List<PartListElement> parts) {
    uploadedParts = parts;
  }
}
