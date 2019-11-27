package other.order.prepaidorderfirst;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;
import commons.utils.DateTimeHelper;

import java.util.List;

/**
 * 入账(万数闪验)
 * 是否首冲
 *
 * @author gongqiang
 */
public class PrepaidOrderFirst2 implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    private static final Long ENTITY_TYPE = 936066226930333L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("预付订单(万数闪验)-是否首冲状态回写-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long id = model.getLong("id");
            Long entityType = model.getLong("entityType");
            //业务类型等于下单
            if (ENTITY_TYPE.equals(entityType)) {
                //客户ID
                Long accountId = model.getLong("customItem6__c");
                //今天零点
                Long nowDate = DateTimeHelper.getDateNow();
                boolean firstOrderFlag = true;
                //查询今天之前是否存在审批通过的订单
                String sql = "SELECT id, accountId FROM _order WHERE (entityType = 735708796617355 AND customItem191__c  = 3 AND accountId = " + accountId + " AND customItem193__c < " + nowDate + ")";
                logger.info("查询今天之前是否存在审批通过的订单:" + sql);
                QueryResult queryResult = XObjectService.instance().query(sql, true);
                logger.info("查询今天之前是否存在审批通过的订单:" + JSONObject.toJSONString(queryResult));
                if (queryResult.getSuccess()) {
                    if (queryResult.getTotalCount() > 0) {
                        firstOrderFlag = false;
                    }
                } else {
                    logger.error("查询今天之前是否存在审批通过的订单错误：" + queryResult.getErrorMessage());
                }

                //查询入账(万数闪验)
                if (firstOrderFlag) {
                    sql = "SELECT id, customItem6__c FROM customEntity56__c WHERE (entityType = 936066226930333 AND customItem13__c = 3 AND customItem6__c = " + accountId + " AND customItem15__c < " + nowDate + ")";
                    logger.info("查询今天之前是否存在审批通过的入账(万数闪验):" + sql);
                    queryResult = XObjectService.instance().query(sql, true);
                    logger.info("查询今天之前是否存在审批通过的入账(万数闪验)::" + JSONObject.toJSONString(queryResult));
                    if (queryResult.getSuccess()) {
                        if (queryResult.getTotalCount() > 0) {
                            firstOrderFlag = false;
                        }
                    } else {
                        logger.error("查询今天之前是否存在审批通过的订单(万数闪验)错误：" + queryResult.getErrorMessage());
                    }
                }

                //查询月结订单(短信业务)
                if (firstOrderFlag) {
                    sql = "SELECT id, customItem1__c FROM customEntity46__c WHERE (customItem56__c = 3 AND customItem1__c = " + accountId + " AND customItem58__c < " + nowDate + ")";
                    logger.info("查询今天之前是否存在审批通过的月结订单(短信业务):" + sql);
                    queryResult = XObjectService.instance().query(sql, true);
                    logger.info("查询今天之前是否存在审批通过的月结订单(短信业务) :" + JSONObject.toJSONString(queryResult));
                    if (queryResult.getSuccess()) {
                        if (queryResult.getTotalCount() > 0) {
                            firstOrderFlag = false;
                        }
                    } else {
                        logger.error("查询今天之前是否存在审批通过的月结订单(短信业务) 错误：" + queryResult.getErrorMessage());
                    }
                }
                if (firstOrderFlag) {
                    //首充
                    model.setAttribute("customItem10__c", 1);
                    logger.info("客户ID:"+accountId+"首充 入账(万数闪验)");
                } else {
                    model.setAttribute("customItem10__c", 2);
                    logger.info("客户ID:"+accountId+"非首充 入账(万数闪验)");
                }
            }
        } catch (Exception ex) {
            logger.error("预付订单(万数闪验)-是否首冲状态回写 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
