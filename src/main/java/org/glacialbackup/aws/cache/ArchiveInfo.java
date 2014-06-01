/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.cache;

public class ArchiveInfo {
  
  private String archiveId;
  private String archiveDescription; // not required when uploading an archive
  private String creationDate;
  private String size;
  private String sha256TreeHash;
  
  public ArchiveInfo() {
    
  }

  /**
   * @return the archiveId
   */
  public String getArchiveId() {
    return archiveId;
  }

  /**
   * @param archiveId the archiveId to set
   */
  public void setArchiveId(String archiveId) {
    this.archiveId = archiveId;
  }

  /**
   * @return the archiveDescription
   */
  public String getArchiveDescription() {
    return archiveDescription;
  }

  /**
   * @param archiveDescription the archiveDescription to set
   */
  public void setArchiveDescription(String archiveDescription) {
    this.archiveDescription = archiveDescription;
  }

  /**
   * @return the creationDate
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return the size
   */
  public String getSize() {
    return size;
  }

  /**
   * @param size the size to set
   */
  public void setSize(String size) {
    this.size = size;
  }

  /**
   * @return the sha256TreeHash
   */
  public String getSha256TreeHash() {
    return sha256TreeHash;
  }

  /**
   * @param sha256TreeHash the sha256TreeHash to set
   */
  public void setSha256TreeHash(String sha256TreeHash) {
    this.sha256TreeHash = sha256TreeHash;
  }
  
}
