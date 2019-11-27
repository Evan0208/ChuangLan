package other.quotachange.emergencyrecharge;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import commons.utils.CommonHttpHelper;
import commons.utils.DateTimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 额度变更申请
 * 应急充值
 * 应急充值提交校验
 *
 * @author gongqiang
 */
public class EmergencyRechargeCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    /**
     * 应急充值的业务类型编码
     */
    private static final Long ENTITY_TYPE = 830096426894046L;

    /**
     * 限制次数的产品BU
     */
    private static final List<String> PRODUCT_LIMIT_LIST = new ArrayList<String>() {{
        add("国内短信");
        add("国际短信");
    }};

    /**
     * 每月提交上限
     */
    private static final Long MONTHLY_UPPER_LIMIT = 30L;


    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("额度变更申请-应急充值提交校验-开始");
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
                //API主账号
                Long apiAccount = model.getLong("customItem31__c");
                //API主账号文本
                String apiAccountStr = model.getString("customItem34__c");
                //API主账号产品
                String ApiAccountProduct = model.getString("customItem58__c");
                //属于国内短信和国际短信
                if (PRODUCT_LIMIT_LIST.contains(ApiAccountProduct)) {
                    long nowMonth = DateTimeHelper.getNowMonth();
                    long nextMonth = DateTimeHelper.getNowNextMonth();
                    String sql = "SELECT id FROM customEntity8__c WHERE (entityType = 830096426894046 AND (approvalStatus = 1 OR approvalStatus = 3) AND createdAt > " + nowMonth + " AND createdAt < " + nextMonth + " AND customItem31__c = " + apiAccount + ") ";
                    QueryResult queryResult = XObjectService.instance().query(sql, true);
                    logger.info("查询sql: " + sql + " ,返回结果：" + JSONObject.toJSONString(queryResult));
                    if (queryResult.getSuccess()) {
                        if (queryResult.getTotalCount() >= MONTHLY_UPPER_LIMIT) {
                            throw new ScriptBusinessException("API主账号：" + apiAccountStr + "本月已经申请应急充值次数较多，此次无法提交。");
                        }
                    } else {
                        logger.error("额度变更申请-应急充值提交校验,查询失败：" + queryResult.getErrorMessage());
                    }
                }

                //只要不报错就直接审批通过
                try {
                    CommonHttpClient client = new CommonHttpClient();
                    //审批通过
                    CommonHttpHelper.Approval(client, id, belongId, "审批通过", true, null, null, entityType);
                } catch (Exception e) {
                    logger.error("额度变更申请-应急充值提交校验,审批通过错误Exception：" + e.getMessage());
                }

            }
        } catch (Exception ex) {
            logger.error("额度变更申请-应急充值提交校验,错误Exception：" + ex.toString());
            try {
                if (id != null && belongId != null) {
                    CommonHttpClient client = new CommonHttpClient();
                    //审批驳回
                    CommonHttpHelper.Approval(client, id, belongId, ex.getMessage(), false, null, null, null);
                }
            } catch (Exception e) {
                logger.error("额度变更申请-应急充值提交校验,审批驳回错误Exception：" + e.getMessage());
            }
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
