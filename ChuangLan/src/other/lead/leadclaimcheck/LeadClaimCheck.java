package other.lead.leadclaimcheck;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.Account;
import com.rkhd.platform.sdk.data.model.CustomEntity51__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import commons.utils.CommonHttpHelper;
import commons.utils.DateTimeHelper;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 销售线索公海池检查
 *
 * @author gongqiang
 */
public class LeadClaimCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 客户状态（领取）
     */
    private static final long STATUS_RECEIVE = 3L;
    /**
     * 状态（未领取领取）
     */
    private static final long STATUS_UNCOLLECTED = 2L;

    /**
     * 一天时间的差值
     */
    private static final long ONE_DAY_INTERVAL = 86400000L;


    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("销售线索领取公海池校验-开始");
        try {

            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户ID
            long id = model.getLong("id");
            //客户状态
            long status = model.getLong("highSeaStatus");
            //客户状态是领取
            if (status == STATUS_RECEIVE) {
                CommonHttpClient client = new CommonHttpClient();
                JSONObject oldLead = CommonHttpHelper.getEntityInfoById(client, "lead", id);
                logger.info("旧的销售线索信息：" + oldLead.toString());
                long oldStatus = oldLead.getLong("highSeaStatus");
                if (oldStatus == STATUS_UNCOLLECTED) {

                    long newOwnerId = model.getLong("ownerId");
                    //部门等于全公司
                    QueryResult<CustomEntity51__c> setNumQueryResult = XObjectService.instance().query("SELECT customItem7__c FROM customEntity51__c WHERE customItem27__c=2 customItem2__c=735707789099733", true);
                    logger.info("查询公海池分配对照表中每日领取数量的设置信息：" + setNumQueryResult.getRecords().toString());
                    if (setNumQueryResult.getSuccess()) {
                        List<CustomEntity51__c> setNumList = setNumQueryResult.getRecords();
                        if (setNumList != null && setNumList.size() > 0) {
                            //查询用户已领取数量
                            long dateNow = DateTimeHelper.getDateNow();
                            QueryResult<Account> claimNumQueryResult = XObjectService.instance().query("SELECT id FROM account WHERE ownerId = " + newOwnerId + " AND claimTime > " + dateNow + " AND claimTime < " + (dateNow + ONE_DAY_INTERVAL) + "", true);
                            logger.info("查询领取人：" + newOwnerId + "今日已经领取了多少客户" + claimNumQueryResult.getRecords().toString());
                            if (claimNumQueryResult.getSuccess()) {
                                List<Account> claimNumList = claimNumQueryResult.getRecords();
                                if (claimNumList != null && claimNumList.size() > 0) {
                                    String setNum = setNumList.get(0).getCustomItem7__c();
                                    long claimNum = claimNumList.size();
                                    logger.info("领取人：" + newOwnerId + "今日领取数量：" + claimNum + ",设置领取数量：" + setNum);
                                    long setNumLong = 0;
                                    if (StringUtils.isNotBlank(setNum)) {
                                        setNumLong = Long.valueOf(setNum);
                                    }
                                    if (claimNum >= setNumLong) {
                                        throw new ScriptBusinessException("你今日已经领取 " + claimNum + ",不能再领取客户");
                                    }
                                }
                            }
                        } else {
                            logger.error("公海池对照表中，每日领取客户数量设置信息丢失。请联系管理员。");
                        }
                    }


                }
            }


        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
            throw new ScriptBusinessException("领取次数已达上限");
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
