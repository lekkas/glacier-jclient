/**
 * @author Kostas Lekkas (kwstasl@gmail.com)initClient();
 */
package org.glacierjclient.operations.vault;

import net.sourceforge.argparse4j.inf.Namespace;

import org.glacierjclient.operations.GlacierOperation;
import org.glacierjclient.operations.cache.model.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * List vaults operation.
 */
public class ListVaults extends GlacierOperation {

  private final Logger log = LoggerFactory.getLogger(ListVaults.class);

  public ListVaults(Namespace argOpts) {
    super(argOpts);
  }

  @Override
  public void exec() {
    try {
      ListVaultsResult listVaultsResult = requestVaultList();
      log.debug("requestVaultList() response: " + listVaultsResult.toString());
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String prettyJson = gson.toJson(listVaultsResult.getVaultList());
      System.out.println(prettyJson);

      /*
       * Save all vaults to cache
       */
      LocalCache.loadCache().addVaultInfoList(listVaultsResult);
    } catch (AmazonServiceException ex) {
      log.error("AmazonServiceException: " + ex.getMessage());
      System.exit(1);
    } catch (AmazonClientException ex) {
      log.error("AmazonClientException: " + ex.getMessage());
      System.exit(1);
    }
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && argOpts.getBoolean("list") == true;
  }

  /**
   * This operation requests the list of all vaults owned by the calling user’s
   * account TODO: Add marker support for lists of over 1000 items.
   */
  public ListVaultsResult requestVaultList() {
    AmazonGlacierClient client = getAWSClient();
    ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
    ListVaultsResult listVaultsResult = client.listVaults(listVaultsRequest);
    return listVaultsResult;
  }
}
