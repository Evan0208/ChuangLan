package other.process.autoapproval;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.api.ApiSupport;
import com.rkhd.platform.sdk.api.annotations.RequestMethod;
import com.rkhd.platform.sdk.api.annotations.RestMapping;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.http.Request;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import commons.utils.CommonHttpHelper;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

/**
 * 自动审批的自定义API
 *
 * @author gongqiang
 */
public class AutoApproval implements ApiSupport {

    private Logger logger = LoggerFactory.getLogger();
    private static final Integer RESULT_CODE = 200;

    private static HashMap ENTITY_TYPE_MAP = new HashMap() {{
        put(830096388047552L, "customEntity8__c");
        put(855873811038955L, "customEntity34__c");
    }};

    /**
     * 自动审批的自定义API
     */
    @Override
    @RestMapping(value = "/autoApproval", method = RequestMethod.POST)
    public String execute(Request request, Long userId, Long tenantId) {
        JSONObject result = new JSONObject();
        try {
            String data = request.getParameter("data");
            logger.info("request-data:" + data);
            if (StringUtils.isBlank(data)) {
                throw new Exception("请求数据data不能为空");
            }
            JSONObject process = JSONObject.parseObject(data);
            //用大写Long 避免有null程序报错
            Long belongId = process.getLong("belongId");
            Long entityType = process.getLong("entityType");
            Long id = process.getLong("id");
            String msg = process.getString("msg");
            Boolean approvalStatus = process.getBoolean("approvalStatus");
            String entityName = null;
            entityName = ENTITY_TYPE_MAP.get(belongId).toString();
            if (entityName == null) {
                throw new Exception("未找到belongId对应得实体表。");
            }
            CommonHttpClient client = new CommonHttpClient();
            String sql = "select processingStatus__c  from " + entityName + " where id=" + id;
            JSONArray list = CommonHttpHelper.v2Query(client, sql);
            if (list != null && list.size() > 0) {
                JSONObject entity = (JSONObject) list.get(0);
                if (entity != null) {
                    Integer flag = entity.getInteger("processingStatus__c");
                    //判断记录的一个字段的值
                    //处理状态==未处理
                    if (flag == 1) {
                        JSONObject updateObject = new JSONObject();
                        if (approvalStatus) {
                            updateObject.put("processingStatus__c", 2);
                            //更新处理状态
                        } else {
                            //更新处理状态
                            updateObject.put("processingStatus__c", 3);
                        }
                        JSONObject responseObject = CommonHttpHelper.updateEntityObject(client, entityName, id, updateObject);
                        Integer responseCode = responseObject.getInteger("code");
                        if (RESULT_CODE.equals(responseCode)) {
                            logger.info("自动审批的自定义API,更新处理状态成功。参数：" + process.toString());
                        } else {
                            logger.error("自动审批的自定义API,更新处理状态失败。参数：" + process.toString() + "错误原因：" + responseObject.getString("msg"));
                            throw new Exception("更新处理状态失败：" + responseObject.getString("msg"));
                        }
                    } else {
                        throw new Exception("审批节点不在 系统处理节点中，无法处理");
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            result.put("code", 500);
            result.put("msg", "自动审批失败,错误原因：" + e.getMessage());
        }
        return result.toString();
    }
}
