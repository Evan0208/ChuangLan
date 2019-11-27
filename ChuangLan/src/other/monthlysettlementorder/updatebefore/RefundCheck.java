package other.monthlysettlementorder.updatebefore;

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
 *退款出账(短信业务)
 *退款出账
 * @author gongqiang
 */
public class RefundCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("退款出账(短信业务)-退款出账校验-开始");
        try {

            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //是否提交
            Boolean submitFlag = model.getBoolean("customItem59__c");
            //退款数量
            Double refundAmount = model.getDouble("customItem50__c");
            //API主账号
            String apiAccount = model.getString("customItem39__c");
            if (submitFlag) {
                if (refundAmount > 0) {
                    //查询API主账号余额
                    Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
                    if (balance != null) {
                        if (balance < refundAmount) {
                            throw new ScriptBusinessException("API主账号：" + apiAccount + " ,查询余额:" + balance + " , 退款数量大于API余额。");
                        }
                    } else {
                        throw new ScriptBusinessException("API主账号：" + apiAccount + " ,未查询到余额信息。");
                    }
                } else {
                    throw new ScriptBusinessException("销账数量必须大于0");
                }
            }
        } catch (Exception ex) {
            logger.error("退款出账(短信业务)-退款出账校验 错误Exception：" + ex.toString());
            throw new ScriptBusinessException("退款出账(短信业务)-退款出账校验 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
