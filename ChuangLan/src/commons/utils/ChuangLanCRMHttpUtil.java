package commons.utils;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * 创蓝CRM访问方法
 *
 * @author gongqiang
 */
public class ChuangLanCRMHttpUtil {

    private static String CODE_STR = "code";
    private static Logger logger = LoggerFactory.getLogger();


    /**
     * 正式环境
     */
    private static String BASE_URL = "http://120.253.136.198:36218/erp-api";
    private static String appKey = "123456";
    private static String appUser = "crmApi";

//private static String BASE_URL = "http://crm.253.com/erp-api";
//private static String appKey = "LojKWdu";
//private static String appUser = "crmApi";

    private static String URL_QUERY_BALANCE = BASE_URL + "/apiAccount/queryBalance";
    private static String CUSTOMER_ACCOUNT_MODIFY_PASSWORD = BASE_URL + "/customerAccount/modifyPassword";
    private static String CUSTOMER_SUB_ACCOUNT_MODIFY_PASSWORD = BASE_URL + "/customerSubAccount/modifyPassword";
    private static String API_ACCOUNT_ADD = BASE_URL + "/apiAccount/add";


    /**
     * 查询API主账号余额
     *
     * @param apiAccount API主账号
     * @param platform   平台  1.万数（自助通)    2.云通讯（API主账号）
     */
    public static Long checkAccountBalance(String apiAccount, Integer platform) {
        Long balance = null;
        Map<String, String> headerMap = new HashMap<>(0);
        headerMap = getHeaderMap();
        JSONObject parameter = new JSONObject();
        parameter.put("apiAccount", apiAccount);
        parameter.put("platform", platform);
        String result = doPost(URL_QUERY_BALANCE, headerMap, parameter);
        logger.debug("查询API主账号余额参数：" + parameter.toString() + " ,返回结果：" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultObject = JSONObject.parseObject(result);
            if (resultObject.containsKey(CODE_STR) && resultObject.getLong(CODE_STR) == 0) {
                balance = resultObject.getLong("data");
            }
        }
        return balance;
    }

