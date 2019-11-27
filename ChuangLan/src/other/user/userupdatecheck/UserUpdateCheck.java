package other.user.userupdatecheck;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import commons.utils.CommonHttpHelper;

import java.util.List;

/**
 * 用户编辑校验
 *
 * @author gongqiang
 */
public class UserUpdateCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("用户编辑校验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());

            Long id = model.getLong("id");
            Long status = model.getLong("status");
            CommonHttpClient client = new CommonHttpClient();
            JSONArray oldUserList = CommonHttpHelper.v2Query(client, "SELECT id, name, status FROM user WHERE id = " + id);
            if (oldUserList != null && oldUserList.size() > 0) {
                JSONObject oldUser = (JSONObject) oldUserList.get(0);
                if (oldUser != null) {
                    Long oldStatus = oldUser.getLong("status");
                    if (!status.equals(oldStatus)) {
                        model.setAttribute("syncState__c", 1);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("用户编辑校验错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
