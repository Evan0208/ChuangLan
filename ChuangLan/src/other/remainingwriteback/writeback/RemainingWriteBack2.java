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
 * 实体：出账(万数闪验)
 * 保存之前触发
 *
 * @author gongqiang
 */
public class RemainingWriteBack2 implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 退款申请的业务类型编码
     */
    private static final Long ENTITY_TYPE = 936087441736332L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("出账(万数闪验)-剩余数量回写-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long entityType = model.getLong("entityType");

            String apiAccount = model.getString("customItem2__c");
            //查询API账号余额
            Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 1);
            logger.info("出账(万数闪验) 保存，查询自助通账号" + apiAccount + " 的余额：" + balance);
            if (balance != null) {
                //剩余数量
                model.setAttribute("customItem8__c", balance);
            } else {
                logger.error("出账(万数闪验)保存，查询自助通账号" + apiAccount + " 的余额失败。");
            }

        } catch (Exception ex) {
            logger.error("出账(万数闪验)-剩余数量回写,错误Exception：" + ex.getMessage().toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
