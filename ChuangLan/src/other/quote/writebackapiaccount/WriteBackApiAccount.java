package other.quote.writebackapiaccount;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity35__c;
import com.rkhd.platform.sdk.data.model.QuoteLine;
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
 * 报价单（合同）
 * 回写API主账号
 *
 * @author gongqiang
 */
public class WriteBackApiAccount implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("报价单审批-回写API主账号-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户ID
            Long accountId = model.getLong("quotationEntityRelAccount");
            Long quoteId = model.getLong("id");
            if (quoteId == null) {
                throw new ScriptBusinessException("报价单ID为空");
            }
            //产品ID
            Long productId = null;
            QueryResult queryResult = XObjectService.instance().query("SELECT id, quotationDetailEntityRelProduct FROM quoteLine WHERE quotationDetailEntityRelQuotationEntity = " + quoteId, true);
            if (queryResult.getSuccess()) {
                List<QuoteLine> quoteLineList = queryResult.getRecords();
                if (quoteLineList != null && quoteLineList.size() > 0) {
                    //获取报价单明细中的产品ID
                    for (QuoteLine quoteLine : quoteLineList) {
                        productId = quoteLine.getQuotationDetailEntityRelProduct();
                        //循环报价单明细给每一个明细关联API主账号
                        Long apiAccountId = null;
                        QueryResult apiAccountQueryResult = XObjectService.instance().query("SELECT id, customItem3__c FROM customEntity35__c WHERE customItem27__c = 2 AND customItem6__c = " + productId + " AND customItem28__c = " + accountId + " ", true);
                        if (apiAccountQueryResult.getSuccess()) {
                            List<CustomEntity35__c> apiAccountList = apiAccountQueryResult.getRecords();
                            if (apiAccountList != null && apiAccountList.size() > 0) {
                                apiAccountId = apiAccountList.get(0).getId();
                            }
                        } else {
                            logger.error("报价单ID：" + quoteId + " 客户ID：" + accountId + " 产品ID：" + productId + " 查询API主账号信息错误，原因：" + apiAccountQueryResult.getErrorMessage());
                        }
                        if (apiAccountId == null) {
                            logger.error("报价单ID：" + quoteId + " 客户ID：" + accountId + " 产品ID：" + productId + " 未找到查询API主账号信息");
                        } else {
                            quoteLine.setCustomItem6__c(apiAccountId);
                        }
                    }
                    logger.info("报价单ID：" + quoteId + " 报价单明细：" + JSONObject.toJSONString(quoteLineList));
                    BatchOperateResult batchOperateResult = XObjectService.instance().update(quoteLineList, true, true);
                    logger.info("更新报价单明细返回结果，" + JSONObject.toJSONString(batchOperateResult));
                    if (batchOperateResult.getSuccess()) {
                        logger.info("报价单ID：" + quoteId + " 回写API主账号：,回写成功。");
                    } else {
                        logger.error("报价单ID：" + quoteId + " 回写API主账号,回写失败。，错误原因：" + batchOperateResult.getErrorMessage());
                    }
                }
            } else {
                throw new ScriptBusinessException("报价单ID:" + quoteId + " 查询报价单明细错误信息：" + queryResult.getErrorMessage());
            }
        } catch (Exception ex) {
            logger.error("报价单审批-回写API主账号,错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
