/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.cache;

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

import com.google.gson.Gson;

/*
 * TODO: Encrypt the cache ?
 * TODO: Support alternative locations for the cache - currently it's '~/.glacialbackup/cache'
 */
public class LocalCache {

  private List<VaultInfo> vaults = new ArrayList<VaultInfo>();
  private List<InProgressUpload> inProgressUploads = new ArrayList<InProgressUpload>();
  
  private transient static final File cacheFile = new File(System.getProperty("user.home") +
        File.separator + ".glacialbackup" + File.separator + "cache");
  
  public transient static Logger log = LoggerFactory.getLogger(LocalCache.class);

  private LocalCache() {
    
  }

  /**
   * Adds a VaultInfo object in the cache. This operation has no effect if the cache already
   * contains a vault with the same vaultARN that has a lastInventoryDate which is not older than 
   * the one we are trying to add.
   * 
   * Based on API x-amz-glacier-version: 2012-06-01
   *  
   * @param vaultInfoJson The JSON reply from the Describe Vault request
   */
  public void addVaultInfo(String vaultInfoJson) {
    Gson gson = new Gson();
    VaultInfo vaultInfo = gson.fromJson(vaultInfoJson, VaultInfo.class);
    
    VaultInfo existingVault = null;
    Iterator<VaultInfo> it = getVaults().iterator();
    while(it.hasNext()) {
      VaultInfo v = it.next();
      if(v.getVaultARN().equals(vaultInfo.getVaultARN())) {
        existingVault = v;
        break;
      }
    }
     
    if(existingVault == null ) {
      getVaults().add(vaultInfo);
      saveCache();
      log.debug("Added vault "+vaultInfo.getVaultName()+" to cache.");
    }
    else if (vaultInfo.getLastInventoryDate().
        compareTo(existingVault.getLastInventoryDate()) > 0) {
      getVaults().remove(existingVault);
      getVaults().add(vaultInfo);
      saveCache();
      log.debug("Added vault "+vaultInfo.getVaultName()+" to cache.");
    }
    else {
      log.debug("Vault "+vaultInfo.getVaultName()+"is already in the cache with the same or more" +
      		" recent last inventory date.");
      return;
    }
  }
  
  /*
   * Add vault list to cache.
   * TODO: Currently we are converting json to objects back-and-forth in order to insert each 
   * VaultInfo object to the cache.
   */
  public void addVaultInfoList(String listVaultInfoJson) {
    Gson gson = new Gson();
    VaultInfo[] list = gson.fromJson(listVaultInfoJson, VaultInfo[].class);
    
    for(int i = 0; i < list.length; i++) {
      addVaultInfo(gson.toJson(list[i]));
    }
  }
  
  /*
   * Removes vault from cache
   */
  public void deleteVaultInfo(String vaultName) {
    Iterator<VaultInfo> it = getVaults().iterator();
    while(it.hasNext()) {
      VaultInfo v = it.next();
      if(v.getVaultName().equals(vaultName)) {
        it.remove();
        log.debug("Removed vault "+vaultName+" from cache.");
        break;
      }
    }
  }
  
  /*
   * Adds an ArchiveInfo object in the cache. This operation adds the object to the cache if 
   * there is no archive with the same archiveId already in the list.
   * 
   * Based on API x-amz-glacier-version: 2012-06-01
   */
  public void addArchiveInfo(String archiveInfoJson, String vaultARN) {
    Gson gson = new Gson();
    ArchiveInfo archiveInfo = gson.fromJson(archiveInfoJson, ArchiveInfo.class);
    
    VaultInfo vault = null;
    for(VaultInfo v : getVaults()) {
      if(v.getVaultARN().equals(vaultARN)) {
        vault = v;
        break;
      }
    }
    
    if(vault == null) {
      log.debug("Could not add archive to cache; vault not found: "+vaultARN);
      return;
    }
    
    for(ArchiveInfo a : vault.getInventory()) {
      if(a.getArchiveId().equals(archiveInfo)) {
        log.debug("Archive "+archiveInfo.getArchiveId()+" is already in the cache");
        return;
      }
    }
    vault.getInventory().add(archiveInfo);
    saveCache();
    log.debug("Added archive "+archiveInfo.getArchiveId()+" to cache under vault "+
        vault.getVaultName());
  }
  
