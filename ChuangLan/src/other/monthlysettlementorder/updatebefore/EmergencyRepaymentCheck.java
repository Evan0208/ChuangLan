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
 * 出账(万数闪验)
 * 应急还款
 *
 * @author gongqiang
 */
public class EmergencyRepaymentCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 应急还款
     */
    private static final Long ENTITY_TYPE = 937515576656549L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("出账(万数闪验)-应急还款-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long entityType = model.getLong("entityType");
            if (ENTITY_TYPE.equals(entityType)) {
                //自助通账号
                String selfServiceAccount = model.getString("customItem2__c");
                //应还金额
                Double amountDue = model.getDouble("customItem22__c");
                //是否提交
                Boolean submitFlag = model.getBoolean("customItem17__c");
                if (submitFlag) {
                    if (amountDue > 0) {
                        //查询API主账号余额
                        Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(selfServiceAccount, 1);
                        if (balance != null) {
                            if (balance < amountDue) {
                                throw new ScriptBusinessException("自助通账号v：" + selfServiceAccount + " ,查询余额:" + balance + " , 应还金额大于自助通余额。");
                            }
                        } else {
                            throw new ScriptBusinessException("自助通账号v：" + selfServiceAccount + " ,未查询到余额信息。");
                        }
                    } else {
                        throw new ScriptBusinessException("应还金额v必须大于0");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("出账(万数闪验)-应急还款 错误Exception：" + ex.toString());
            throw new ScriptBusinessException("出账(万数闪验)-应急还款 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
