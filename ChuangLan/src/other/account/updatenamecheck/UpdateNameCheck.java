package other.account.updatenamecheck;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.Account;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;

/**
 * 客户修改名称校验
 *
 * @author gongqiang
 */
public class UpdateNameCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户修改名称校验-开始");
        try {

            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户ID
            long id = model.getLong("id");
            logger.info("客户ID：" + id);
            //客户名称
            String accountName = model.getString("accountName");
            logger.info("客户名称：" + accountName);
            //是否已认证
            Integer accreditationFlag = model.getInteger("customItem200__c");
            logger.info("是否已认证：" + accreditationFlag);
            if (accreditationFlag != null && accreditationFlag.intValue() == 2) {
                RkhdHttpClient client = new RkhdHttpClient();
                //已经认证
                Account oldAccount = new Account();
                oldAccount.setId(id);
                oldAccount = XObjectService.instance().get(oldAccount,true);
                logger.info("旧的客户信息：" + oldAccount.toString());
                String oldAccountName = oldAccount.getAccountName();
                //新客户名称和旧的名称不一致
                if (!oldAccountName.equals(accountName)) {
                    throw new ScriptBusinessException("客户已经工商认证通过，不可修改客户名称");
                } else {
                    //修改客户同步状态为未同步
                    model.setAttribute("synchState__c", 1);
                }
            } else {
                //修改客户同步状态为未同步
                model.setAttribute("synchState__c", 1);
            }
        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
            throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