  /*
   * Add archive list to cache.
   * 
   * TODO: Currently we are converting json to objects back-and-forth in order to insert each 
   * ArciveInfo object to the cache.
   */
  public void addArchiveInfoList(String archiveInfoListJson, String vaultARN) {
    Gson gson = new Gson();
    ArchiveInfo[] list = gson.fromJson(archiveInfoListJson, ArchiveInfo[].class);
    
    for(int i = 0; i < list.length; i++) {
      addArchiveInfo(gson.toJson(list[i]), vaultARN);
    }
  }
  
  /*
   * Saves objects to cache
   */
  private void saveCache() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    
    try {
      PrintWriter out = new PrintWriter(cacheFile, "UTF-8");
      out.write(json);
      out.close();
    } catch (FileNotFoundException ex) {
      log.info("FileNotFoundException: "+ex.getMessage());
    } catch (UnsupportedEncodingException ex) {
      log.info("UnsupportedEncodingException: "+ex.getMessage());
    }
  }
  
  public void printVaultInfo(VaultInfo vault) {
    StringBuilder buf = new StringBuilder();
    buf.append("\nCreationDate: " + vault.getCreationDate());
    buf.append("\nLastInventoryDate: " + vault.getLastInventoryDate());
    buf.append("\nNumberOfArchives: " + vault.getNumberOfArchives());
    buf.append("\nSizeInBytes: " + vault.getSizeInBytes());
    buf.append("\nVaultARN: " + vault.getVaultARN()); 
    buf.append("\nVaultName: " + vault.getVaultName());
    buf.append("\n");
    System.out.print(buf.toString());
  }
  
  public void printArchiveInfo(ArchiveInfo archive) {
    StringBuilder buf = new StringBuilder();
    buf.append("\nArchiveId: " + archive.getArchiveId());
    buf.append("\nArchiveDescription: " + archive.getArchiveDescription());
    buf.append("\nCreationDate: " + archive.getCreationDate());
    buf.append("\nSize: " + archive.getSize());
    buf.append("\nSHA256TreeHash: " + archive.getSha256TreeHash());
    buf.append("\n");
    System.out.print(buf.toString());
  }
  
  public static LocalCache loadCache() {
    if(cacheFile.exists()) {
      LocalCache cache = loadJSONFromFile();
      return cache;
    }
    else {
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
      log.error("IOException: "+ex.getMessage());
      return null;
    } 
  }
  
  private static LocalCache createEmptyCache() {

    if(!cacheFile.getParentFile().exists()) {
      boolean createdDir = cacheFile.getParentFile().mkdir();
      if(!createdDir) {
        log.error("Could not create directory "+cacheFile.getParent());
        return null;
      }
    }
    
    try {
      PrintWriter out = new PrintWriter(cacheFile, "UTF-8");
      Gson gson = new Gson();
      String json = gson.toJson(new LocalCache());
      out.print(json);
      out.close();
      log.info("Created empty cache in "+cacheFile.getCanonicalPath());
      return new LocalCache();
    } catch (FileNotFoundException ex) {
      log.error("FileNotFoundException: "+ex.getMessage());
      return null;
    } catch (UnsupportedEncodingException ex) {
      log.error("UnsupportedEncodingException: "+ex.getMessage());
      return null;
    } catch(IOException ex) {
      log.error("IOException: "+ex.getMessage());
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
   * @param vaults the vaults to set
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
   * @param inProgressUploads the inProgressUploads to set
   */
  public void setInProgressUploads(List<InProgressUpload> inProgressUploads) {
    this.inProgressUploads = inProgressUploads;
  }
}
