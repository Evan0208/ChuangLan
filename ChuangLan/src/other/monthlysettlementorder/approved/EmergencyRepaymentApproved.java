package other.monthlysettlementorder.approved;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity56__c;
import com.rkhd.platform.sdk.data.model.CustomEntity66__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.OperateResult;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;

/**
 * 出账(万数闪验)
 * 应急还款
 * 审批通过
 *
 * @author gongqiang
 */
public class EmergencyRepaymentApproved implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("出账(万数闪验)-应急还款-审批通过-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long id = model.getLong("id");
            //查询应急充值明细（万数闪验）
            String sql = "SELECT id, customItem1__c, customItem2__c, name FROM customEntity66__c WHERE customItem1__c = " + id;
            QueryResult queryResult = XObjectService.instance().query(sql, true);
            logger.info("查询应急充值明细（万数闪验）：" + JSONObject.toJSONString(queryResult));
            if (queryResult.getSuccess()) {
                List<CustomEntity66__c> orderDetailsList = queryResult.getRecords();
                if (orderDetailsList != null && orderDetailsList.size() > 0) {

                    for (CustomEntity66__c orderDetail : orderDetailsList) {
                        if (orderDetail.getCustomItem2__c() != null) {
                            //查询入账（万数闪验）
                            sql = "SELECT id, customItem29__c, name FROM customEntity56__c WHERE id = " + orderDetail.getCustomItem2__c();
                            queryResult = XObjectService.instance().query(sql, true);
                            logger.info("查询入账（万数闪验）：" + JSONObject.toJSONString(queryResult));
                            if (queryResult.getSuccess()) {
                                List<CustomEntity56__c> billList = queryResult.getRecords();
                                if (billList != null && billList.size() > 0) {
                                    CustomEntity56__c bill = billList.get(0);
                                    //结算状态：1已结清，2未结清
                                    bill.setCustomItem29__c(1);
                                    OperateResult operateResult = XObjectService.instance().update(bill, true);
                                    logger.info("更新入账（万数闪验）结果：" + JSONObject.toJSONString(operateResult));
                                    if (operateResult.getSuccess()) {
                                        logger.info("出账(万数闪验)ID：" + id + " 审批通过，应急充值明细（万数闪验）id：" + orderDetail.getId() + ", 入账（万数闪验）ID：" + bill.getId() + " 更新 结算状态 为已付清");
                                    } else {
                                        logger.error("出账(万数闪验)ID：" + id + " 审批通过，应急充值明细（万数闪验）id：" + orderDetail.getId() + ", 入账（万数闪验）ID：" + bill.getId() + " 更新失败，原因：" + operateResult.getErrorMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("出账(万数闪验)-应急还款-审批通过 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
