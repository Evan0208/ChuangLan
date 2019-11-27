package other.authentication.writebackaccount;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import commons.utils.XsyHelper;
import java.util.List;

/**
 * 客户认证回写客户信息
 *
 * @author gongqiang
 */
public class WriteBackAccount implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();


    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户认证回写客户信息-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户认证ID
            long id = model.getLong("id");
            //客户id
            long accountId = model.getLong("customItem2__c");
            //新客户名称
            String newAccountName = model.getString("customItem5__c");
            //营业执照号
            String BusinessLicenseNumber = model.getString("customItem3__c");

            JSONObject account = new JSONObject();
            account.put("accountName", newAccountName);
            account.put("customItem191__c", BusinessLicenseNumber);
            //修改客户同步状态为未同步
            account.put("synchState__c", 1);

            account.put("customItem200__c", 2);

            RkhdHttpClient client = new RkhdHttpClient();

            boolean result = XsyHelper.updateEntity(client, "account", accountId, account);
            if (result) {
                logger.info("客户认证回写客户信息成功，客户ID：" + accountId + " 修改内容：" + account);
            } else {
                logger.error("客户认证回写客户信息失败，客户ID：" + accountId + " 修改内容：" + account);
            }
        } catch (Exception ex) {
            System.out.println(" 错误Exception：" + ex);
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}

