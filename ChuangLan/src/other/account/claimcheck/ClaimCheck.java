package other.account.claimcheck;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.Account;
import com.rkhd.platform.sdk.data.model.CustomEntity51__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;

import commons.utils.DateTimeHelper;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 客户领取公海池校验
 *
 * @author gongqiang
 */
public class ClaimCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();
    /**
     * 客户状态（领取）
     */
    private static final Long STATUS_RECEIVE = 3L;
    /**
     * 客户状态（未领取领取）
     */
    private static final Long STATUS_UNCOLLECTED = 2L;
    /**
     * 五天间隔差值
     */
    private static final long FIVE_DAYS_TIME = 432000000L;

    /**
     * 一天时间的差值
     */
    private static final long ONE_DAY_INTERVAL = 86400000L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户领取公海池校验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户ID
            Long id = model.getLong("id");
            //客户状态
            Long status = model.getLong("highSeaStatus");

            Account oldAccount = new Account();
            oldAccount.setId(id);
            oldAccount = XObjectService.instance().get(oldAccount, true);
            logger.info("旧的客户信息：" + oldAccount.toString());
            long oldStatus = oldAccount.getHighSeaStatus();

            //领取公海池
            if (STATUS_RECEIVE.equals(status) && STATUS_UNCOLLECTED.equals(oldStatus)) {

                //退回时间没有，用修改时间替代
                long releaseTime = oldAccount.getUpdatedAt();
                //当前时间
                long dateTimeNow = DateTimeHelper.getDateTimeNow();
                //原销售
                long lastOwnerId = oldAccount.getOwnerId();
                //新销售
                long newOwnerId = model.getLong("ownerId");
                logger.info("修改时间:" + releaseTime + "  当前时间:" + dateTimeNow + " 原销售:" + lastOwnerId + " 新销售:" + newOwnerId);
                //仅处理操作人和领取人一致的情况
                if (scriptTriggerParam.getUserId() == newOwnerId) {
                    if (newOwnerId == lastOwnerId && dateTimeNow - releaseTime < FIVE_DAYS_TIME) {
                        throw new ScriptBusinessException("不能领取自己在五天内退回的客户");
                    }

                    //部门等于全公司
                    QueryResult<CustomEntity51__c> setNumQueryResult = XObjectService.instance().query("SELECT customItem7__c FROM customEntity51__c WHERE customItem27__c=1 customItem2__c=735707789099733", true);
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


                //如果能领取的情况下
                //客户状态(6\丢弃)
                Integer customItem190__c = model.getInteger("customItem190__c");
                Integer customItem190 = 6;
                if (customItem190.equals(customItem190__c)) {
                    //设置客户类型(1\意向客户)
                    model.setAttribute("customItem188__c", 1);
                    //设置客户状态为1、浅意向
                    model.setAttribute("customItem190__c", 1);
                }
            }

            //退回公海池
            if (STATUS_UNCOLLECTED.equals(status) && STATUS_RECEIVE.equals(oldStatus)) {

                //客户类型(1\意向客户)
                Integer customItem188__c = model.getInteger("customItem188__c");

                Integer customItem188 = 1;
                if (customItem188.equals(customItem188__c)) {

                    //设置客户类型(3\丢弃客户)
                    model.setAttribute("customItem188__c", 3);
                    //设置客户状态为6、丢弃
                    model.setAttribute("customItem190__c", 6);

                } else {
                    throw new ScriptBusinessException("仅允许退回客户类型等于意向客户的客户。");
                }
            }
        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
            throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
