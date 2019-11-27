package other.repaymentcheck.approved;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity43__c;
import com.rkhd.platform.sdk.data.model.OrderProduct;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.BatchOperateResult;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import java.util.List;

/**
 * 还款核收(短信业务)
 * 审批通过
 *
 * @author gongqiang
 */
public class RepaymentCheckApproved implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("还款核收(短信业务)-审批通过-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long id = model.getLong("id");
            //查询还款核收明细（短信业务）
            String sql = "SELECT id, customItem9__c, customItem11__c, customItem21__c FROM customEntity43__c WHERE customItem9__c = " + id;
            logger.info("查询还款核收明细SQL:" + sql);
            QueryResult queryResult = XObjectService.instance().query(sql, true);
            logger.info("查询还款核收明细结果" + JSONObject.toJSONString(queryResult));
            if (queryResult.getSuccess()) {
                List<CustomEntity43__c> repaymentList = queryResult.getRecords();
                if (repaymentList != null && repaymentList.size() > 0) {
                    for (CustomEntity43__c repayment : repaymentList) {
                        //订单明细ID不能为空
                        if (repayment.getCustomItem11__c() != null) {
                            //查询订单明细
                            sql = "SELECT id, customItem176__c, name FROM orderProduct WHERE id = " + repayment.getCustomItem11__c();
                            queryResult = XObjectService.instance().query(sql, true);
                            logger.info("查询订单明细结果" + JSONObject.toJSONString(queryResult));
                            if (queryResult.getSuccess()) {
                                List<OrderProduct> orderProductList = queryResult.getRecords();
                                if (orderProductList != null && orderProductList.size() > 0) {
                                    repayment.setCustomItem21__c(orderProductList.get(0).getCustomItem176__c());
                                }
                            }
                        }
                    }
                    logger.info("更新还款金额信息:" + JSONObject.toJSONString(repaymentList));
                    //更新还款金额
                    BatchOperateResult batchOperateResult = XObjectService.instance().update(repaymentList, true, true);
                    logger.info("更新还款金额结果:" + JSONObject.toJSONString(batchOperateResult));
                    if (batchOperateResult.getSuccess()) {
                        logger.info("还款核收(短信业务)ID:" + id + ", 更新还款核收明细的还款金额成功。");
                    } else {
                        logger.error("还款核收(短信业务)ID:" + id + ", 更新还款核收明细的还款金额失败。原因:" + batchOperateResult.getErrorMessage());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("还款核收(短信业务)-审批通过,错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }

}
