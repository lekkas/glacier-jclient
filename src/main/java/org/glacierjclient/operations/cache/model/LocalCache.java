/**
 * @author Kostas Lekkas (kwstasl@gmail.com)
 */
package org.glacierjclient.operations.cache.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*
 * TODO: Encrypt the cache ?
 * TODO: Support alternative locations for the cache - currently it's '~/.glacierjclient/cache'
 */
public class LocalCache {

  public transient static Logger log = LoggerFactory.getLogger(LocalCache.class);

  private List<VaultInfo> vaults = new ArrayList<VaultInfo>();
  private List<InProgressUpload> inProgressUploads = new ArrayList<InProgressUpload>();

  private transient static final File cacheFile = new File(System.getProperty("user.home")
      + File.separator + ".glacierjclient" + File.separator + "cache");

  private LocalCache() {

  }

  /**
   * Add in progress multipart upload in the cache.
   * 
   * @param uploadInfo
   */
  public void addInProgressUpload(InProgressUpload inProgressUpload) {
    Iterator<InProgressUpload> it = getInProgressUploads().iterator();
    while (it.hasNext()) {
      InProgressUpload u = it.next();
      if (u.getArchiveFilePath().equals(inProgressUpload.getArchiveFilePath())) {
        log.debug("There is already one upload job for archive " + u.getArchiveFilePath());
        return;
      }
    }
    getInProgressUploads().add(inProgressUpload);
    saveCache();
    log.debug("Added multipart job for archive " + inProgressUpload.getArchiveFilePath()
        + " with job " + "id " + inProgressUpload.getMultipartUploadId());
  }

  /**
   * Find upload Id
   * 
   * @param vaultName
   * @param filePath
   * @return UploadId , null if no matching jobs were found.
   */
  public String getInProgressUpload(String vaultName, String filePath) {
    Iterator<InProgressUpload> it = getInProgressUploads().iterator();
    while (it.hasNext()) {
      InProgressUpload u = it.next();
      if (u.getArchiveFilePath().equals(filePath) && u.getVault().equals(vaultName)) {
        String uploadId = u.getMultipartUploadId();
        log.debug("Found pending multipart upload for file " + filePath + " on vault '" + vaultName
            + "'");
        return uploadId;
      }
    }
    return null;
  }

  /**
   * Remove in-progress upload from cache.
   * 
   * @param vaultName
   * @param uploadId
   */
  public void deleteInProgressUpload(String vaultName, String uploadId) {
    Iterator<InProgressUpload> it = getInProgressUploads().iterator();
    while (it.hasNext()) {
      InProgressUpload u = it.next();
      if (u.getMultipartUploadId().equals(uploadId) && u.getVault().equals(vaultName)) {
        String fname = u.getArchiveFilePath();
        it.remove();
        saveCache();
        log.debug("Removed cached upload operation for archive " + fname + " on vault '"
            + vaultName + "' with upload id " + uploadId);
        break;
      }
    }
  }

  /**
   * Adds a DescribeVaultResult object in the cache.
   * 
   * @param vaultMetadata
   *          {@link DescribeVaultResult} object
   */
  public void addVaultInfo(DescribeVaultResult vaultMetadata) {
    VaultInfo existingVault = null;
    Iterator<VaultInfo> it = getVaults().iterator();
    while (it.hasNext()) {
      VaultInfo v = it.next();
      if (v.getVaultMetadata().getVaultARN().equals(vaultMetadata.getVaultARN())) {
        existingVault = v;
        break;
      }
    }
    /*
     * Create new vault cache entry
     */
    if (existingVault == null) {
      VaultInfo newVault = new VaultInfo();
      newVault.setVaultMetadata(vaultMetadata);
      getVaults().add(newVault);
      saveCache();
      log.debug("Added vault " + vaultMetadata.getVaultName() + " to cache.");
    } else {
      /*
       * Update existing vault cache entry metadata
       */
      existingVault.setVaultMetadata(vaultMetadata);
      saveCache();
      log.debug("Updated vault " + vaultMetadata.getVaultName() + " in cache.");
    }
  }

  /**
   * Add vault list to cache.
   * 
   * @param listVaultsResult
   *          {@link ListVaultsResult} object
   */
  public void addVaultInfoList(ListVaultsResult listVaultsResult) {
    List<DescribeVaultOutput> vaultMetaList = listVaultsResult.getVaultList();
    for (DescribeVaultOutput v : vaultMetaList) {
      DescribeVaultResult vMeta = new DescribeVaultResult();
      vMeta.setCreationDate(v.getCreationDate());
      vMeta.setLastInventoryDate(v.getLastInventoryDate());
      vMeta.setNumberOfArchives(v.getNumberOfArchives());
      vMeta.setSizeInBytes(v.getNumberOfArchives());
      vMeta.setVaultARN(v.getVaultARN());
      vMeta.setVaultName(v.getVaultName());
      addVaultInfo(vMeta);
    }
  }

