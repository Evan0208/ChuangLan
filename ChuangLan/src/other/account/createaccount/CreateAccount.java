package other.account.createaccount;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.api.ApiSupport;
import com.rkhd.platform.sdk.api.annotations.RequestMethod;
import com.rkhd.platform.sdk.api.annotations.RestMapping;
import com.rkhd.platform.sdk.data.model.Account;
import com.rkhd.platform.sdk.data.model.CustomEntity51__c;
import com.rkhd.platform.sdk.data.model.CustomEntity52__c;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.http.Request;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.BatchOperateResult;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.service.XObjectService;
import org.apache.commons.lang.StringUtils;

import commons.utils.CommonHttpHelper;
import commons.utils.XobjectHelper;

import java.util.List;


/**
 * 创建客户自定义API
 *
 * @author gongqiang
 */
public class CreateAccount implements ApiSupport {

    private Logger logger = LoggerFactory.getLogger();
    /**
     * v2接口返回
     */
    private static final String RESULT_CODE = "200";
    /**
     * 中国号码的区号
     */
    private static final String CHINA_NUM = "0086";
    /**
     * 上线时间（2019-10-22 00:00:00）
     */
    private static final Long ON_LINE_TIME = 1571673600000L;

    /**
     * 自定义API创建客户
     *
     * @param request  请求参数
     * @param userId   用户
     * @param tenantId
     * @return String
     * @author gongqiang
     * script-api/customopenapi/account-create
     */
    @Override
    @RestMapping(value = "/account-create", method = RequestMethod.POST)
    public String execute(Request request, Long userId, Long tenantId) {
        JSONObject result = new JSONObject();
        try {
            String data = request.getParameter("data");
            logger.info("request-data:" + data);
            if (StringUtils.isBlank(data)) {
                throw new Exception("请求数据data不能为空");
            }
            JSONObject account = JSONObject.parseObject(data);

            if (StringUtils.isBlank(account.getString("accountName"))) {
                throw new Exception("请求数据data中accountName不能为空");
            }
            if (StringUtils.isBlank(account.getString("countryNum"))) {
                throw new Exception("请求数据data中countryNum不能为空");
            }

            //region 客户查重
            //ERP创建时间
            String accountName = account.getString("accountName");
            Long ERPCreatTime = account.getLong("customItem235__c");
            if (ERPCreatTime != null && ERPCreatTime.longValue() > ON_LINE_TIME.longValue()) {
                String sql = "SELECT id, accountName FROM account WHERE accountName = '" + accountName + "'";
                logger.info("查询SQL：" + sql);
                QueryResult<Account> duplicateAccountQueryResult = XObjectService.instance().query(sql, true);
                logger.info("查询结果：" + JSONObject.toJSONString(duplicateAccountQueryResult));
                if (duplicateAccountQueryResult.getSuccess()) {
                    if (duplicateAccountQueryResult.getRecords().size() > 0) {
                        throw new ScriptBusinessException(accountName + "该客户名称重复.操作失败。");
                    }
                }
            }
            //endregion

            CommonHttpClient client = new CommonHttpClient();

            //region 字段翻译
            String opSql = "SELECT customItem2__c, customItem4__c,customItem6__c,customItem8__c FROM customEntity52__c WHERE" +
                    "(customItem2__c = '" + account.getString("fState") + "' AND customItem6__c = '省份') OR " +
                    "(customItem2__c = '" + account.getString("fCity") + "' AND customItem6__c =  '城市') OR " +
                    //"(customItem2__c = '" + account.getString("customItem206__c")  + "' AND customItem6__c =  '客户来源') OR " +
                    //"(customItem2__c = '" + account.getString("customItem188__c")  + "' AND customItem6__c =  '客户类型') OR " +
                    //"(customItem2__c = '" + account.getString("customItem190__c")  + "' AND customItem6__c =  '客户状态') OR " +
                    "(customItem2__c = '" + account.getString("customItem209__c") + "' AND customItem6__c =  '营业执照审核状态') OR " +
                    "(customItem2__c = '" + account.getString("customItem213__c") + "' AND customItem6__c =  '客户标记') OR " +
                    "(customItem2__c = '" + account.getString("customItem219__c") + "' AND customItem6__c =  'ERP所属产品线') OR " +
                    "(customItem2__c = '" + account.getString("customItem226__c") + "' AND customItem6__c = '10690/10680') ";
            List<CustomEntity52__c> optionList = XobjectHelper.v2Query(opSql);
            logger.info("客户字段翻译：" + optionList.toString());
            if (optionList != null && optionList.size() > 0) {
                for (CustomEntity52__c option : optionList) {
                    if ("省份".equals(option.getCustomItem6__c())) {
                        account.put("fState", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("城市".equals(option.getCustomItem6__c())) {
                        account.put("fCity", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("营业执照审核状态".equals(option.getCustomItem6__c())) {
                        account.put("customItem209__c", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("客户标记".equals(option.getCustomItem6__c())) {
                        account.put("customItem213__c", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("ERP所属产品线".equals(option.getCustomItem6__c())) {
                        account.put("customItem219__c", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("10690/10680".equals(option.getCustomItem6__c())) {
                        account.put("customItem226__c", Integer.valueOf(option.getCustomItem4__c()));
                    }
                }
            }

            String whereSql = "";
            if (StringUtils.isNotBlank(account.getString("ownerId"))) {
                if (StringUtils.isNotBlank(whereSql)) {
                    whereSql += " OR ";
                }
                whereSql += "phone = '" + account.getString("ownerId") + "'";
            }
            if (StringUtils.isNotBlank(account.getString("customItem222__c"))) {
                if (StringUtils.isNotBlank(whereSql)) {
                    whereSql += " OR ";
                }
                whereSql += "phone = '" + account.getString("customItem222__c") + "'";
            }
            if (StringUtils.isNotBlank(account.getString("customItem223__c"))) {
                if (StringUtils.isNotBlank(whereSql)) {
                    whereSql += " OR ";
                }
                whereSql += "phone = '" + account.getString("customItem223__c") + "'";
            }
            if (StringUtils.isNotBlank(account.getString("customItem224__c"))) {
                if (StringUtils.isNotBlank(whereSql)) {
                    whereSql += " OR ";
                }
                whereSql += "phone = '" + account.getString("customItem224__c") + "'";
            }


            if (StringUtils.isNotBlank(whereSql)) {
                String userSql = "SELECT id, phone FROM user WHERE " + whereSql;
                JSONArray userList = CommonHttpHelper.v2Query(client, userSql);
                logger.info("客户用户字段翻译：" + userList.toString());
                if (userList != null && userList.size() > 0) {
                    String ownerIdStr = account.getString("ownerId");
                    String customItem222Str = account.getString("customItem222__c");
                    String customItem223Str = account.getString("customItem223__c");
                    String customItem224Str = account.getString("customItem224__c");
                    for (Object user : userList) {
                        JSONObject userObject = (JSONObject) user;
                        String phoneStr = userObject.getString("phone");
                        if (StringUtils.isNotBlank(phoneStr)) {
                            if (phoneStr.equals(ownerIdStr)) {
                                account.put("ownerId", userObject.getLong("id"));
                            }
                            if (phoneStr.equals(customItem222Str)) {
                                account.put("customItem222__c", userObject.getLong("id"));
                            }
                            if (phoneStr.equals(customItem223Str)) {
                                account.put("customItem223__c", userObject.getLong("id"));
                            }
                            if (phoneStr.equals(customItem224Str)) {
                                account.put("customItem224__c", userObject.getLong("id"));
                            }
                        }
                    }
                }
            }


            //endregion


            //省
            String fState = account.getString("fState");
            //市
            String fCity = account.getString("fCity");
            if (StringUtils.isBlank(fState)) {
                fState = null;
                account.put("fState", null);
            }
            if (StringUtils.isBlank(fCity)) {
                fCity = null;
                account.put("fCity", null);
            }

            String countryNum = account.getString("countryNum");
            String ownerIdStr = account.getString("ownerId");

            logger.info("转换后参数;" + account.toString());
            long ownerId = 0L;
            long highSeaId = 0L;
            CustomEntity51__c allocationSet = null;
            if (StringUtils.isNotBlank(ownerIdStr)) {
                ownerId = Long.valueOf(ownerIdStr);
                logger.info("查询用户：" + ownerId);
                JSONArray userList = CommonHttpHelper.v2Query(client, "select id,dimDepart from user where id=" + ownerId);
                logger.info("跟进人信息：" + userList.toString());
                if (userList != null && userList.size() > 0) {
                    long dimDepart = ((JSONObject) userList.get(0)).getLong("dimDepart");
                    //根据部门id去公海池对照表中查询所属公海
                    JSONArray allocationList = CommonHttpHelper.v2Query(client, "select id, customItem23__c FROM customEntity51__c WHERE   customItem27__c = 1 AND  customItem2__c = " + dimDepart);
                    logger.info("跟进人部门对应公海池：" + userList.toString());
                    if (allocationList != null && allocationList.size() > 0) {
                        String highSeaStr = ((JSONObject) allocationList.get(0)).getString("customItem23__c");
                        highSeaId = Long.valueOf(highSeaStr);
                    }
                } else {
                    throw new Exception("新增的客户信息所有人电话：" + ownerId + ",对应得用户不存在。");
                }
            } else {
                allocationSet = checkAllocationSet(client, fState, fCity, countryNum);
                ownerId = Long.parseLong(allocationSet.getCustomItem13__c());
                logger.info("修改公海池分配对照表数据：" + allocationSet.toString());
                logger.info("本次资源分配用户ID" + ownerId);
                highSeaId = Long.valueOf(allocationSet.getCustomItem23__c());
            }

            if (highSeaId == 0L) {
                throw new Exception("所有人：" + ownerId + "，没有对应得公海池");
            }

            //所有人
            account.put("ownerId", ownerId);
            account.put("accountName", accountName);
            //以下代码自己处理的字段
            account.put("entityType", 735708095382208L);
            //所属公海
            account.put("highSeaId", highSeaId);

            //移除字段
            account.remove("countryNum");
            //创建客户
            result = CommonHttpHelper.creatEntityObject(client, "account", account);
            result.put("code", result.getString("code"));
            if (RESULT_CODE.equals(result.getString("code"))) {
                if (allocationSet != null) {
                    try {
                        JSONObject object = JSONObject.parseObject(allocationSet.toString());
                        object.remove("name");
                        //如果配置客户次数 和 当前客户次数 相等 表示 该配置成员全部已经分满
                        //线索上次分配的人ID 和 部门负责人
                        //团队成员最小的用户 和 客户待分配的人ID 不一样
                        //说明该记录已经分配满了，自动分配给负责人
                        //if (allocationSet.getCustomItem10__c().equals(allocationSet.getCustomItem12__c()) &&
                        //        allocationSet.getCustomItem14__c().equals(allocationSet.getCustomItem4__c()) &&
                        //        !allocationSet.getCustomItem6__c().equals(allocationSet.getCustomItem11__c())) {
                        //    //将上次分配的人员ID 重置为最小ID，避免（判断当前分配对应表的逻辑） 时出错。
                        //    allocationSet.setCustomItem14__c(allocationSet.getCustomItem6__c());
                        //}

                        logger.info("修改公海池分配对照表数据：" + object.toString());
                        boolean a = CommonHttpHelper.updateEntity(client, "CustomEntity51__c", allocationSet.getId(), object);
                        if (a) {
                            logger.info("更新公海池分配对照表成功" + object.toString());
                        } else {
                            logger.error("更新公海池分配对照表失败" + object.toString());
                        }
                    } catch (Exception e) {
                        logger.error(" 更新公海池分配对照表失败,报错原因" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" 报错信息：" + e);
            result.put("code", 500);
            result.put("msg", "创建销售线索失败,错误原因：" + e.getMessage());
        }

        return result.toString();
    }

    /**
     * 修改公海池配置表
     */
    public CustomEntity51__c checkAllocationSet(CommonHttpClient client, String fState, String fCity, String internationalNum) throws Exception {
        CustomEntity51__c allocationSet = null;
        String sql;
        if (internationalNum.equals(CHINA_NUM)) {
            //查询省份
            sql = "SELECT id, name, customItem1__c, customItem2__c, customItem3__c , customItem4__c, customItem5__c, customItem6__c, customItem7__c, customItem8__c , customItem9__c, customItem13__c, customItem23__c, customItem24__c, customItem25__c FROM customEntity51__c WHERE customItem27__c = 1 AND customItem24__c = '0086' AND customItem1__c = " + fState + " AND customItem3__c = " + fCity + " ";
        } else {
            //查询国家区号
            sql = "SELECT id, name, customItem1__c, customItem2__c, customItem3__c , customItem4__c, customItem5__c, customItem6__c, customItem7__c, customItem8__c , customItem9__c, customItem13__c, customItem23__c, customItem24__c, customItem25__c FROM customEntity51__c WHERE customItem27__c = 1 AND customItem24__c = " + internationalNum + " ";
        }
        List<CustomEntity51__c> allocationSetList = XobjectHelper.v2Query(sql);
        logger.info("公海池分配对照表列表信息：" + allocationSetList.toString());
        if (allocationSetList == null || allocationSetList.size() == 0) {
            throw new Exception("公海池分配对照表没有找到唯一匹配省市的数据,省份：" + fState + " 城市：" + fCity + " 国家区号：" + internationalNum);
        }
        //region 判断当前分配对应表的逻辑
        //判断当前分配对应表的逻辑
        for (int i = 0; i < allocationSetList.size(); i++) {
            CustomEntity51__c tempModel = allocationSetList.get(i);
            //配置中线索上次分配的人ID 和  团队成员最小的用户 都不能为空
            //配置中配置线索次数 和  当前线索次数 都不能为空
            if (StringUtils.isNotBlank(tempModel.getCustomItem6__c()) &&
                    StringUtils.isNotBlank(tempModel.getCustomItem13__c()) &&
                    StringUtils.isNotBlank(tempModel.getCustomItem7__c()) &&
                    StringUtils.isNotBlank(tempModel.getCustomItem9__c())) {
                //线索上次分配的人ID 和  团队成员最小的用户 相等表示 一个循环的分配正好结束
                //配置线索次数 和 当前线索次数 相同时表示 该配置全部分配结束
                if (tempModel.getCustomItem6__c().equals(tempModel.getCustomItem13__c())) {
                    if (i == allocationSetList.size() - 1) {
                        //重置所有的配置的上一次分配为空
                        for (CustomEntity51__c a : allocationSetList) {
                            a.setCustomItem13__c("");
                        }
                        logger.info("批量修改公海池配置表中的上次分配人，修改信息" + allocationSetList.toString());
                        try {
                            BatchOperateResult batchResult = XobjectHelper.updateAll(allocationSetList);
                            logger.info("批量修改公海池配置表中的上次分配人,返回结果" + batchResult.getOperateResults());
                        } catch (Exception e) {
                            logger.error("批量修改公海池配置表中的上次分配人失败，错误；" + e.getMessage());
                        }
                        allocationSet = allocationSetList.get(0);
                        break;
                    }
                    continue;
                } else {
                    allocationSet = tempModel;
                    logger.info("当前分配的配置是: 第 " + i + "条");
                    break;
                }
            } else {
                allocationSet = tempModel;
                logger.info("当前分配的配置是: 第 " + i + "条");
                break;
            }
        }
        //如果列表全部分配结束，则取第一条
        if (allocationSet == null) {
            logger.info("公海池分配列表全部已满，默认分配第一条");
            allocationSet = allocationSetList.get(0);
        }

        //endregion

        return initAllocationSet(client, allocationSet);
    }

    /**
     * 初始化配置信息
     */
    public CustomEntity51__c initAllocationSet(CommonHttpClient client, CustomEntity51__c allocationSet) throws Exception {
        //获取唯一匹配的待分配的设置
        logger.info("公海池分配对照表信息：" + allocationSet.toString());
        //当全部分配满后所有资源分配给部门负责人
        if (StringUtils.isNotBlank(allocationSet.getCustomItem7__c()) && StringUtils.isNotBlank(allocationSet.getCustomItem9__c())) {
            long currentNum = Long.parseLong(allocationSet.getCustomItem9__c());
            long configNum = Long.parseLong(allocationSet.getCustomItem7__c());
            //配置次数 小于等于 当前数量 时分配给负责人
            if (configNum <= currentNum) {
                //部门负责人不能为空
                if (allocationSet.getCustomItem4__c() == null) {
                    throw new Exception("公海池分配对照表编号：" + allocationSet.getName() + "数据中部门负责人为空");
                }
                allocationSet.setCustomItem13__c(String.valueOf(allocationSet.getCustomItem4__c()));
                logger.info("公海池分配已满，资源分配给部门负责人：" + allocationSet.getCustomItem13__c());
                return allocationSet;
            }
        }
        //部门
        if (allocationSet.getCustomItem2__c() == null) {
            throw new Exception("公海池分配对照表编号：" + allocationSet.getName() + "数据中部门数据为空");
        }
        //查询部门下第一个用户ID(正序)
        String sql = "SELECT id FROM user WHERE status=1 AND dimDepart = " + allocationSet.getCustomItem2__c() + " ";
        JSONArray userList = CommonHttpHelper.v2Query(client, sql, 0, 3);
        if (userList == null || userList.size() == 0) {
            throw new Exception("公海池分配对照表编号：" + allocationSet.getName() + "数据中对应部门下没有用户");
        }
        logger.info("userList:" + userList.toString());
        JSONObject userObject = (JSONObject) userList.get(0);
        String firstUserId = userObject.getString("id");
        logger.info("团队成员最小的用户" + firstUserId);
        //团队成员最小的用户
        if (StringUtils.isNotBlank(allocationSet.getCustomItem6__c())) {
            if (!allocationSet.getCustomItem6__c().equals(firstUserId)) {
                //更换最小的用户ID
                allocationSet.setCustomItem6__c(firstUserId);
            }
        } else {
            //更换最小的用户ID
            allocationSet.setCustomItem6__c(firstUserId);
        }
        logger.info("团队成员最小的用户设置：" + allocationSet.getCustomItem6__c());
        //配置次数
        if (StringUtils.isBlank(allocationSet.getCustomItem7__c())) {
            throw new Exception("公海池分配对照表编号：" + allocationSet.getName() + "数据中没有配置次数");
        }
        //当前线索次数
        if (StringUtils.isBlank(allocationSet.getCustomItem9__c())) {
            allocationSet.setCustomItem9__c("0");
        }
        logger.info("配置数和目前次数：" + allocationSet.getCustomItem7__c() + " " + allocationSet.getCustomItem9__c());
        //线索待分配的人ID(为空)
        //团队成员最小的用户为循环中最后一个分配的人用于触发当前线索次数的变更
        if (StringUtils.isBlank(allocationSet.getCustomItem8__c())) {
            if (userList.size() > 1) {
                String nextUserId = ((JSONObject) userList.get(1)).getString("id");
                //客户待分配的人ID
                allocationSet.setCustomItem8__c(nextUserId);
            } else {
                allocationSet.setCustomItem8__c(firstUserId);
            }
            //客户上次分配的人ID
            allocationSet.setCustomItem13__c(allocationSet.getCustomItem8__c());
        } else {
            allocationSet.setCustomItem13__c(allocationSet.getCustomItem8__c());
            //修改配置信息为下一人
            sql = "SELECT id FROM user WHERE status=1 AND  dimDepart = " + allocationSet.getCustomItem2__c() + " AND id>" + allocationSet.getCustomItem13__c() + " ";
            JSONArray nextUserList = CommonHttpHelper.v2Query(client, sql, 0, 2);
            logger.info("nextUserList:" + userList.toString());
            //当部门已经没有比上一次分配的人更大ID的用户则用最小ID
            if (nextUserList == null || nextUserList.size() == 0) {
                allocationSet.setCustomItem8__c(firstUserId);
            } else {
                String nextUserId = ((JSONObject) nextUserList.get(0)).getString("id");
                allocationSet.setCustomItem8__c(nextUserId);
            }
        }


        //判断当前分配的人是否是团队成员最小的用户
        //if (firstUserId.equals(allocationSet.getCustomItem13__c())) {
        //    //当前线索次数加一
        //    if (StringUtils.isNotBlank(allocationSet.getCustomItem9__c())) {
        //        long currentNum = Long.parseLong(allocationSet.getCustomItem9__c());
        //        currentNum++;
        //        allocationSet.setCustomItem9__c(String.valueOf(currentNum));
        //    } else {
        //        allocationSet.setCustomItem9__c("1");
        //    }
        //}

        return allocationSet;
    }

}


