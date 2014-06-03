/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.cache;

public class InProgressUpload {

  private String archiveFilePath;
  private String vault;
  private String multipartUploadId;
  
  public InProgressUpload() {
    
  }
  
  public InProgressUpload(String archiveFilePath, String multipartUploadId, String vault) {
    this.archiveFilePath = archiveFilePath;
    this.multipartUploadId = multipartUploadId;
    this.vault = vault;
  }

  /**
   * @return the multipartUploadId
   */
  public String getMultipartUploadId() {
    return multipartUploadId;
  }

  /**
   * @param multipartUploadId the multipartUploadId to set
   */
  public void setMultipartUploadId(String multipartUploadId) {
    this.multipartUploadId = multipartUploadId;
  }

  /**
   * @return the vault
   */
  public String getVault() {
    return vault;
  }

  /**
   * @param vault the vault to set
   */
  public void setVault(String vault) {
    this.vault = vault;
  }

  /**
   * @return the archiveFilePath
   */
  public String getArchiveFilePath() {
    return archiveFilePath;
  }

  /**
   * @param archiveFilePath the archiveFilePath to set
   */
  public void setArchiveFilePath(String archiveFilePath) {
    this.archiveFilePath = archiveFilePath;
  }
}
