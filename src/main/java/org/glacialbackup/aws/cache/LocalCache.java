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
import com.google.gson.GsonBuilder;

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
   * Add in progress multipart upload in the cache. 
   * @param uploadInfo
   */
  public void addInProgressUpload(InProgressUpload inProgressUpload) {
    Iterator<InProgressUpload> it = getInProgressUploads().iterator();
    while(it.hasNext()) {
      InProgressUpload u = it.next();
      if(u.getArchiveFilePath().equals(inProgressUpload.getArchiveFilePath())) {
        log.debug("There is already one upload job for archive "+u.getArchiveFilePath());
        return;
      }
    }
    getInProgressUploads().add(inProgressUpload);
    saveCache();
    log.debug("Added multipart job for archive "+inProgressUpload.getArchiveFilePath()+" with job " +
    		"id "+inProgressUpload.getMultipartUploadId());
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
    while(it.hasNext()) {
      InProgressUpload u = it.next();
      if(u.getArchiveFilePath().equals(filePath) && u.getVault().equals(vaultName)) {
        String uploadId = u.getMultipartUploadId();
        log.debug("Found pending multipart upload for file "+filePath+" on vault '"+vaultName+"'");
        return uploadId;
      }
    }
    return null;
  }
  
  /*
   * Remove in progress upload from cache
   */
  public void deleteInProgressUpload(String vaultName, String uploadId) {
    Iterator<InProgressUpload> it = getInProgressUploads().iterator();
    while(it.hasNext()) {
      InProgressUpload u = it.next();
      if(u.getMultipartUploadId().equals(uploadId) && u.getVault().equals(vaultName)) {
        String fname = u.getArchiveFilePath();
        it.remove();
        saveCache();
        log.debug("Removed cached upload operation for archive "+fname+" on vault '"+vaultName+
            "' with upload id "+uploadId);
        break;
      }
    }
  }
  /**
   * Adds a VaultInfo object in the cache. 
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
    else {
      /* Keep inventory */
      VaultInventory vaultInventory = existingVault.getVaultInventory();
      vaultInfo.setVaultInventory(vaultInventory);

      getVaults().remove(existingVault);
      getVaults().add(vaultInfo);
      saveCache();
      log.debug("Updated vault "+vaultInfo.getVaultName()+" in cache.");
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
        saveCache();
        log.debug("Removed vault "+vaultName+" from cache.");
        break;
      }
    }
  }

  /*
   * Cache vault inventory
   */
  public void addInventory(String jsonInventory) {
    Gson gson = new Gson();
    VaultInventory vaultInventory = gson.fromJson(jsonInventory, VaultInventory.class);
    
    /*
    System.out.println("arn: "+vaultInventory.getVaultARN());
    System.out.println("date: "+vaultInventory.getInventoryDate());
    System.out.println("list size: "+vaultInventory.getArchiveList().size());
    */
    
    String vaultARN = vaultInventory.getVaultARN();
    for(VaultInfo v : getVaults()) {
      if(v.getVaultARN().equals(vaultARN)) {
        v.setVaultInventory(vaultInventory);
        saveCache();
        log.debug("Cached vault inventory for vault "+v.getVaultName());
        return;
      }
    }
    log.debug("Could not find vault "+vaultInventory.getVaultARN()+" in cache.");
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
  
  private void prettyPrintVault(VaultInfo vault) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(vault);
    System.out.println(json);
  }
  
  public void prettyPrintVaults() {
    for(VaultInfo v : getVaults())
      prettyPrintVault(v);
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
