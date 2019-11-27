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
 * 月结订单
 * 后款销账
 *
 * @author gongqiang
 */
public class AfterPayment implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final Long ENTITY_TYPE = 946128594109129L;

    private static final Long ENTITY_TYPE_ = 954297149850346L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("月结订单-后款销账-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long entityType = model.getLong("entityType");
            //国内短信或国际短信销账
            if (ENTITY_TYPE.equals(entityType) || ENTITY_TYPE_.equals(entityType)) {
                //API主账号
                String apiAccount = model.getString("customItem51__c");
                //调整条数
                Double adjustmentNumber = model.getDouble("customItem49__c");
                //是否提交
                Boolean submitFlag = model.getBoolean("customItem62__c");
                if (submitFlag) {
                    if (adjustmentNumber > 0) {
                        //查询API主账号余额
                        Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
                        if (balance != null) {
                            if (balance < adjustmentNumber) {
                                throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,查询余额:" + balance + " , 调整条数大于API余额。");
                            }
                        } else {
                            throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,未查询到余额信息。");
                        }
                    } else {
                        throw new ScriptBusinessException("调整条数必须大于0");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("月结订单-后款销账 错误Exception：" + ex.toString());
            throw new ScriptBusinessException("月结订单-后款销账 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }


}
