package other.quotachange.monthlysettlement;

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
 * 申请月度结算
 * 月结申请提交校验
 *
 * @author gongqiang
 */
public class MonthlySettlementApplication implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    /**
     * 应急充值的业务类型编码
     */
    private static final Long ENTITY_TYPE = 844307173769871L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("额度变更申请-月结申请提交校验-开始");
        Long id = null;
        Long belongId = null;
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long entityType = model.getLong("entityType");
            id = model.getLong("id");
            belongId = model.getLong("belongId");
            //业务类型等于月度结算
            if (ENTITY_TYPE.equals(entityType)) {
                String apiAccount = model.getString("customItem34__c");
                Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccount, 2);
                if (balance != null) {
                    if (balance != 0L) {
                        throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,还有余额：" + balance + " ,无法提交月结申请。");
                    }
                } else {
                    throw new ScriptBusinessException("Api主账号：" + apiAccount + " ,未查询到余额信息。");
                }
            }
        } catch (Exception ex) {
            logger.error("额度变更申请-月结申请提交校验,错误Exception：" + ex.toString());
            //throw new ScriptBusinessException(ex.getMessage());
            try {
                if (id != null && belongId != null) {
                    CommonHttpClient client = new CommonHttpClient();
                    //审批驳回
                    CommonHttpHelper.Approval(client, id, belongId, ex.getMessage(), false, null, null, null);
                }
            } catch (Exception e) {
                logger.error("额度变更申请-月结申请提交校验,审批驳回错误Exception：" + e.getMessage());
            }
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
