package other.selfserviceaccounts.createcheeck;


import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity18__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import commons.utils.XobjectHelper;

import java.util.List;

/**
 * 自助通账号新增校验
 *
 * @author gongqiang
 */
public class CreateCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("自助通账号新增校验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户id
            long accountId = model.getLong("customItem1__c");

            List<CustomEntity18__c> selfAccountList = XobjectHelper.v2Query("SELECT id, customItem1__c FROM customEntity18__c WHERE customItem1__c = " + accountId);
            if (selfAccountList != null && selfAccountList.size() > 0) {
                throw new ScriptBusinessException("一个客户下只能有一个自助通账号。");
            }
        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
            throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
