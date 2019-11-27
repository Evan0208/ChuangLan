package other.monthlysettlementorder.approved;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity42__c;
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
 * 退款出账(短信业务)
 * 审批通过
 *
 * @author gongqiang
 */
public class RefundApproved implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("退款出账(短信业务)-审批通过-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long id = model.getLong("id");
            //查询退款出账明细（短信业务）
            String sql = "SELECT id, customItem4__c, customItem1__c FROM customEntity42__c WHERE  customItem1__c=" + id;
            QueryResult queryResult = XObjectService.instance().query(sql, true);
            logger.info("退款出账查询退款出账明细：" + JSONObject.toJSONString(queryResult));
            if (queryResult.getSuccess()) {
                List<CustomEntity42__c> orderDetailsList = queryResult.getRecords();
                if (orderDetailsList != null && orderDetailsList.size() > 0) {

                    for (CustomEntity42__c orderDetail : orderDetailsList) {
                        if (orderDetail.getCustomItem4__c() != null) {
                            //查询月结订单明细对应的月结账号
                            sql = "SELECT id, customItem173__c FROM orderProduct WHERE id = " + orderDetail.getCustomItem4__c();
                            queryResult = XObjectService.instance().query(sql, true);
                            logger.info("退款出账明细查询订单明细：" + JSONObject.toJSONString(queryResult));
                            if (queryResult.getSuccess()) {
                                List<OrderProduct> billList = queryResult.getRecords();
                                if (billList != null && billList.size() > 0) {
                                    OrderProduct bill = billList.get(0);
                                    //是否操作过退款：1已结清，2未结清
                                    bill.setCustomItem173__c(1);
                                    OperateResult operateResult = XObjectService.instance().update(bill, true);
                                    logger.info("更新订单明细结果：" + JSONObject.toJSONString(operateResult));
                                    if (operateResult.getSuccess()) {
                                        logger.info("退款出账ID：" + id + " 审批通过，退款出账明细id：" + orderDetail.getId() + ", 订单明细ID：" + bill.getId() + " 更新 是否操作过退款 为是");
                                    } else {
                                        logger.error("退款出账ID：" + id + " 审批通过，退款出账明细id：" + orderDetail.getId() + ", 订单明细ID：" + bill.getId() + " 更新失败，原因：" + operateResult.getErrorMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("退款出账(短信业务)-审批通过 错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
