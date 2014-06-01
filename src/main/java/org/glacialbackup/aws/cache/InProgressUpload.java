/**
 * @author Kostas Lekkas (kwstasl@gmail.com) 
 */
package org.glacialbackup.aws.cache;

public class InProgressUpload {

  private String multipartUploadId;
  private String x_amz_content_sha256;
  private String x_amz_sha256_tree_hash;
  private String x_amz_archive_description;
  private String x_amz_part_size;
  private String content_length;
  
  public InProgressUpload() {
    
  }

  /**
   * @return the multipartUploadId
   */
  public String getMultipartUploadId() {
    return multipartUploadId;
  }

  /**
   * @param multipartUploadId the multipartUploadId to set
   */
  public void setMultipartUploadId(String multipartUploadId) {
    this.multipartUploadId = multipartUploadId;
  }

  /**
   * @return the x_amz_content_sha256
   */
  public String getX_amz_content_sha256() {
    return x_amz_content_sha256;
  }

  /**
   * @param x_amz_content_sha256 the x_amz_content_sha256 to set
   */
  public void setX_amz_content_sha256(String x_amz_content_sha256) {
    this.x_amz_content_sha256 = x_amz_content_sha256;
  }

  /**
   * @return the x_amz_sha256_tree_hash
   */
  public String getX_amz_sha256_tree_hash() {
    return x_amz_sha256_tree_hash;
  }

  /**
   * @param x_amz_sha256_tree_hash the x_amz_sha256_tree_hash to set
   */
  public void setX_amz_sha256_tree_hash(String x_amz_sha256_tree_hash) {
    this.x_amz_sha256_tree_hash = x_amz_sha256_tree_hash;
  }

  /**
   * @return the x_amz_archive_description
   */
  public String getX_amz_archive_description() {
    return x_amz_archive_description;
  }

  /**
   * @param x_amz_archive_description the x_amz_archive_description to set
   */
  public void setX_amz_archive_description(String x_amz_archive_description) {
    this.x_amz_archive_description = x_amz_archive_description;
  }

  /**
   * @return the x_amz_part_size
   */
  public String getX_amz_part_size() {
    return x_amz_part_size;
  }

  /**
   * @param x_amz_part_size the x_amz_part_size to set
   */
  public void setX_amz_part_size(String x_amz_part_size) {
    this.x_amz_part_size = x_amz_part_size;
  }

  /**
   * @return the content_length
   */
  public String getContent_length() {
    return content_length;
  }

  /**
   * @param content_length the content_length to set
   */
  public void setContent_length(String content_length) {
    this.content_length = content_length;
  }
  
  
}
