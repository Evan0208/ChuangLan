package other.monthlysettlementorder.approved;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity21__c;
import com.rkhd.platform.sdk.data.model.CustomEntity63__c;
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
 * 月结订单(万数闪验)
 * 后款销账
 * 审批通过
 *
 * @author gongqiang
 */
public class AfterPaymentApproved2 implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("月结订单(万数闪验)-后款销账-审批通过-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long id = model.getLong("id");
            //查询月结订单明细
            String sql = "SELECT id, customItem2__c, customItem1__c FROM customEntity63__c WHERE  customItem1__c=" + id;
            QueryResult queryResult = XObjectService.instance().query(sql, true);
            logger.info("月结订单（万数闪验）查询月结订单明细：" + JSONObject.toJSONString(queryResult));
            if (queryResult.getSuccess()) {
                List<CustomEntity63__c> orderDetailsList = queryResult.getRecords();
                if (orderDetailsList != null && orderDetailsList.size() > 0) {

                    for (CustomEntity63__c orderDetail : orderDetailsList) {
                        if (orderDetail.getCustomItem2__c() != null) {
                            //查询月结订单明细对应的月结账号
                            sql = "SELECT id, customItem20__c FROM customEntity21__c WHERE id = " + orderDetail.getCustomItem2__c();
                            queryResult = XObjectService.instance().query(sql, true);
                            logger.info("月结订单明细（万数闪验）查询月结账单：" + JSONObject.toJSONString(queryResult));
                            if (queryResult.getSuccess()) {
                                List<CustomEntity21__c> billList = queryResult.getRecords();
                                if (billList != null && billList.size() > 0) {
                                    CustomEntity21__c bill = billList.get(0);
                                    //结算状态：1已结清，2未结清
                                    bill.setCustomItem20__c(1);
                                    OperateResult operateResult = XObjectService.instance().update(bill, true);
                                    logger.info("更新月结账单（万数闪验）结果：" + JSONObject.toJSONString(operateResult));
                                    if (operateResult.getSuccess()) {
                                        logger.info("月结订单（万数闪验）ID：" + id + " 审批通过，月结订单明细id：" + orderDetail.getId() + ", 月结账单ID：" + bill.getId() + " 更新 结算状态 为已付清");
                                    } else {
                                        logger.error("月结订单（万数闪验）ID：" + id + " 审批通过，月结订单明细id：" + orderDetail.getId() + ", 月结账单ID：" + bill.getId() + " 更新失败，原因：" + operateResult.getErrorMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("月结订单(万数闪验)-后款销账-审批通过 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
