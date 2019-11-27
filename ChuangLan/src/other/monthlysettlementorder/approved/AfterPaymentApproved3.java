package other.monthlysettlementorder.approved;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity40__c;
import com.rkhd.platform.sdk.data.model.OrderProduct;
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
 * 预付销账(短信业务)
 * 后款销账
 * 审批通过
 *
 * @author gongqiang
 */
public class AfterPaymentApproved3 implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("预付销账(短信业务)-后款销账-审批通过-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long id = model.getLong("id");
            //查询月结订单明细
            String sql = "SELECT id, customItem17__c, customItem1__c FROM customEntity40__c WHERE  customItem1__c=" + id;
            QueryResult queryResult = XObjectService.instance().query(sql, true);
            logger.info("月结订单（预付销账）查询月结订单明细：" + JSONObject.toJSONString(queryResult));
            if (queryResult.getSuccess()) {
                List<CustomEntity40__c> orderDetailsList = queryResult.getRecords();
                if (orderDetailsList != null && orderDetailsList.size() > 0) {
                    //月结订单明细循环更新
                    for (CustomEntity40__c orderDetail : orderDetailsList) {
                        if (orderDetail.getCustomItem17__c() != null) {
                            //查询月结订单明细对应的月结账号
                            sql = "SELECT id, customItem174__c FROM orderProduct WHERE id = " + orderDetail.getCustomItem17__c();
                            queryResult = XObjectService.instance().query(sql, true);
                            logger.info("月结订单明细（预付销账）查询月结账单：" + JSONObject.toJSONString(queryResult));
                            if (queryResult.getSuccess()) {
                                List<OrderProduct> billList = queryResult.getRecords();
                                if (billList != null && billList.size() > 0) {
                                    OrderProduct bill = billList.get(0);
                                    //结算状态：1已结清，2未结清
                                    bill.setCustomItem174__c(1);
                                    OperateResult operateResult = XObjectService.instance().update(bill, true);
                                    logger.info("更新月结账单（预付销账）结果：" + JSONObject.toJSONString(operateResult));
                                    if (operateResult.getSuccess()) {
                                        logger.info("预付销账ID：" + id + " 审批通过，月结订单明细id：" + orderDetail.getId() + ", 月结账单ID：" + bill.getId() + " 更新 结算状态 为已付清");
                                    } else {
                                        logger.error("预付销账ID：" + id + " 审批通过，月结订单明细id：" + orderDetail.getId() + ", 月结账单ID：" + bill.getId() + " 更新失败，原因：" + operateResult.getErrorMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("预付销账(短信业务)-后款销账-审批通过 错误Exception：" + ex.toString());
            throw new ScriptBusinessException("预付销账(短信业务)-后款销账-审批通过 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
