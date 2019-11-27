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
 * 预付销账(短信业务)
 * 后款销账
 *
 * @author gongqiang
 */
public class AfterPayment3 implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final Long ENTITY_TYPE = 872888674812551L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("预付销账(短信业务)-后款销账-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long entityType = model.getLong("entityType");
            //销账申请
            if (ENTITY_TYPE.equals(entityType)) {
                //API主账号
                String apiAccount = model.getString("customItem44__c");
                //销账数量
                Double repayment = model.getDouble("customItem32__c");
                //是否提交
                Boolean submitFlag = model.getBoolean("customItem63__c");
                if (submitFlag) {
                    if (repayment > 0) {
                        //查询API主账号余额
                        Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
                        if (balance != null) {
                            if (balance < repayment) {
                                throw new ScriptBusinessException("API主账号：" + apiAccount + " ,查询余额:" + balance + " , 销账数量大于API余额。");
                            }
                        } else {
                            throw new ScriptBusinessException("API主账号：" + apiAccount + " ,未查询到余额信息。");
                        }
                    } else {
                        throw new ScriptBusinessException("销账数量必须大于0");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("预付销账(短信业务)-后款销账 错误Exception：" + ex.toString());
            throw new ScriptBusinessException("预付销账(短信业务)-后款销账 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }


}
