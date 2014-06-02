/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.cache;

import java.util.List;

/*
 * We needed a local cache to support resume for archive uploads. So, why not doing a little
 * extra work and support local caching of vault inventories ?
 */
public class VaultInfo {

  private String creationDate;
  private String lastInventoryDate;
  private String numberOfArchives;
  private String sizeInBytes;
  private String vaultARN;
  private String vaultName;
  
  private List<ArchiveInfo> inventory;
  
  public VaultInfo() {
    
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
   * @return the lastInventoryDate
   */
  public String getLastInventoryDate() {
    return lastInventoryDate;
  }

  /**
   * @param lastInventoryDate the lastInventoryDate to set
   */
  public void setLastInventoryDate(String lastInventoryDate) {
    this.lastInventoryDate = lastInventoryDate;
  }

  /**
   * @return the numberOfArchives
   */
  public String getNumberOfArchives() {
    return numberOfArchives;
  }

  /**
   * @param numberOfArchives the numberOfArchives to set
   */
  public void setNumberOfArchives(String numberOfArchives) {
    this.numberOfArchives = numberOfArchives;
  }

  /**
   * @return the sizeInBytes
   */
  public String getSizeInBytes() {
    return sizeInBytes;
  }

  /**
   * @param sizeInBytes the sizeInBytes to set
   */
  public void setSizeInBytes(String sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  /**
   * @return the vaultARN
   */
  public String getVaultARN() {
    return vaultARN;
  }

  /**
   * @param vaultARN the vaultARN to set
   */
  public void setVaultARN(String vaultARN) {
    this.vaultARN = vaultARN;
  }

  /**
   * @return the vaultName
   */
  public String getVaultName() {
    return vaultName;
  }

  /**
   * @param vaultName the vaultName to set
   */
  public void setVaultName(String vaultName) {
    this.vaultName = vaultName;
  }

  /**
   * @return the archiveList
   */
  public List<ArchiveInfo> getInventory() {
    return inventory;
  }

  /**
   * @param archiveList the archiveList to set
   */
  public void setInventory(List<ArchiveInfo> archiveList) {
    this.inventory = archiveList;
  }
  
}