  /**
   * Remove vault from cache
   * 
   * @param vaultName
   */
  public void deleteVaultInfo(String vaultName) {
    Iterator<VaultInfo> it = getVaults().iterator();
    while (it.hasNext()) {
      VaultInfo v = it.next();
      if (v.getVaultMetadata().getVaultName().equals(vaultName)) {
        it.remove();
        saveCache();
        log.debug("Removed vault " + vaultName + " from cache.");
        break;
      }
    }
  }

  /**
   * Remove archive from cached vault
   * 
   * @param vaultName
   * @param archiveId
   */
  public void deleteArchiveInfo(String vaultName, String archiveId) {
    Iterator<VaultInfo> it = getVaults().iterator();
    while (it.hasNext()) {
      VaultInfo v = it.next();
      if (v.getVaultMetadata().getVaultName().equals(vaultName)) {
        List<ArchiveInfo> archiveList = v.getVaultInventory().getArchiveList();
        Iterator<ArchiveInfo> archIt = archiveList.iterator();
        while (archIt.hasNext()) {
          ArchiveInfo a = archIt.next();
          if (a.getArchiveId().equals(archiveId)) {
            archIt.remove();
            saveCache();
            log.debug("Removed archive " + archiveId + " from cache.");
            break;
          }
        }
      }
    }
  }

  /**
   * Add inventory under existing vault in cache
   * 
   * @param jsonInventory
   */
  public void addInventory(String jsonInventory) {
    Gson gson = new Gson();
    VaultInventory vaultInventory = gson.fromJson(jsonInventory, VaultInventory.class);
    String vaultARN = vaultInventory.getVaultARN();
    for (VaultInfo v : getVaults()) {
      if (v.getVaultMetadata().getVaultARN().equals(vaultARN)) {
        v.setVaultInventory(vaultInventory);
        saveCache();
        log.debug("Cached vault inventory for vault " + v.getVaultMetadata().getVaultName());
        return;
      }
    }
    log.debug("Could not cached vault inventory; vault " + vaultInventory.getVaultARN() + " "
        + "does not exist in cache.");
  }

  /**
   * Save cache.
   */
  private void saveCache() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    try {
      PrintWriter out = new PrintWriter(cacheFile, "UTF-8");
      out.write(json);
      out.close();
    } catch (FileNotFoundException ex) {
      log.info("FileNotFoundException: " + ex.getMessage());
    } catch (UnsupportedEncodingException ex) {
      log.info("UnsupportedEncodingException: " + ex.getMessage());
    }
  }

  private void prettyPrintVault(VaultInfo vault) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(vault);
    System.out.println(json);
  }

  public void prettyPrintVaults() {
    for (VaultInfo v : getVaults()) {
      prettyPrintVault(v);
    }
  }

  private void prettyPrintInProgressUpload(InProgressUpload upload) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(upload);
    System.out.println(json);
  }

  public void prettyPrintInProgressUploads() {
    for (InProgressUpload u : getInProgressUploads()) {
      prettyPrintInProgressUpload(u);
    }
  }

  public static LocalCache loadCache() {
    if (cacheFile.exists()) {
      LocalCache cache = loadJSONFromFile();
      return cache;
    } else {
      return createEmptyCache();
    }
  }

  private static LocalCache loadJSONFromFile() {
    try {
      BufferedReader in = new BufferedReader(new FileReader(cacheFile));
      Gson gson = new Gson();
      LocalCache cache = gson.fromJson(in, LocalCache.class);
      return cache;
    } catch (IOException ex) {
      log.error("IOException: " + ex.getMessage());
      return null;
    }
  }

  private static LocalCache createEmptyCache() {
    if (!cacheFile.getParentFile().exists()) {
      boolean createdDir = cacheFile.getParentFile().mkdir();
      if (!createdDir) {
        log.error("Could not create directory " + cacheFile.getParent());
        return null;
      }
    }

    try {
      PrintWriter out = new PrintWriter(cacheFile, "UTF-8");
      Gson gson = new Gson();
      String json = gson.toJson(new LocalCache());
      out.print(json);
      out.close();
      log.info("Created empty cache in " + cacheFile.getCanonicalPath());
      return new LocalCache();
    } catch (FileNotFoundException ex) {
      log.error("FileNotFoundException: " + ex.getMessage());
      return null;
    } catch (UnsupportedEncodingException ex) {
      log.error("UnsupportedEncodingException: " + ex.getMessage());
      return null;
    } catch (IOException ex) {
      log.error("IOException: " + ex.getMessage());
      return null;
    }
  }

  /**
   * @return the vaults
   */
  public List<VaultInfo> getVaults() {
    return vaults;
  }

  /**
   * @param vaults
   *          the vaults to set
   */
  public void setVaults(List<VaultInfo> vaults) {
    this.vaults = vaults;
  }

  /**
   * @return the inProgressUploads
   */
  public List<InProgressUpload> getInProgressUploads() {
    return inProgressUploads;
  }

  /**
   * @param inProgressUploads
   *          the inProgressUploads to set
   */
  public void setInProgressUploads(List<InProgressUpload> inProgressUploads) {
    this.inProgressUploads = inProgressUploads;
  }
}
