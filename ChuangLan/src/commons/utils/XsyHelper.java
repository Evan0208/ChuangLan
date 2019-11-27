package commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * 销售易查询辅助类
 *
 * @author qiang.gong
 */

public class XsyHelper {

    private static Logger logger = LoggerFactory.getLogger();

    private static final Integer RESULT_CODE = 200;

    private static final Integer RESULT_STATUS = 0;


    /**
     * 获取实体信息
     *
     * @param client     RkhdHttpClient
     * @param entityName 实体名称
     * @param id         实体记录Id
     * @return JSONObject 实体信息
     */
    public static JSONObject getEntityInfoById(RkhdHttpClient client, String entityName, Long id) {
        logger.debug(" GetEntityInfoById：查询实体:" + entityName + "查询ID：" + id);
        JSONObject entity = null;
        try {
            if (client == null) {
                client = new RkhdHttpClient();
            }
            RkhdHttpData data = new RkhdHttpData();
            data.setCall_type("GET");
            data.setCallString("/rest/data/v2/objects/" + entityName + "/" + id);
            String responseStr = client.performRequest(data);
            if (StringUtils.isNotBlank(responseStr)) {
                logger.debug(" GetEntityInfoById：查询实体信息结果：" + responseStr);
                JSONObject responseObject = JSONObject.parseObject(responseStr);
                Integer responseCode = responseObject.getIntValue("code");
                if (RESULT_CODE.equals(responseCode)) {
                    String entityStr = responseObject.getString("result");
                    entity = JSONObject.parseObject(entityStr);
                }
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }
        logger.debug(" GetEntityInfoById：返回结果:" + entity);
        return entity;
    }

    /**
     * 获取实体信息
     *
     * @param client     RkhdHttpClient
     * @param entityName 实体名称
     * @param id         实体记录Id
     * @return JSONObject 实体信息
     */
    public static JSONObject getEntityInfoById(RkhdHttpClient client, String entityName, String id) {
        long lid = Long.parseLong(id);
        return getEntityInfoById(client, entityName, lid);
    }

    /**
     * 根据SQL查询结果集
     *
     * @param client RkhdHttpClient
     * @param sql    查询语句
     * @return 查询结果List
     */
    public static JSONArray getJsonArrayBySql(RkhdHttpClient client, String sql) {
        logger.debug(" GetJSONArrayBySql：查询SQL:" + sql);
        JSONArray list = null;
        try {
            if (client == null) {
                client = new RkhdHttpClient();
            }
            RkhdHttpData data = new RkhdHttpData();
            data.setCall_type("GET");
            data.setCallString("/rest/data/v2/query?q=" + URLEncoder.encode(sql, "utf-8"));
            String responseStr = client.performRequest(data);
            if (StringUtils.isNotBlank(responseStr)) {
                logger.debug(" GetJSONArrayBySql：查询列表结果：" + responseStr);
                JSONObject responseObject = JSONObject.parseObject(responseStr);
                Integer responseCode = responseObject.getIntValue("code");
                if (RESULT_CODE.equals(responseCode)) {
                    String resultStr = responseObject.getString("result");
                    JSONObject result = JSONObject.parseObject(resultStr);
                    Long totalSize = result.getLong("totalSize");
                    if (totalSize != null && totalSize.longValue() > 0L) {
                        String records = result.getString("records");
                        list = JSONArray.parseArray(records);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }
        logger.debug(" GetJSONArrayBySql：返回结果:" + list);
        return list;
    }

    /**
     * 添加团队成员
     *
     * @param client     RkhdHttpClient
     * @param businessId 记录ID
     * @param belongId   实体ID
     * @param ownerId    新团队成员
     * @return boolean
     */
    public static boolean joinGroupOwner(RkhdHttpClient client, Long businessId, Long belongId, Long ownerId) {
        boolean result = false;
        try {
            if (client == null) {
                client = new RkhdHttpClient();
            }
            RkhdHttpData data = new RkhdHttpData();
            data.setCallString("/data/v1/objects/group/join-owner");
            data.setCall_type("POST");
            JSONArray users = new JSONArray();
            JSONObject owener = new JSONObject();
            owener.put("id", ownerId);
            users.add(owener);
            JSONObject parms = new JSONObject();
            parms.put("businessId", businessId);
            parms.put("belongId", belongId);
            parms.put("users", users);
            data.putFormData("params", parms);
            String groupResult = client.performRequest(data);
            JSONObject groupObject = JSONObject.parseObject(groupResult);
            String errUsers = groupObject.getString("errUsers");
            JSONArray errUsersArray = JSONArray.parseArray(errUsers);
            if (errUsersArray.size() == 0) {
                result = true;
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 创建实体记录
     *
     * @param client     RkhdHttpClient
     * @param entityName 实体名称
     * @param object     新纪录的信息
     * @return Id
     */
    public static long creatEntity(RkhdHttpClient client, String entityName, JSONObject object) {
        long resultId = 0;
        try {
            if (client == null) {
                client = new RkhdHttpClient();
            }
            RkhdHttpData data = new RkhdHttpData();
            data.setCallString("/rest/data/v2/objects/" + entityName);
            data.setCall_type("POST");
            JSONObject record = new JSONObject();
            record.put("data", object);
            logger.debug("新增实体信息，实体名：" + entityName + "新增内容：" + record.toString());
            data.setBody(record.toString());
            String responseStr = client.performRequest(data);
            logger.debug("返回信息：" + responseStr);
            JSONObject responseObject = JSONObject.parseObject(responseStr);
            Integer responseCode = responseObject.getIntValue("code");
            if (RESULT_CODE.equals(responseCode)) {
                String resultStr = responseObject.getString("result");
                JSONObject result = JSONObject.parseObject(resultStr);
                resultId = result.getLong("id");
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }
        return resultId;
    }

    /**
     * 修改实体信息
     *
     * @param client     RkhdHttpClient
     * @param entityName 实体名
     * @param id         记录ID
     * @param object     修改纪录的信息
     * @return boolean
     */
    public static boolean updateEntity(RkhdHttpClient client, String entityName, long id, JSONObject object) {
        boolean result = false;
        try {
            if (client == null) {
                client = new RkhdHttpClient();
            }
            RkhdHttpData data = new RkhdHttpData();
            data.setCallString("/rest/data/v2/objects/" + entityName + "/" + id);
            data.setCall_type("PATCH");
            JSONObject record = new JSONObject();
            record.put("data", object);
            logger.debug("修改实体信息，实体名：" + entityName + "修改记录ID ：" + id + "，修改内容：" + record.toString());
            data.setBody(record.toString());
            String responseStr = client.performRequest(data);
            logger.debug("返回信息：" + responseStr);
            JSONObject responseObject = JSONObject.parseObject(responseStr);
            Integer responseCode = responseObject.getIntValue("code");
            if (RESULT_CODE.equals(responseCode)) {
                result = true;
            } else {
                logger.debug("返回信息：" + responseStr);
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 修改实体信息V1
     *
     * @param client     RkhdHttpClient
     * @param entityName 实体名
     * @param object     修改纪录的信息
     * @return boolean
     */
    public static boolean updateEntity(RkhdHttpClient client, String entityName, JSONObject object) {
        boolean result = false;
        try {
            if (client == null) {
                client = new RkhdHttpClient();
            }
            RkhdHttpData data = new RkhdHttpData();
            data.setCallString("/data/v1/objects/" + entityName + "/update");
            data.setCall_type("POST");
            logger.debug("修改实体信息，实体名：" + entityName + "，修改内容：" + object.toString());
            data.setBody(object.toString());
            String responseStr = client.performRequest(data);
            logger.debug("返回信息：" + responseStr);
            JSONObject responseObject = JSONObject.parseObject(responseStr);
            Integer responseCode = responseObject.getIntValue("status");
            if (RESULT_STATUS.equals(responseCode)) {
                result = true;
            } else {
                logger.debug("返回信息：" + responseStr);
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }
        return result;
    }

}
