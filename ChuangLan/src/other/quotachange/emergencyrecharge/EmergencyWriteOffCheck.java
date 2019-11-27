package other.quotachange.emergencyrecharge;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import commons.utils.ChuangLanCRMHttpUtil;
import commons.utils.CommonHttpHelper;

import java.util.List;

/**
 * 额度变更申请
 * 应急充值
 * 应急充值销账提交校验
 *
 * @author gongqiang
 */
public class EmergencyWriteOffCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 应急充值的业务类型编码
     */
    private static final Long ENTITY_TYPE = 830096426894046L;

    private static final Integer RECHARGE_TYPE = 2;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("额度变更申请-应急充值销账提交校验-开始");
        Long id = null;
        Long belongId = null;
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            id = model.getLong("id");
            belongId = model.getLong("belongId");
            Long entityType = model.getLong("entityType");
            //业务类型等于应急充值
            if (ENTITY_TYPE.equals(entityType)) {
                //应急充值类型
                Integer rechargeType = model.getInteger("customItem82__c");
                //归还
                if (RECHARGE_TYPE.equals(rechargeType))
                {
                    String apiAccount = model.getString("customItem34__c");
                    Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
                    if (balance != null) {
                        //充值数额
                        Double deductionNumber = model.getDouble("customItem5__c");
                        if (deductionNumber == null) {
                            throw new ScriptBusinessException("申请充值数额不能为空。");
                        } else {
                            if (deductionNumber.doubleValue() > balance.longValue()) {
                                throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,余额:" + balance + " ,无法申请：" + deductionNumber + " 数量的充值数额。");
                            }
                        }
                    } else {
                        throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,未查询到余额信息。");
                    }

                    //只要不报错就直接审批通过
                    try {
                        CommonHttpClient client = new CommonHttpClient();
                        //审批通过
                        CommonHttpHelper.Approval(client, id, belongId, "审批通过", true, null, null, entityType);
                    } catch (Exception e) {
                        logger.error("额度变更申请-应急充值(销账)提交校验,审批通过错误Exception：" + e.getMessage());
                    }

                }
            }
        } catch (Exception ex) {
            logger.error("额度变更申请-应急充值(销账)提交校验,错误Exception：" + ex.toString());
            try {
                if (id != null && belongId != null) {
                    CommonHttpClient client = new CommonHttpClient();
                    //审批驳回
                    CommonHttpHelper.Approval(client, id, belongId, ex.getMessage(), false, null, null, null);
                }
            } catch (Exception e) {
                logger.error("额度变更申请-应急充值(销账)提交校验,审批驳回错误Exception：" + e.getMessage());
            }
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
