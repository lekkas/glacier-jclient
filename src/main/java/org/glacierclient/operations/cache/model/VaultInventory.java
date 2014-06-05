/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacierclient.operations.cache.model;

import java.util.List;

public class VaultInventory {
  private String VaultARN;
  private String InventoryDate;
  private List<ArchiveInfo> ArchiveList;
  
  public VaultInventory() {
    
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
   * @return the inventoryDate
   */
  public String getInventoryDate() {
    return InventoryDate;
  }

  /**
   * @param inventoryDate the inventoryDate to set
   */
  public void setInventoryDate(String inventoryDate) {
    InventoryDate = inventoryDate;
  }

  /**
   * @return the archiveList
   */
  public List<ArchiveInfo> getArchiveList() {
    return ArchiveList;
  }

  /**
   * @param archiveList the archiveList to set
   */
  public void setArchiveList(List<ArchiveInfo> archiveList) {
    ArchiveList = archiveList;
  }

  
}
