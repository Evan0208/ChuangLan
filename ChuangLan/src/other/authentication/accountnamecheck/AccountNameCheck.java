package other.authentication.accountnamecheck;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.Account;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;


/**
 * 客户认证新增或修改时校验新名称是否已经存在
 *
 * @author gongqainq
 */
public class AccountNameCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户认证校验客户名称-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            String newAccountName = model.getString("customItem5__c");

            Long accountId = model.getLong("customItem2__c");

            QueryResult<Account> accountQueryResult = XObjectService.instance().query("SELECT id, accountName FROM account WHERE id<>" + accountId + " AND accountName = '" + newAccountName + "'", true);
            logger.info("查询客户信息：" + JSONObject.toJSONString(accountQueryResult));
            if (accountQueryResult.getSuccess()) {
                List<Account> accountList = accountQueryResult.getRecords();
                if (accountList != null && accountList.size() > 0) {
                    throw new ScriptBusinessException("客户认证的新客户名称已存在。");
                }
            }
        } catch (Exception ex) {
            logger.error("客户认证校验客户名称，报错信息" + ex.getMessage());
            throw new ScriptBusinessException(ex);
        }
        return new

                ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
