package other.quotachange.deductionnumberamount;

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
 * <p>
 * 数额扣除提交校验
 *
 * @author gongqiang
 */
public class DeductionNumberAmountCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 数额扣除的业务类型编码
     */
    private static final Long ENTITY_TYPE = 830100998308497L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("额度变更申请-数额扣除提交校验-开始");
        Long id = null;
        Long belongId = null;
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            id = model.getLong("id");
            belongId = model.getLong("belongId");
            Long entityType = model.getLong("entityType");
            if (ENTITY_TYPE.equals(entityType)) {
                String apiAccount = model.getString("customItem34__c");
                Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
                if (balance != null) {
                    //申请扣除条数
                    Double deductionNumber = model.getDouble("customItem61__c");
                    if (deductionNumber == null) {
                        throw new ScriptBusinessException("申请扣除条数不能为空。");
                    } else {
                        if (deductionNumber.doubleValue() > balance.longValue()) {
                            throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,余额:" + balance + " ,无法申请：" + deductionNumber + " 数量的扣除。");
                        }
                    }
                } else {
                    throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,未查询到余额信息。");
                }
            }
        } catch (Exception ex) {
            logger.error("额度变更申请-数额扣除提交校验,错误Exception：" + ex.getMessage().toString());

            try {
                if (id != null && belongId != null) {
                    CommonHttpClient client = new CommonHttpClient();
                    //审批驳回
                    CommonHttpHelper.Approval(client, id, belongId, ex.getMessage(), false, null, null, null);
                }
            } catch (Exception e) {
                logger.error("额度变更申请-数额扣除提交校验,审批驳回错误Exception：" + e.getMessage());
            }
            //throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
