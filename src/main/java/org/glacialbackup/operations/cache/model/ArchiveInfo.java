/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.operations.cache.model;

public class ArchiveInfo {
  
  private String ArchiveId;
  private String ArchiveDescription;
  private String CreationDate;
  private String Size;
  private String SHA256TreeHash;
  
  public ArchiveInfo() {
    
  }

  /**
   * @return the archiveId
   */
  public String getArchiveId() {
    return ArchiveId;
  }

  /**
   * @param archiveId the archiveId to set
   */
  public void setArchiveId(String archiveId) {
    ArchiveId = archiveId;
  }

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
   * @return the size
   */
  public String getSize() {
    return Size;
  }

  /**
   * @param size the size to set
   */
  public void setSize(String size) {
    Size = size;
  }

  /**
   * @return the sHA256TreeHash
   */
  public String getSHA256TreeHash() {
    return SHA256TreeHash;
  }

  /**
   * @param sHA256TreeHash the sHA256TreeHash to set
   */
  public void setSHA256TreeHash(String sHA256TreeHash) {
    SHA256TreeHash = sHA256TreeHash;
  }
  
}