    /**
     * 修改自助通账号密码
     *
     * @param erpId 自助通账号erpId
     * @return 新密码
     */
    public static String modifyPassword2ERP(Integer erpId) {
        String password = null;
        Map<String, String> headerMap = new HashMap<>(0);
        headerMap = getHeaderMap();
        JSONObject parameter = new JSONObject();
        parameter.put("id", erpId);
        String result = doPost(CUSTOMER_ACCOUNT_MODIFY_PASSWORD, headerMap, parameter);
        logger.debug("重置自助通账号密码，参数：" + parameter.toString() + " ,返回结果：" + result);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultObject = JSONObject.parseObject(result);
            if (resultObject.containsKey(CODE_STR) && resultObject.getLong(CODE_STR) == 0) {
                password = resultObject.getString("data");
            }
        }
        return password;
    }

    /**
     * 修改自助通子账号账号密码
     *
     * @param erpId 自助通子账号账号erpId
     * @return 新密码
     */
    public static String subAccountModifyPassword2ERP(Long erpId) {
        String password = null;
        Map<String, String> headerMap = new HashMap<>(0);
        headerMap = getHeaderMap();
        JSONObject parameter = new JSONObject();
        parameter.put("id", erpId);
        String result = doPost(CUSTOMER_SUB_ACCOUNT_MODIFY_PASSWORD, headerMap, parameter);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultObject = JSONObject.parseObject(result);
            if (resultObject.containsKey(CODE_STR) && resultObject.getLong(CODE_STR) == 0) {
                password = resultObject.getString("data");
            }
        }
        return password;
    }

    /**
     * 添加主api账号
     *
     * @param productId erp产品Id
     * @param accountId erp自助通Id
     * @param isCmpp    是否cmpp 1是0否
     * @param spId      CMPP接口账号
     * @param spCode    CMPP接口接入号
     * @param speed     CMPP接口流速
     * @return appId  appId
     */
    public static JSONObject addMainApiAccount2ERP(String productId, String accountId, Integer isCmpp, String spId, String spCode, String speed) {
        JSONObject appId = null;
        Map<String, String> headerMap = new HashMap<>(0);
        headerMap = getHeaderMap();
        JSONObject parameter = new JSONObject();
        parameter.put("productId", productId);
        parameter.put("accountId", accountId);
        parameter.put("isCmpp", isCmpp);
        //isSub为0标识添加主账号
        parameter.put("isSub", 0);
        //是否CMPP 1是0否 如果是cmpp spId spCode speed必填
        if (isCmpp == 1) {
            parameter.put("spId", spId);
            parameter.put("spCode", spCode);
            parameter.put("speed", speed);
        }
        String result = doPost(API_ACCOUNT_ADD, headerMap, parameter);
        if (StringUtils.isNotBlank(result)) {
            logger.debug("激活API主账号，参数：" + parameter.toString() + " ,返回结果：" + result);
            JSONObject resultObject = JSONObject.parseObject(result);
            if (resultObject.containsKey(CODE_STR) && resultObject.getLong(CODE_STR) == 0) {
                String appIdStr = resultObject.getString("data");
                appId = JSONObject.parseObject(appIdStr);
            }
        }
        return appId;
    }


    /**
     * 添加api子账号
     *
     * @param productId
     * @param accountId
     * @param accountName 子账号名称
     * @param password    子账号密码
     * @return appId  appId
     */
    public static String addSubApiAccount2ERP(String productId, String accountId, String accountName, String password) {
        String appId = null;
        Map<String, String> headerMap = new HashMap<>(0);
        headerMap = getHeaderMap();
        JSONObject parameter = new JSONObject();

        //isSub为0标识添加主账号1子账号
        parameter.put("isSub", 1);
        parameter.put("productId", productId);
        parameter.put("accountId", accountId);
        parameter.put("accountName", accountName);
        parameter.put("password", password);

        String result = doPost(API_ACCOUNT_ADD, headerMap, parameter);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultObject = JSONObject.parseObject(result);
            if (resultObject.containsKey(CODE_STR) && resultObject.getLong(CODE_STR) == 0) {
                appId = resultObject.getString("data");
            }
        }
        return appId;
    }


    public static Map<String, String> getHeaderMap() {
        Map<String, String> map = new HashMap<>(0);
        map.put("appKey", appKey);
        map.put("appUser", appUser);
        map.put("timestamp", System.currentTimeMillis() + "");
        String queryString = map.entrySet().stream().sorted(
                Comparator.comparing(Map.Entry::getKey)
        ).map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        String md5 = stringToMD5(queryString);
        map.put("sign", md5);
        map.remove("appKey");
        map.remove("appUser");
        return map;
    }


    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }


    //region httpClient

    public static String doGet(String url, Map<String, String> headerMap) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置请求头信息，鉴权

            for (Entry<String, String> vo : headerMap.entrySet()) {
                httpGet.setHeader(vo.getKey(), vo.getValue());
            }
            // 设置配置请求参数// 连接主机服务超时时间// 请求超时时间// 数据读取超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(35000)
                    .setConnectionRequestTimeout(35000)
                    .setSocketTimeout(60000)
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, String> headerMap, Map<String, Object> paramMap) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例// 设置连接主机服务超时时间// 设置连接请求超时时间// 设置读取数据连接超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(35000)
                .setConnectionRequestTimeout(35000)
                .setSocketTimeout(60000)
                .build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        // 设置请求头
        for (Entry<String, String> vo : headerMap.entrySet()) {
            httpPost.setHeader(vo.getKey(), vo.getValue());
        }

        // 封装post请求参数
        if (null != paramMap && paramMap.size() > 0) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 通过map集成entrySet方法获取entity
            Set<Entry<String, Object>> entrySet = paramMap.entrySet();
            // 循环遍历，获取迭代器
            Iterator<Entry<String, Object>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> mapEntry = iterator.next();
                nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry
                        .getValue().toString()));
            }

            // 为httpPost设置封装好的请求参数
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, String> headerMap, JSONObject jsonObject) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        JSONObject response = null;

        try {
            StringEntity s = new StringEntity(jsonObject.toString());
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            httpPost.setEntity(s);
            // 设置请求头
            for (Entry<String, String> vo : headerMap.entrySet()) {
                httpPost.setHeader(vo.getKey(), vo.getValue());
            }
            HttpResponse res = httpClient.execute(httpPost);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                result = EntityUtils.toString(entity);
                response = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    //endregion


}
