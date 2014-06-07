/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.cache.model;

import com.amazonaws.services.glacier.model.DescribeVaultResult;

/*
 * We needed a local cache to support resume for archive uploads. So, why not doing a little
 * extra work and support local caching of vault inventories ?
 */
public class VaultInfo {

  private DescribeVaultResult vaultMetadata = new DescribeVaultResult();
  private VaultInventory vaultInventory = new VaultInventory();

  public VaultInfo() {

  }

  /**
   * @return the vaultInventory
   */
  public VaultInventory getVaultInventory() {
    return vaultInventory;
  }

  /**
   * @param vaultInventory
   *          the vaultInventory to set
   */
  public void setVaultInventory(VaultInventory vaultInventory) {
    this.vaultInventory = vaultInventory;
  }

  public DescribeVaultResult getVaultMetadata() {
    return vaultMetadata;
  }

  public void setVaultMetadata(DescribeVaultResult vaultMetadata) {
    this.vaultMetadata = vaultMetadata;
  }
}
