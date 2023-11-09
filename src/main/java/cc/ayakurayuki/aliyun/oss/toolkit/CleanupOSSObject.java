package cc.ayakurayuki.aliyun.oss.toolkit;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.OSSObjectSummary;
import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author Ayakura Yuki
 * @date 2023/10/09-17:10
 */
public class CleanupOSSObject {

  private static final String ENDPOINT    = "https://oss-cn-hangzhou.aliyuncs.com";
  private static final String BUCKET_NAME = "oss-dodo-voice-agora-storage";
  private static final String ACCESS_KEY  = "CDZEHqEmEgPklQay";
  private static final String SECRET_KEY  = "jjondPPsLAMj0blhjIQ2YbWJAHvwAo";
  private static final int    DAYS_BEFORE = 180; // 需要删除指定天数前的文件，例如 180 表示从现在往前 180 天的那一天之前的文件会被删除

  public static void main(String[] args) {
    var credentialProvider = CredentialsProviderFactory.newDefaultCredentialProvider(ACCESS_KEY, SECRET_KEY);
    var client = new OSSClientBuilder().build(ENDPOINT, credentialProvider);

    String nextContinuationToken = null;
    List<String> keys = Lists.newArrayList();
    long deadlineTimestamp = LocalDateTime.now()
        .minusDays(DAYS_BEFORE)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli();
    Date deadlineDate = new Date(deadlineTimestamp);

    for (; ; ) {
      var req = new ListObjectsV2Request()
          .withBucketName(BUCKET_NAME)
          .withMaxKeys(1000)
          .withContinuationToken(nextContinuationToken);
      var objectListing = client.listObjectsV2(req);
      if (objectListing.getObjectSummaries() == null || objectListing.getObjectSummaries().isEmpty() || objectListing.getNextContinuationToken() == null) {
        break;
      }

      nextContinuationToken = objectListing.getNextContinuationToken();

      for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        if (objectSummary.getLastModified().after(deadlineDate)) {
          continue;
        }

        keys.add(objectSummary.getKey());
      }
      if (keys.isEmpty()) {
        System.out.println("move to next token: " + nextContinuationToken);
        continue;
      }

      var res = client.deleteObjects(new DeleteObjectsRequest(BUCKET_NAME).withKeys(keys));
      if (res != null) {
        System.out.println("key deleted:");
        System.out.println(JSON.toJSONString(res.getDeletedObjects(), Feature.PrettyFormat));
      }

      keys.clear();
    }

    client.shutdown();
  }

}
