package other.customerassociation.checkassociation;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity9__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 客户发票信息
 * 新增前
 * 检查客户关联申请
 *
 * @author gongqiang
 */
public class InvoiceAddCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户发票信息-新增检查客户关联申请-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long id = model.getLong("id");

            Long accountId = model.getLong("customItem1__c");

            Long accountId2 = model.getLong("customItem11__c");

            String accountName = model.getString("customItem20__c");

            String accountName2 = model.getString("customItem16__c");

            logger.info("客户名称分别是：" + accountName + " , " + accountName2 + "");
            if (StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountName2)) {
                //如果两个客户名称相同则不在校验关联申请
                if (accountName.equals(accountName2)) {
                    return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
                }
            }

            String sql = "SELECT id, customItem11__c, customItem12__c FROM customEntity9__c WHERE ((customItem11__c = " + accountId + " AND customItem11__c = " + accountId2 + ") OR (customItem11__c = " + accountId2 + " AND customItem11__c = " + accountId + ")) AND (customItem15__c = 3 OR approvalStatus = 3)";
            QueryResult queryResult = XObjectService.instance().query(sql, true);
            logger.info("查询客户关联sql:" + sql + " ,返回结果：" + JSONObject.toJSONString(queryResult));
            if (queryResult.getSuccess()) {
                List<CustomEntity9__c> associationList = queryResult.getRecords();
                if (associationList != null && associationList.size() > 0) {
                    logger.info("客户发票信息ID：" + id + ",客户ID：" + accountId + " ,客户2ID：" + accountId2 + " ,存在客户关联申请。");
                } else {
                    throw new ScriptBusinessException("该发票抬头和该客户不是关联客户！");
                }
            } else {
                throw new ScriptBusinessException("该发票抬头和该客户不是关联客户！");
            }
        } catch (Exception ex) {
            logger.error("客户发票信息-新增检查客户关联申请,错误Exception：" + ex.toString());
            throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
