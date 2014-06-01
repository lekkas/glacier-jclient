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

  public void saveCache() {
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
