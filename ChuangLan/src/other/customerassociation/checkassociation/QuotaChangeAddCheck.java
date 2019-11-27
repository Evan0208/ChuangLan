package other.customerassociation.checkassociation;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity35__c;
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
 * 额度变更申请
 * 新增转数额
 * 检查客户关联申请
 *
 * @author gongqiang
 */
public class QuotaChangeAddCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 转条数、金额的业务类型编码
     */
    private static final Long ENTITY_TYPE = 830100998308497L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("额度变更申请-新增转数额检查客户关联申请-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long entityType = model.getLong("entityType");
            if (ENTITY_TYPE.equals(entityType)) {
                Long id = model.getLong("id");
                Long apiAccountId = model.getLong("customItem31__c");
                Long apiAccountId2 = model.getLong("customItem33__c");
                String sql = "SELECT customItem28__c,customItem10__c FROM customEntity35__c WHERE id = " + apiAccountId + " OR id = " + apiAccountId2 + "";
                QueryResult queryResult = XObjectService.instance().query(sql, true);
                logger.info("查询转入传出API账户对应得客户信息：" + sql + " , 返回信息" + JSONObject.toJSONString(queryResult) + "");
                if (queryResult.getSuccess()) {
                    List<CustomEntity35__c> apiAccountList = queryResult.getRecords();
                    if (apiAccountList != null && apiAccountList.size() > 0 && apiAccountList.size() == 2) {
                        Long accountId = apiAccountList.get(0).getCustomItem28__c();
                        Long accountId2 = apiAccountList.get(1).getCustomItem28__c();
                        String accountName = apiAccountList.get(0).getCustomItem10__c();
                        String accountName2 = apiAccountList.get(1).getCustomItem10__c();

                        logger.info("客户名称分别是：" + accountName + " , " + accountName2 + "");
                        if (StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountName2)) {
                            //如果两个客户名称相同则不在校验关联申请
                            if (accountName.equals(accountName2)) {
                                return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
                            }
                        }

                        sql = "SELECT id, customItem11__c, customItem12__c FROM customEntity9__c WHERE ((customItem11__c = " + accountId + " AND customItem11__c = " + accountId2 + ") OR (customItem11__c = " + accountId2 + " AND customItem11__c = " + accountId + ")) AND (customItem15__c = 3 OR approvalStatus = 3)";
                        queryResult = XObjectService.instance().query(sql, true);
                        logger.info("查询客户关联sql:" + sql + " ,返回结果：" + JSONObject.toJSONString(queryResult));
                        if (queryResult.getSuccess()) {
                            List<CustomEntity9__c> associationList = queryResult.getRecords();
                            if (associationList != null && associationList.size() > 0) {
                                logger.info("额度变更申请ID：" + id + ",客户ID：" + accountId + " ,客户2ID：" + accountId2 + " ,存在客户关联申请。");
                            } else {
                                throw new ScriptBusinessException("转入API主账号的客户和转出API主账号的客户不是关联客户！！");
                            }
                        } else {
                            throw new ScriptBusinessException("转入API主账号的客户和转出API主账号的客户不是关联客户！！");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("额度变更申请-新增转数额检查客户关联申请,错误Exception：" + ex.toString());
            throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
