package other.process.submitcheck;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;

import java.util.List;

import commons.utils.CommonHttpHelper;

/**
 * 审批流提交校验
 *
 * @author gongqiang
 */
public class SubmitCheck implements ScriptTrigger {
    private static final Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("审批流提交校验-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //用大写Long 避免有null程序报错
            Long belongId = model.getLong("belongId");
            Long id = model.getLong("id");
            Long entityType = model.getLong("entityType");
            if (belongId == null || id == null || entityType == null) {
                logger.error("审批流提交校验,belongId:" + belongId + ",id:" + id + "entityType:" + entityType);
            }
            CommonHttpClient client = new CommonHttpClient();
            if (true) {
               // CommonHttpHelper.backApproval(client, id, belongId, "不满足要求");
            }
        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
            throw new ScriptBusinessException("审批流提交校验失败,错误Exception" + ex.getMessage());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
