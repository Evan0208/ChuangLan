package other.selfserviceaccounts.updatecheck;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity18__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;

/**
 * 自助通账号 更新手机号区号和 自助通账户
 *
 * @author gongqiang
 */
public class UpdateCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("自助通账号更新校验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long id = model.getLong("id");
            CustomEntity18__c selfAccount = new CustomEntity18__c();
            selfAccount.setId(id);
            selfAccount = XObjectService.instance().get(selfAccount, true);
            if (selfAccount != null) {
                //手机区号
                Long customItem38__c = model.getLong("customItem38__c");
                //自助通账户
                String customItem18__c = model.getString("customItem18__c");

                if (customItem38__c != null && !customItem18__c.equals(selfAccount.getCustomItem38__c())) {
                    //是否处理
                    model.setAttribute("customItem50__c", 1);
                }

                if (customItem18__c != null && !customItem18__c.equals(selfAccount.getCustomItem18__c())) {
                    //是否处理
                    model.setAttribute("customItem50__c", 1);
                }
            }
        } catch (Exception ex) {
            logger.error("自助通账号更新校验，错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
