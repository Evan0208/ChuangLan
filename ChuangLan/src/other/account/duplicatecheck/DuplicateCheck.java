package other.account.duplicatecheck;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.data.model.Account;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.service.XObjectService;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 客户查重校验
 *
 * @author gongqiang
 */
public class DuplicateCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    /**
     * 上线时间（2019-10-22 00:00:00）
     */
    private static final Long ON_LINE_TIME = 1571673600000L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("客户查重校验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //客户ID
            Long id = model.getLong("id");

            String accountName = model.getString("accountName");
            //ERP创建时间
            Long erpCreatTime = model.getLong("customItem235__c");
            logger.info("ERP创建时间：" + erpCreatTime);

            boolean checkFlag = false;
            if (id != null) {
                QueryResult<Account> oldAccountQueryResult = XObjectService.instance().query("SELECT id, accountName FROM account WHERE id=" + id, true);
                logger.info("查询修改前客户信息：" + JSONObject.toJSONString(oldAccountQueryResult));
                if (oldAccountQueryResult.getSuccess()) {
                    List<Account> oldAccountList = oldAccountQueryResult.getRecords();
                    if (oldAccountList != null && oldAccountList.size() > 0) {
                        Account oldAccount = oldAccountList.get(0);
                        logger.info("客户名称修改前：" + oldAccount.getAccountName() + ",修改后：" + accountName);
                        if (StringUtils.isNotBlank(accountName) && !accountName.equals(oldAccount.getAccountName())) {
                            //编辑时仅在修改名称字段的时候查重
                            checkFlag = true;
                        }
                    }
                }
            } else {
                //新增时必定查重
                checkFlag = true;
            }


            if (checkFlag) {
                String sql = "SELECT id, accountName FROM account WHERE accountName = '" + accountName + "' ";
                if (id != null) {
                    sql += " AND id <> " + id;
                }
                logger.info("查询SQL：" + sql);
                QueryResult<Account> duplicateAccountQueryResult = XObjectService.instance().query(sql, true);
                logger.info("查询结果：" + JSONObject.toJSONString(duplicateAccountQueryResult));
                if (duplicateAccountQueryResult.getSuccess()) {
                    if (duplicateAccountQueryResult.getRecords().size() > 0) {
                        throw new ScriptBusinessException(accountName + "该客户名称重复.操作失败。");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
            throw new ScriptBusinessException(ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
