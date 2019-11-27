package other.account.transfercheck;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.Account;
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
 * 客户转移检验
 *
 * @author gongqiang
 */
public class TransferCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户转移检验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            //修改同步状态为未同步
            model.setAttribute("synchState__c", 1);

            Account contact = new Account();
            contact.setId(model.getLong("id"));
            contact.setSynchState__c(1);
            OperateResult operateResult = XObjectService.instance().update(contact, true);
            if (operateResult.getSuccess()) {
                logger.info("转移客户 " + model.getLong("id") + " 同步状态成功：" + operateResult.getErrorMessage());
            } else {
                logger.error("转移客户 " + model.getLong("id") + " 同步状态失败：" + operateResult.getErrorMessage());
            }


        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());

        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }

}
