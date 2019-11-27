package commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.exception.ApiEntityServiceException;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonData;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;


/**
 * CommonHttp方法
 *
 * @author Administrator
 * @version 1.0 CommonHttp功能
 */
public class CommonHttpHelper {

    //region 常量

    private final static int V1_LENGTH = 300;

    /**
     * V2最大查询长度
     */
    private final static int V2_LENGTH = 100;
    /**
     * query最大sql长度
     */
    private final static int QUERY_LENGTH = 1000;
    /**
     * query预留长度
     */
    private final static int QUERY_OBL_LENGTH = 50;
    /**
     * API频率错误代码
     */
    private static final int FREQUENCY_ERROR = 1020025;
    /**
     * V2接口成功代码
     */
    private static final Integer RESULT_CODE = 200;
    /**
     * V1接口成功代码
     */
    private static final Integer RESULT_STATUS = 0;

    /**
     * totalSize字符串
     */
    private static final String TOTAL_SIZE_STR = "totalSize";

    //endregion

    /**
     * 日志生成器
     */
    private static final Logger logger = LoggerFactory.getLogger();


    //#region SQL查询

    /**
     * 获取V2Query结果-全量获取
     *
     * @param client CommonHttpClient类型的连接器-null时自动生成
     * @param sql    查询语句不带分页
     * @return 返回查询结果字符串
     */
    public static JSONArray v1Query(CommonHttpClient client, String sql) throws ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        int start = 0;
        CommonData data = new CommonData();
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/query";
        // 是否继续读取数据
        boolean hasData = false;
        JSONArray records = new JSONArray();
        do {
            hasData = false;
            String tempSql = sql + "limit " + start + " , " + V1_LENGTH;
            logger.debug("tempSql--->" + tempSql);
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            data.setCall_type("Post");
            data.setCallString(url);
            data.putFormData("q", tempSql);
            String result = client.performRequest(data);
            logger.debug("result--->" + result);
            if (StringUtils.isNotBlank(result)) {
                JSONObject resultJson = JSONObject.parseObject(result);
                if (resultJson.containsKey(TOTAL_SIZE_STR)) {
                    int count = resultJson.getIntValue("count");
                    int totalSize = resultJson.getIntValue("totalSize");
                    if (count > 0) {
                        records.addAll(resultJson.getJSONArray("records"));
                    }
                    start = start + V1_LENGTH;
                    if (totalSize > start) {
                        hasData = true;
                    }
                } else {
                    throw new ScriptBusinessException(sql + "错误:-->" + resultJson.getString("error_code") + "|错误原因:" + resultJson.getString("message"));
                }
            } else {
                throw new ScriptBusinessException(sql + "错误:-->返回为空");
            }
        } while (hasData);
        return records;
    }

    /**
     * 获取V2Query结果-全量获取
     *
     * @param client CommonHttpClient类型的连接器-null时自动生成
     * @param sql    查询语句不带分页
     * @param start  开始值
     * @param end    结束值
     * @return 返回查询结果字符串
     */
    public static JSONArray v1Query(CommonHttpClient client, String sql, int start, int end) throws IOException, ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String baseUrl = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/query";
        logger.debug("sql:" + baseUrl + sql);
        // 是否继续读取数据
        boolean hasData = false;
        JSONArray records = new JSONArray();
        do {
            int tempEnd = 0;
            if ((end - start) > V1_LENGTH) {
                tempEnd = V1_LENGTH;
            } else {
                tempEnd = end;
            }
            hasData = false;
            String tempSql = sql + "limit " + start + " , " + tempEnd;
            logger.debug("tempSql--->" + tempSql);
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            data.setCall_type("post");
            data.setCallString(baseUrl);
            data.putFormData("q", tempSql);
            String result = client.performRequest(data);
            logger.debug("result--->" + result);
            if (StringUtils.isNotBlank(result)) {
                JSONObject resultJson = JSONObject.parseObject(result);
                if (resultJson.containsKey(TOTAL_SIZE_STR)) {
                    int count = resultJson.getIntValue("count");
                    int totalSize = resultJson.getIntValue("totalSize");
                    if (count > 0) {
                        records.addAll(resultJson.getJSONArray("records"));
                    }
                    start = start + V2_LENGTH;
                    if (totalSize > start) {
                        hasData = true;
                    }
                } else {
                    throw new ScriptBusinessException(sql + "错误:-->" + resultJson.getString("error_code") + "|错误原因:" + resultJson.getString("msg"));
                }
            } else {
                throw new ScriptBusinessException(sql + "错误:-->返回为空");
            }

        } while (hasData);
        return records;

    }


    /**
     * 获取V2Query结果-全量获取
     *
     * @param client CommonHttpClient类型的连接器-null时自动生成
     * @param sql    查询语句不带分页
     * @return 返回查询结果字符串
     */
    public static JSONArray v2Query(CommonHttpClient client, String sql) throws IOException, ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        int start = 0;
        CommonData data = new CommonData();
        String baseUrl = PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/query?q=";
        logger.debug("sql:" + baseUrl + sql);
        // 是否继续读取数据
        boolean hasData = false;
        JSONArray records = new JSONArray();
        do {
            hasData = false;
            String url = baseUrl + URLEncoder.encode(sql + "limit " + start + " , " + V2_LENGTH, "utf-8");
            logger.debug("url--->" + url);
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            data.setCall_type("Get");
            data.setCallString(url);
            String result = client.performRequest(data);
            logger.debug("result--->" + result);
            if (StringUtils.isNotBlank(result)) {
                JSONObject resultJson = JSONObject.parseObject(result);
                long code = resultJson.getLong("code");
                if (code == RESULT_CODE) {
                    JSONObject resultobj = resultJson.getJSONObject("result");
                    int count = resultobj.getIntValue("count");
                    int totalSize = resultobj.getIntValue("totalSize");
                    if (count > 0) {
                        records.addAll(resultJson.getJSONObject("result").getJSONArray("records"));
                    }
                    start = start + V2_LENGTH;
                    if (totalSize > start) {
                        hasData = true;
                    }
                } else {
                    throw new ScriptBusinessException(sql + "错误:-->" + code + "|错误原因:" + resultJson.getString("msg"));
                }
            } else {
                throw new ScriptBusinessException(sql + "错误:-->返回为空");
            }

        } while (hasData);
        return records;

    }

    /**
     * 获取V2Query结果-全量获取
     *
     * @param client CommonHttpClient类型的连接器-null时自动生成
     * @param sql    查询语句不带分页
     * @param start  开始值
     * @param end    结束值
     * @return 返回查询结果字符串
     */
    public static JSONArray v2Query(CommonHttpClient client, String sql, int start, int end) throws IOException, ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String baseUrl = PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/query?q=";
        logger.debug("sql:" + baseUrl + sql);
        // 是否继续读取数据
        boolean hasData = false;
        JSONArray records = new JSONArray();
        do {
            int tempEmd = 0;
            if ((end - start) > V2_LENGTH) {
                tempEmd = V2_LENGTH;
            } else {
                tempEmd = end;
            }
            hasData = false;
            String url = baseUrl + URLEncoder.encode(sql + "limit " + start + " , " + tempEmd, "utf-8");
            logger.debug("url--->" + url);
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            data.setCall_type("Get");
            data.setCallString(url);
            String result = client.performRequest(data);
            logger.debug("result--->" + result);
            if (StringUtils.isNotBlank(result)) {
                JSONObject resultJson = JSONObject.parseObject(result);
                long code = resultJson.getLong("code");
                if (code == RESULT_CODE) {
                    JSONObject resultobj = resultJson.getJSONObject("result");
                    int count = resultobj.getIntValue("count");
                    int totalSize = resultobj.getIntValue("totalSize");
                    if (count > 0) {
                        records.addAll(resultJson.getJSONObject("result").getJSONArray("records"));
                    }
                    start = start + V2_LENGTH;
                    if (totalSize > start) {
                        hasData = true;
                    }
                } else {
                    throw new ScriptBusinessException(sql + "错误:-->" + code + "|错误原因:" + resultJson.getString("msg"));
                }
            } else {
                throw new ScriptBusinessException(sql + "错误:-->返回为空");
            }

        } while (hasData);
        return records;

    }

    //endregion

    //region 其他查询

    /**
     * 获取V2Query结果-全量获取-且拆分WHERE
     *
     * @param client  CommonHttpClient类型的连接器-null时自动生成
     * @param baseSql 查询语句不带分页,format格式，预留需要添加的where位置
     * @return 返回查询结果字符串
     */
    public static JSONArray v2QuerySplitWhere(CommonHttpClient client, String baseSql, List<String> whereList) throws IOException, ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        JSONArray records = new JSONArray();
        int sqlLength = QUERY_LENGTH - QUERY_OBL_LENGTH - baseSql.length();
        StringBuilder tempSql = new StringBuilder("");
        for (String item : whereList) {
            if ((tempSql.length() + item.length() + 1) > sqlLength) {
                String sql = String.format(baseSql, "(" + tempSql + ")");
                logger.debug("formatSql:" + sql);
                records.addAll(CommonHttpHelper.v2Query(client, sql));
                tempSql = new StringBuilder("");
            }
            if ("".equals(tempSql)) {
                tempSql.append("(").append(item).append(")");
            } else {
                tempSql.append(" or (").append(item).append(")");
            }
        }
        if (!"".equals(tempSql)) {
            String sql = String.format(baseSql, "(" + tempSql + ")");
            logger.debug("formatSql:" + sql);
            records.addAll(CommonHttpHelper.v2Query(client, sql));
        }
        return records;
    }

    /**
     * 获取V2Query结果-全量获取-且拆分WHERE
     *
     * @param client  CommonHttpClient类型的连接器-null时自动生成
     * @param baseSql 查询语句不带分页,format格式，预留需要添加的where位置
     * @return 返回查询结果字符串
     */
    public static JSONArray v2QuerySplitIn(CommonHttpClient client, String baseSql, List<String> whereList) throws IOException, ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        JSONArray records = new JSONArray();
        int sqlLength = QUERY_LENGTH - QUERY_OBL_LENGTH - baseSql.length();
        StringBuilder tempSql = new StringBuilder("");
        for (String item : whereList) {
            if ((tempSql.length() + item.length() + 1) > sqlLength) {
                String sql = String.format(baseSql, "(" + tempSql + ")");
                logger.debug("formatSql:" + sql);
                records.addAll(CommonHttpHelper.v2Query(client, sql));
                tempSql = new StringBuilder("");
            }
            if ("".equals(tempSql)) {
                tempSql = new StringBuilder(item);
            } else {
                tempSql.append(",").append(item);
            }
        }
        if (!"".equals(tempSql)) {
            String sql = String.format(baseSql, "(" + tempSql + ")");
            logger.debug("formatSql:" + sql);
            records.addAll(CommonHttpHelper.v2Query(client, sql));
        }
        return records;
    }

    /**
     * 获取xoql结果
     *
     * @param client CommonHttpClient类型的连接器-null时自动生成
     * @return 返回token
     */
    public static JSONArray xoql(CommonHttpClient client, String sql) throws ScriptBusinessException, IOException, ApiEntityServiceException, InterruptedException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        JSONArray records = new JSONArray();
        CommonData data = new CommonData();
        client.setContentType("application/x-www-form-urlencoded");
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2.0/query/xoql";
        logger.debug("url--->" + url);
        logger.debug("sql:" + sql);
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Post");
        data.setCallString(url);
        data.putFormData("xoql", sql);
        String result = client.performRequest(data);
        logger.debug(result);
        client.setContentType("application/json");
        logger.debug("result--->" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            long code = resultJson.getLong("code");
            if (code == RESULT_CODE) {
                JSONObject resultobj = resultJson.getJSONObject("data");
                int count = resultobj.getIntValue("count");
                if (count > 0) {
                    records.addAll(resultobj.getJSONArray("records"));
                }
            } else if (code == FREQUENCY_ERROR) {
                //因为返回信息为频率过高所以暂停一秒钟
                Thread.sleep(1000);
                records = xoql(client, sql);
            } else {
                throw new ScriptBusinessException(sql + "错误:-->" + code + "|错误原因:" + resultJson.getString("msg"));
            }
        } else {
            throw new ScriptBusinessException(sql + "错误:-->返回为空");
        }
        return records;
    }

    /**
     * 获取指定通用选项集翻译
     *
     * @param client           CommonHttpClient类型的连接器-null时自动生成
     * @param globalPickApiKey 通用选项集apiKey
     * @param optionCode       查询的ID
     * @return 放回ID对应字符
     */
    public static String globalPicksOptionLabel(CommonHttpClient client, String globalPickApiKey, Long optionCode) throws ApiEntityServiceException, ScriptBusinessException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        JSONArray records = new JSONArray();
        CommonData data = new CommonData();
        client.setContentType("application/x-www-form-urlencoded");
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/metadata/v2.0/settings/globalPicks/" + globalPickApiKey;
        logger.debug("url--->" + url);
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Get");
        data.setCallString(url);
        String result = client.performRequest(data);
        client.setContentType("application/json");
        logger.debug("result--->" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            long code = resultJson.getLong("code");
            if (code == 0) {
                JSONObject resultObj = resultJson.getJSONObject("data");
                int count = resultObj.getIntValue("count");
                if (count > 0) {
                    JSONObject recordObj = resultObj.getJSONObject("records");
                    records.addAll(recordObj.getJSONArray("pickOption"));
                }
            } else {
                throw new ScriptBusinessException("错误:-->" + code + "|错误原因:" + resultJson.getString("msg"));
            }
        } else {
            throw new ScriptBusinessException("错误:-->返回为空");
        }

        String optionLabel = "";
        if (records.size() > 0) {
            for (int i = 0; i < records.size(); i++) {
                JSONObject jsonObj = records.getJSONObject(i);
                Long id = jsonObj.getLong("optionCode");
                if (id.equals(optionCode)) {
                    optionLabel = jsonObj.getString("optionLabel");
                    break;
                }
            }
        }

        return optionLabel;
    }

    /**
     * 获取实体信息
     *
     * @param client     RkhdHttpClient
     * @param entityName 实体名称
     * @param id         实体记录Id
     * @return JSONObject 实体信息
     */
    public static JSONObject getEntityInfoById(CommonHttpClient client, String entityName, Long id) {
        logger.debug(" GetEntityInfoById：查询实体:" + entityName + "查询ID：" + id);
        JSONObject entity = null;
        try {
            if (client == null) {
                client = new CommonHttpClient();
            }
            CommonData data = new CommonData();
            data.setCall_type("GET");
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            data.setCallString(PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/objects/" + entityName + "/" + id);
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

    //endregion

    //region 新增

    /**
     * 创建实体记录
     *
     * @param client     CommonHttpClient
     * @param entityName 实体名称
     * @param object     新纪录的信息
     * @return Id
     */
    public static long creatEntity(CommonHttpClient client, String entityName, JSONObject object) throws ApiEntityServiceException, ScriptBusinessException {
        long resultId = 0;

        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        data.setCallString(PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/objects/" + entityName);
        data.setCall_type("POST");
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        JSONObject record = new JSONObject();
        record.put("data", object);
        logger.debug("新增实体信息，实体名：" + entityName + "新增内容：" + record.toString());
        data.setBody(record.toString());
        String responseStr = client.performRequest(data);
        logger.debug("返回信息：" + responseStr);
        JSONObject responseObject = JSONObject.parseObject(responseStr);
        int responseCode = responseObject.getIntValue("code");
        if (RESULT_CODE == responseCode) {
            String resultStr = responseObject.getString("result");
            JSONObject result = JSONObject.parseObject(resultStr);
            resultId = result.getLong("id");
        }

        return resultId;
    }


    /**
     * 创建实体记录
     *
     * @param client     CommonHttpClient
     * @param entityName 实体名称
     * @param object     新纪录的信息
     * @return Id
     */
    public static JSONObject creatEntityObject(CommonHttpClient client, String entityName, JSONObject object) throws ApiEntityServiceException, ScriptBusinessException {
        JSONObject result = new JSONObject();
        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/objects/" + entityName;
        logger.debug("url--->" + url);
        data.setCallString(url);
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("POST");
        JSONObject record = new JSONObject();
        record.put("data", object);
        logger.debug("新增实体信息，实体名：" + entityName + "新增内容：" + record.toString());
        data.setBody(record.toString());
        String responseStr = client.performRequest(data);
        logger.debug("返回信息：" + responseStr);
        result = JSONObject.parseObject(responseStr);
        return result;
    }

    /**
     * 创建实体记录
     *
     * @param client     CommonHttpClient
     * @param entityName 实体名称
     * @param highSea    是否公海池
     * @param object     新纪录的信息
     * @return Id
     */
    public static JSONObject creatEntityObject(CommonHttpClient client, String entityName, boolean highSea, JSONObject object) throws ApiEntityServiceException, ScriptBusinessException {
        JSONObject result = new JSONObject();

        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/" + entityName + "/create";
        logger.debug("url--->" + url);
        data.setCallString(url);
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("POST");
        JSONObject record = new JSONObject();
        record.put("public", highSea);
        record.put("record", object);
        logger.debug("新增实体信息，实体名：" + entityName + "新增内容：" + record.toString());
        data.setBody(record.toString());
        String responseStr = client.performRequest(data);
        logger.debug("返回信息：" + responseStr);
        result = JSONObject.parseObject(responseStr);
        return result;
    }


    //endregion

    //region 修改

    /**
     * 修改实体信息
     *
     * @param client     CommonHttpClient
     * @param entityName 实体名
     * @param id         记录ID
     * @param object     修改纪录的信息
     * @return boolean
     */
    public static JSONObject updateEntityObject(CommonHttpClient client, String entityName, long id, JSONObject object) {
        JSONObject result = new JSONObject();
        try {
            if (client == null) {
                client = new CommonHttpClient();
            }
            CommonData data = new CommonData();
            data.setCallString(PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/objects/" + entityName + "/" + id);
            data.setCall_type("PATCH");
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            JSONObject record = new JSONObject();
            record.put("data", object);
            logger.debug("修改实体信息，实体名：" + entityName + "修改记录ID ：" + id + "，修改内容：" + record.toString());
            data.setBody(record.toString());
            String responseStr = client.performRequest(data);
            logger.debug("返回信息：" + responseStr);
            result = JSONObject.parseObject(responseStr);
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 修改实体信息
     *
     * @param client     CommonHttpClient
     * @param entityName 实体名
     * @param id         记录ID
     * @param object     修改纪录的信息
     * @return boolean
     */
    public static boolean updateEntity(CommonHttpClient client, String entityName, long id, JSONObject object) {
        boolean result = false;
        try {
            if (client == null) {
                client = new CommonHttpClient();
            }
            CommonData data = new CommonData();
            data.setCallString(PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/data/v2/objects/" + entityName + "/" + id);
            data.setCall_type("PATCH");
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            JSONObject record = new JSONObject();
            record.put("data", object);
            logger.debug("修改实体信息，实体名：" + entityName + "修改记录ID ：" + id + "，修改内容：" + record.toString());
            data.setBody(record.toString());
            String responseStr = client.performRequest(data);
            logger.debug("返回信息：" + responseStr);
            JSONObject responseObject = JSONObject.parseObject(responseStr);
            Integer responseCode = responseObject.getInteger("code");
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
     * @param client     CommonHttpClient
     * @param entityName 实体名
     * @param object     修改纪录的信息
     * @return boolean
     */
    public static boolean updateEntity(CommonHttpClient client, String entityName, JSONObject object) {
        boolean result = false;
        try {
            if (client == null) {
                client = new CommonHttpClient();
            }
            CommonData data = new CommonData();
            data.setCallString(PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/" + entityName + "/update");
            data.setCall_type("POST");
            data.addHeader("Authorization", PasswordHelper.getToken(client));
            logger.debug("修改实体信息，实体名：" + entityName + "，修改内容：" + object.toString());
            data.setBody(object.toString());
            String responseStr = client.performRequest(data);
            logger.debug("返回信息：" + responseStr);
            JSONObject responseObject = JSONObject.parseObject(responseStr);
            Integer responseCode = responseObject.getInteger("status");
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

    //endregion

    //region 审批流相关操作

    /**
     * 查看审批历史记录
     *
     * @param client
     * @param dataId   数据id
     * @param belongId 业务对象id
     */
    public static JSONArray v1QueryApprovalHistory(CommonHttpClient client, long belongId, long dataId) throws ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String baseUrl = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/approval/approvalHistory?dataId=" + dataId + "&belongId=" + belongId + "";
        logger.debug("baseUrl:" + baseUrl);
        JSONArray records = new JSONArray();
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Get");
        data.setCallString(baseUrl);
        String result = client.performRequest(data);
        logger.debug("result--->" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            if (resultJson.containsKey(TOTAL_SIZE_STR)) {
                int totalSize = resultJson.getIntValue("totalSize");
                if (totalSize > 0) {
                    records.addAll(resultJson.getJSONArray("records"));
                }
            } else {
                throw new ScriptBusinessException("错误:-->" + resultJson.getString("error_code") + "|错误原因:" + resultJson.getString("msg"));
            }
        } else {
            throw new ScriptBusinessException("错误:-->返回为空");
        }

        return records;

    }

    /**
     * 获取下一级审批人
     *
     * @param client
     * @param dataId     数据id
     * @param belongId   业务对象id
     * @param defineId   审批流程定义的id
     * @param approvalId 待审批的id
     */
    public static Long approvalNextUser(CommonHttpClient client, long belongId, long dataId, long defineId, long approvalId) throws ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String baseUrl = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/approval/nextUser?belongId=" + belongId + "&dataId=" + dataId + "&approvalId=" + approvalId + "&defineId=" + defineId + "";
        logger.debug("baseUrl:" + baseUrl);
        Long nextUser = 0L;
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Get");
        data.setCallString(baseUrl);
        String result = client.performRequest(data);
        logger.debug("result--->" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            if (resultJson.containsKey(TOTAL_SIZE_STR)) {
                int totalSize = resultJson.getIntValue("totalSize");
                if (totalSize > 0) {
                    JSONArray records = resultJson.getJSONArray("records");
                    if (records.size() > 0) {
                        nextUser = ((JSONObject) records.get(0)).getLong("id");
                    }
                }
            } else {
                throw new ScriptBusinessException("错误:-->" + resultJson.getString("error_code") + "|错误原因:" + resultJson.getString("msg"));
            }
        } else {
            throw new ScriptBusinessException("错误:-->返回为空");
        }
        return nextUser;
    }

    /**
     * 审批流程定义ID
     *
     * @param client
     * @param belongId
     * @param entityType
     */
    public static Long approvalDefine(CommonHttpClient client, long belongId, long entityType) throws ScriptBusinessException, ApiEntityServiceException {
        if (client == null) {
            client = new CommonHttpClient();
        }
        CommonData data = new CommonData();
        String baseUrl = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/approval/define?belongId=" + belongId + "&entityType=" + entityType + "";
        logger.debug("baseUrl:" + baseUrl);
        Long defineId = 0L;
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Get");
        data.setCallString(baseUrl);
        String result = client.performRequest(data);
        logger.debug("result--->" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            if (resultJson.containsKey(TOTAL_SIZE_STR)) {
                int totalSize = resultJson.getIntValue("totalSize");
                if (totalSize > 0) {
                    JSONArray records = resultJson.getJSONArray("records");
                    if (records.size() > 0) {
                        defineId = ((JSONObject) records.get(0)).getLong("id");
                    }
                }
            } else {
                throw new ScriptBusinessException("错误:-->" + resultJson.getString("error_code") + "|错误原因:" + resultJson.getString("msg"));
            }
        } else {
            throw new ScriptBusinessException("错误:-->返回为空");
        }
        return defineId;
    }

    /**
     * 审批
     *
     * @param client
     * @param dataId         数据id
     * @param belongId       业务对象id
     * @param msg            审批意见
     * @param approvalStatus 通过或拒绝
     * @param approvalUserId 下级审批人id (通过时用，可为null,)
     * @param defineId       审批流程定义的id (查询下级审批人时用，可为null)
     * @param entityType     业务类型 (查询审批流程定义的id时用，可为null)
     */
    public static void Approval(CommonHttpClient client, long dataId, long belongId, String msg, boolean approvalStatus, Long approvalUserId, Long defineId, Long entityType) throws ScriptBusinessException, ApiEntityServiceException {
        JSONArray records = v1QueryApprovalHistory(client, belongId, dataId);
        if (records.size() == 0) {
            throw new ScriptBusinessException("未找到审批流中待审批的id");
        }

        JSONObject nowApp = JSONObject.parseObject(records.get(0).toString());

        Long approvalId = nowApp.getLong("approvalId");
        if (approvalStatus) {
            if (approvalUserId == null) {
                if (defineId == null) {
                    if (entityType == null) {
                        throw new ScriptBusinessException("approvalUserId,defineId,entityType 三个参数不能在审批通过时同时为空");
                    } else {
                        defineId = approvalDefine(client, belongId, entityType);
                    }
                }
                approvalUserId = approvalNextUser(client, belongId, dataId, defineId, approvalId);
            }

            // 通过审批
            agreeApproval(client, approvalId, approvalUserId, msg);
        } else {
            // 拒绝审批
            refuseApproval(client, approvalId, msg);
        }
    }

    /**
     * 通过审批
     *
     * @param client
     * @param approvalId     待审批的id
     * @param approvalUserId 下级审批人id
     * @param msg            审批意见
     */
    public static void agreeApproval(CommonHttpClient client, long approvalId, long approvalUserId, String msg) throws ScriptBusinessException, ApiEntityServiceException {
        logger.debug("approvalId=>" + approvalId);
        if (client == null) {
            client = new CommonHttpClient();
        }
        if (msg != null && msg.length() > 800) {
            msg = msg.substring(0, 800);
        }
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/approval/agree";
        logger.debug("url--->" + url);
        CommonData data = new CommonData();
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Post");
        data.setCallString(url);
        JSONObject valueJson = new JSONObject();
        valueJson.put("approvalId", approvalId);
        valueJson.put("approvalUserId", approvalUserId);
        valueJson.put("comments", msg);
        data.setBody(valueJson.toString());
        String result = client.performRequest(data);
        logger.debug("result=>" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            if (resultJson.containsKey("error_code")) {
                logger.error("错误:通过审批失败-->错误原因:" + resultJson.getString("message"));
                throw new ScriptBusinessException("通过审批失败");
            } else {
                int status = resultJson.getInteger("status");
                if (status == 0) {
                    logger.error("通过审批成功");
                } else {
                    logger.error("通过审批失败");
                }
            }
            // error_code
        } else {
            logger.error("错误:通过审批失败");
            throw new ScriptBusinessException("错误:通过审批失败");
        }

    }

    /**
     * 拒绝审批
     *
     * @param client     CommonHttpClient类型的连接器-null时自动生成
     * @param approvalId 待审批的id
     * @param msg        拒绝原因
     */
    private static void refuseApproval(CommonHttpClient client, long approvalId, String msg) throws ScriptBusinessException, ApiEntityServiceException {
        logger.debug("approvalId=>" + approvalId);
        if (client == null) {
            client = new CommonHttpClient();
        }
        if (msg != null && msg.length() > 800) {
            msg = msg.substring(0, 800);
        }
        // 获取审批历史
        String url = PasswordHelper.getPasswordSet().getUrl_str__c() + "/data/v1/objects/approval/refuse";
        logger.debug("url--->" + url);
        CommonData data = new CommonData();
        data.addHeader("Authorization", PasswordHelper.getToken(client));
        data.setCall_type("Post");
        data.setCallString(url);
        JSONObject valueJson = new JSONObject();
        valueJson.put("approvalId", approvalId);
        valueJson.put("comments", msg);
        data.setBody(valueJson.toString());
        String result = client.performRequest(data);
        logger.debug("result=>" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            if (resultJson.containsKey("error_code")) {
                logger.error("错误:拒绝审批失败-->错误原因:" + resultJson.getString("message"));
                throw new ScriptBusinessException("拒绝审批失败");
            } else {
                int status = resultJson.getInteger("status");
                if (status == 0) {
                    logger.error("拒绝审批成功");
                } else {
                    logger.error("拒绝审批失败");
                }
            }
            // error_code
        } else {
            logger.error("错误:拒绝审批失败");
            throw new ScriptBusinessException("错误:拒绝审批失败");
        }

    }

    /**
     * 通知消息
     *
     * @param client
     * @param belongId
     * @param dataId
     * @param user
     * @param content
     * @param customLink
     */
    public static boolean notice(CommonHttpClient client, long belongId, long dataId, List<Long> user, String content, String customLink) {
        logger.debug("通用方法,通知消息-开始");
        logger.debug("belongId:" + belongId + " dataId:" + dataId + " user:" + user.toString() + " content:" + content + " customLink:" + customLink);
        boolean result = false;
        try {
            if (client == null) {
                client = new CommonHttpClient();
            }
            CommonData data = new CommonData();
            data.setCall_type("Post");
            data.setCallString(PasswordHelper.getPasswordSet().getUrl_str__c() + "/rest/notice/v2.0/newNotice");
            data.addHeader("Authorization", PasswordHelper.getToken(client));

            JSONObject noticeObject = new JSONObject();
            noticeObject.put("belongId", belongId);
            noticeObject.put("content", content);

            JSONArray mergeFields = new JSONArray();
            JSONObject mergeFieldsObject = new JSONObject();
            mergeFieldsObject.put("customLink", customLink);
            mergeFieldsObject.put("objectId", dataId);
            mergeFieldsObject.put("type", 1);

            mergeFields.add(mergeFieldsObject);
            noticeObject.put("mergeFields", mergeFields);
            noticeObject.put("mergeFieldsIndex", 1);
            noticeObject.put("objectId", dataId);

            JSONArray receivers = new JSONArray();
            for (Long userId : user) {
                JSONObject receiversObject = new JSONObject();
                receiversObject.put("id", userId);
                receiversObject.put("receiverType", 0);
                receivers.add(receiversObject);
            }
            noticeObject.put("receivers", receivers);
            logger.debug("请求参数：" + noticeObject.toString());
            data.putFormData("params", noticeObject);
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
            logger.error("通知消息报错，" + e.getMessage());
        }

        return result;
    }


    //endregion


}
