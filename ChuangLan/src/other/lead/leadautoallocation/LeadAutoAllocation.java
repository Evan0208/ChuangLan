package other.lead.leadautoallocation;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import commons.utils.XsyHelper;
import java.util.List;

/**
 * 销售线索 创建时自动分配
 *
 * @author gongqiang
 */
public class LeadAutoAllocation implements ScriptTrigger {

    private static final Logger logger = LoggerFactory.getLogger();

    private static final long STATUS_UNCOLLECTED = 2L;

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        logger.info("销售线索 创建时自动分配-开始");
        try {
            List<DataModel> dataModelList = scriptTriggerParam.getDataModelList();
            DataModel model = dataModelList.get(0);
            logger.info("DataModel信息：" + model.toString());
            //销售线索所有人
            long id = model.getLong("id");
            //状态
            long highSeaStatus = model.getLong("highSeaStatus");

            long newOwnerId = 825839468544704L;

            if (highSeaStatus == STATUS_UNCOLLECTED) {
                RkhdHttpClient client = new RkhdHttpClient();
                JSONObject object = new JSONObject();
                object.put("id", id);

                object.put("highSeaStatus", 3L);

                boolean result = XsyHelper.updateEntity(client, "lead", object);
            }

        } catch (Exception ex) {
            logger.error("错误Exception：" + ex.toString());
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }

}
