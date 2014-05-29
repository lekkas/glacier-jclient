/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.vault;

import org.glacialbackup.aws.GlacierOperation;

import net.sourceforge.argparse4j.inf.Namespace;


public class RequestVaultInventory extends GlacierOperation {

  public RequestVaultInventory(Namespace argOpts) {
    super(argOpts);
  }
  
  @Override
  public void exec() {
    System.out.println("Executing "+this.getClass().getName());
    
  }

  @Override
  public boolean valid() {
    return argOpts.getString("command_name").equals("vault") && 
            argOpts.getString("inventory") != null;
  }

}
