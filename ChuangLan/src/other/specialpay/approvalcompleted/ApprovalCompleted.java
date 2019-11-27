package other.specialpay.approvalcompleted;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity35__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.OperateResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;
import commons.utils.XobjectHelper;

import java.util.List;

/**
 * 特殊计费审批完成脚本代码
 *
 * @author gongqiang
 */
public class ApprovalCompleted implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("特殊计费审批完成-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            long id = model.getLong("id");
            //申请类型(1提交计费2成功计费3月结)
            long applicationType = model.getLong("customItem8__c");
            //APi主账号Id
            long apiAccountId = model.getLong("customItem4__c");

            List<CustomEntity35__c> apiAccountList = XobjectHelper.v2Query("SELECT id, customItem1__c, customItem11__c, customItem12__c FROM customEntity35__c WHERE id = " + apiAccountId);
            logger.info("特殊计费对应得API账号信息：" + apiAccountList.toString());
            if (apiAccountList != null && apiAccountList.size() > 0) {
                CustomEntity35__c apiAccount = apiAccountList.get(0);
                if (applicationType == 1) {
                    //计费类型（提交计费）
                    apiAccount.setCustomItem11__c(1);
                } else if (applicationType == 2) {
                    //计费类型（成功计费）
                    apiAccount.setCustomItem11__c(2);
                } else {
                    //付费类型(月结)
                    apiAccount.setCustomItem12__c(2);
                }

                OperateResult operateResult = XObjectService.instance().update(apiAccount);
                if (operateResult.getSuccess()) {
                    logger.info("特殊计费Id:" + id + " 审批完成，回写API主账号计费类型或付费类型成功，");
                } else {
                    logger.error("特殊计费id:" + id + "审批完成，回写API主账号计费类型或付费类型失败，原因:" + operateResult.getErrorMessage());
                }
            }
        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());

        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
