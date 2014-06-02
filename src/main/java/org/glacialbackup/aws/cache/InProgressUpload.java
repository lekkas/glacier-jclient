/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.cache;

public class InProgressUpload {

  private String archiveFilePath;
  private String archiveFileSHA256;
  private String vault;
  private String multipartUploadId;
  
  public InProgressUpload() {
    
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

  /**
   * @return the archiveFileSHA256
   */
  public String getArchiveFileSHA256() {
    return archiveFileSHA256;
  }

  /**
   * @param archiveFileSHA256 the archiveFileSHA256 to set
   */
  public void setArchiveFileSHA256(String archiveFileSHA256) {
    this.archiveFileSHA256 = archiveFileSHA256;
  }
}
