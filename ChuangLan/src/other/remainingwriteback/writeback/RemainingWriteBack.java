package other.remainingwriteback.writeback;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import commons.utils.ChuangLanCRMHttpUtil;


import java.util.List;

/**
 * 剩余数量回写
 * 实体：退款出账明细(短信业务)
 * 保存之前触发
 *
 * @author gongqiang
 */
public class RemainingWriteBack implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("退款出账明细(短信业务)-剩余数量回写-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            String apiAccount = model.getString("customItem12__c");
            //查询API账号余额
            Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
            logger.info("退款出账明细(短信业务) 保存，查询API账号" + apiAccount + " 的余额：" + balance);
            if (balance != null) {
                //剩余数量
                model.setAttribute("customItem17__c", balance);
            } else {
                logger.error("退款出账明细(短信业务) 保存，查询API账号" + apiAccount + " 的余额失败。");
            }
        } catch (Exception ex) {
            logger.error("退款出账明细(短信业务)-剩余数量回写,错误Exception：" + ex.getMessage().toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
