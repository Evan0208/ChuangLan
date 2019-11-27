package commons.utils;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.data.model.PasswordSet__c;
import com.rkhd.platform.sdk.exception.ApiEntityServiceException;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonData;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;

/**
 * @author Administrator
 */
public class PasswordHelper {
    private static PasswordSet__c passwordModel = null;
    private static final Logger logger = LoggerFactory.getLogger();

    public static PasswordSet__c getPasswordSet() throws ScriptBusinessException, ApiEntityServiceException {
        if (passwordModel == null) {
            QueryResult<PasswordSet__c> result = XObjectService.instance().query("select id,client_id__c,client_secret__c,redirect_uri__c,username__c,password__c,security__c,url_str__c from passwordSet__c order by id asc limit 0,1", true);
            if (result.getSuccess()) {
                List<PasswordSet__c> list = result.getRecords();
                if (list != null && list.size() > 0) {
                    passwordModel = list.get(0);
                } else {
                    throw new ScriptBusinessException("获取秘钥失败-->秘钥表无数据");
                }
            } else {
                throw new ScriptBusinessException("获取秘钥失败" + result.getErrorMessage());
            }
        }
        return passwordModel;
    }

    public static String getToken(CommonHttpClient client) throws ScriptBusinessException, ApiEntityServiceException {
        PasswordSet__c model = getPasswordSet();
        String oauthUri = model.getUrl_str__c() + "/oauth2/token.action";
        logger.debug(oauthUri);
        CommonData param = new CommonData();
        param.setCall_type("POST");
        param.setCallString(oauthUri);
        param.putFormData("grant_type", "password");
        param.putFormData("client_id", model.getClient_id__c());
        param.putFormData("client_secret", model.getClient_secret__c());
        param.putFormData("redirect_uri", model.getRedirect_uri__c());
        param.putFormData("username", model.getUsername__c());
        param.putFormData("password", model.getPassword__c() + model.getSecurity__c());
        String resultToken = client.performRequest(param);
        logger.debug(resultToken);
        JSONObject resultJson = JSONObject.parseObject(resultToken);
        String token = resultJson.getString("access_token");
        return token;
    }
}
