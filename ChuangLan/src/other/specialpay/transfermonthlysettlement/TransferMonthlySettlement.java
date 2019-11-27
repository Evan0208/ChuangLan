package other.specialpay.transfermonthlysettlement;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity18__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.OperateResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;

/**
 * 万数闪验转月结申请
 * 审批通过
 *
 * @author gongqiang
 */
public class TransferMonthlySettlement implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("万数闪验转月结申请审批完成-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            //自助通账号
            Long selfAccountId = model.getLong("customItem1__c");
            if (selfAccountId != null) {
                CustomEntity18__c selfAccount = new CustomEntity18__c();
                selfAccount.setId(selfAccountId);
                selfAccount = XObjectService.instance().get(selfAccount, true);
                if (selfAccount != null) {
                    selfAccount.setCustomItem45__c(2);
                    OperateResult operateResult = XObjectService.instance().update(selfAccount, true);
                    if (operateResult != null && operateResult.getSuccess()) {
                        logger.info ("万数闪验转月结申请审批完成-自助通账号ID：" + selfAccount.getId() + " ,修改成功");
                    } else {
                        logger.error("万数闪验转月结申请审批完成-自助通账号ID：" + selfAccount.getId() + " ,修改万数付费类型为月结失败，错误：" + operateResult.getErrorMessage());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("万数闪验转月结申请审批完成-错误Exception：" + ex.toString());

        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
