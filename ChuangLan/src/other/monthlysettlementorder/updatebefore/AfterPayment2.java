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
 * 月结订单(万数闪验)
 * 后款销账
 *
 * @author gongqiang
 */
public class AfterPayment2 implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final Long ENTITY_TYPE = 956313347834533L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("月结订单(万数闪验)-后款销账-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long entityType = model.getLong("entityType");
            //万数闪验平台
            if (ENTITY_TYPE.equals(entityType)) {
                //自助通账号(ERPID)
                String selfServiceAccounts = model.getString("customItem22__c");
                //合计还款
                Double repayment = model.getDouble("customItem10__c");
                //是否提交
                Boolean submitFlag = model.getBoolean("customItem12__c");
                if (submitFlag) {
                    if (repayment > 0) {
                        //查询API主账号余额
                        Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(selfServiceAccounts, 1);
                        if (balance != null) {
                            if (balance < repayment) {
                                throw new ScriptBusinessException("自助通账号：" + selfServiceAccounts + " ,查询余额:" + balance + " , 合计还款大于API余额。");
                            }
                        } else {
                            throw new ScriptBusinessException("自助通账号：" + selfServiceAccounts + " ,未查询到余额信息。");
                        }
                    } else {
                        throw new ScriptBusinessException("合计还款必须大于0");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("月结订单(万数闪验)-后款销账 错误Exception：" + ex.toString());
            throw new ScriptBusinessException("月结订单(万数闪验)-后款销账 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }


}
