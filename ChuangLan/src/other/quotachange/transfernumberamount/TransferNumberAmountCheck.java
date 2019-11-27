package other.quotachange.transfernumberamount;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.CustomEntity18__c;
import com.rkhd.platform.sdk.data.model.CustomEntity35__c;
import com.rkhd.platform.sdk.data.model.Quote;
import com.rkhd.platform.sdk.data.model.QuoteLine;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;
import commons.utils.ChuangLanCRMHttpUtil;
import commons.utils.CommonHttpHelper;

import java.util.List;

/**
 * 额度变更申请
 * 转条数、金额
 * 转条数、金额提交校验
 *
 * @author gongqiang
 */
public class TransferNumberAmountCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 转条数、金额的业务类型编码
     */
    private static final Long ENTITY_TYPE = 830100998308497L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("额度变更申请-转条数.金额提交校验-开始");
        Long id = null;
        Long belongId = null;
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            Long entityType = model.getLong("entityType");
            id = model.getLong("id");
            belongId = model.getLong("belongId");
            //业务类型等于转条数、金额
            if (ENTITY_TYPE.equals(entityType)) {
                //API主账号
                Long apiAccountId = model.getLong("customItem31__c");
                //转入API主账号
                Long transferApiAccount = model.getLong("customItem33__c");

                if (apiAccountId == null || transferApiAccount == null) {
                    throw new ScriptBusinessException("转数额申请 API主账号、转入API主账号 不能为空");
                }

                QuoteLine quoteLine = null;
                QuoteLine transferQuoteLine = null;
                //报价单
                String sql = "SELECT id, customItem7__c, customItem6__c, price, customItem15__c FROM quoteLine WHERE (customItem15__c = '已审核' OR customItem15__c = '免审') AND customItem6__c = " + apiAccountId + " ORDER BY id DESC";
                QueryResult quoteQueryResult = XObjectService.instance().query(sql, true);
                logger.info("查询Sql:" + sql + "  ,查询报价单明细结果：" + JSONObject.toJSONString(quoteQueryResult));
                if (quoteQueryResult.getSuccess()) {

                    List<QuoteLine> quoteList = quoteQueryResult.getRecords();
                    if (quoteList != null && quoteList.size() > 0) {
                        quoteLine = quoteList.get(0);
                    } else {
                        throw new ScriptBusinessException("API主账号没有报价单信息");
                    }
                }

                //转入报价单
                sql = "SELECT id, customItem7__c, customItem6__c, price, customItem15__c FROM quoteLine WHERE (customItem15__c = '已审核' OR customItem15__c = '免审') AND customItem6__c = " + transferApiAccount + " ORDER BY id DESC";
                QueryResult transferQuoteQueryResult = XObjectService.instance().query(sql, true);
                logger.info("查询Sql:" + sql + "  ,查询转入报价单明细结果：" + JSONObject.toJSONString(transferQuoteQueryResult));
                if (transferQuoteQueryResult.getSuccess()) {
                    List<QuoteLine> transferQuoteList = transferQuoteQueryResult.getRecords();
                    if (transferQuoteList != null && transferQuoteList.size() > 0) {
                        transferQuoteLine = transferQuoteList.get(0);
                    } else {
                        throw new ScriptBusinessException("转入API主账号没有报价单明细信息");
                    }
                }
                if (quoteLine == null) {
                    throw new ScriptBusinessException("API主账号没有报价单明细信息");
                }
                if (transferQuoteLine == null) {
                    throw new ScriptBusinessException("转入API主账号没有报价单明细信息");
                }

                if (quoteLine.getPrice() < transferQuoteLine.getPrice()) {
                    String msg = "API主账号报价单明细:" + quoteLine.getId() + " 的单价：" + quoteLine.getPrice() + " ," +
                            " 装入API账号报价单明细：" + transferQuoteLine.getId() + " 的单价：" + transferQuoteLine.getPrice() + "  ,转入账号的产品单价不能高于转出账号的产品单价。 ";
                    logger.error(msg);
                    throw new ScriptBusinessException("转入账号的产品单价不能高于转出账号的产品单价");
                }

                //查询ERP余额是否大于本次转出的余额
                String apiAccountStr = model.getString("customItem34__c");
                Long balance = ChuangLanCRMHttpUtil.checkAccountBalance(apiAccountStr, 2);
                logger.info("API主账号id：" + apiAccountId + ",查询余额：" + balance);
                if (balance == null) {
                    throw new ScriptBusinessException("查询账户的可用余额失败。");
                } else {
                    //转移数额
                    Double transferAmount = model.getDouble("customItem54__c");
                    if (balance > transferAmount) {
                        String msg = "转移数(" + transferAmount + ")小于余额(" + balance + "),成功提交.";
                        logger.info(msg);
                    } else {
                        String msg = "转移数(" + transferAmount + ")大于余额(" + balance + "),不能提交.";
                        logger.error(msg);
                        throw new ScriptBusinessException(msg);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("额度变更申请-转条数.金额提交校验,错误Exception：" + ex.toString());
            try {
                if (id != null && belongId != null) {
                    CommonHttpClient client = new CommonHttpClient();
                    //审批驳回
                    CommonHttpHelper.Approval(client, id, belongId, ex.getMessage(), false, null, null, null);
                }
            } catch (Exception e) {
                logger.error("额度变更申请-转条数.金额提交校验,审批驳回错误Exception：" + e.getMessage());
            }
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
