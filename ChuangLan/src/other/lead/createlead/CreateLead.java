package other.lead.createlead;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.api.ApiSupport;
import com.rkhd.platform.sdk.api.annotations.RequestMethod;
import com.rkhd.platform.sdk.api.annotations.RestMapping;
import com.rkhd.platform.sdk.data.model.CustomEntity52__c;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.http.Request;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.BatchOperateResult;
import com.rkhd.platform.sdk.data.model.CustomEntity51__c;
import org.apache.commons.lang.StringUtils;

import commons.utils.CommonHttpHelper;
import commons.utils.XobjectHelper;

import java.util.List;

/**
 * 创建销售线索自定义API
 *
 * @author gongqiang
 */
public class CreateLead implements ApiSupport {

    private Logger logger = LoggerFactory.getLogger();

    private static final String RESULT_CODE = "200";
    /**
     * 中国号码的区号
     */
    private static final String CHINA_NUM = "0086";

    private static final String NULL_STR = "NULL";

    /**
     * 自定义API创建销售线索
     *
     * @param request  请求参数
     * @param userId   用户
     * @param tenantId
     * @return String
     * script-api/customopenapi/lead-create
     */
    @Override
    @RestMapping(value = "/lead-create", method = RequestMethod.POST)
    public String execute(Request request, Long userId, Long tenantId) {
        JSONObject result = new JSONObject();
        try {
            String data = request.getParameter("data");
            logger.info("request-data:" + data);
            if (StringUtils.isBlank(data)) {
                throw new Exception("请求数据data不能为空");
            }


            JSONObject lead = JSONObject.parseObject(data);

            if (StringUtils.isBlank(lead.getString("name"))) {
                throw new Exception("请求数据data中name不能为空");
            }
            if (StringUtils.isBlank(lead.getString("companyName"))) {
                throw new Exception("请求数据data中companyName不能为空");
            }
            if (StringUtils.isBlank(lead.getString("countryNum"))) {
                throw new Exception("请求数据data中countryNum不能为空");
            }

            if (StringUtils.isNotBlank(lead.getString("customItem181__c"))) {
                if (NULL_STR.equals(lead.getString("customItem181__c")) || "0".equals(lead.getString("customItem181__c"))) {
                    //ERP中二级录入是int默认零
                    lead.remove("customItem181__c");
                }
            }

            CommonHttpClient client = new CommonHttpClient();
            String opSql = "SELECT customItem2__c, customItem4__c,customItem6__c,customItem8__c FROM customEntity52__c WHERE" +
                    "(customItem2__c = '" + lead.getString("fState") + "' AND customItem6__c = '省份') OR " +
                    "(customItem2__c = '" + lead.getString("gender") + "' AND customItem6__c = '性别') OR " +
                    "(customItem2__c = '" + lead.getString("fCity") + "' AND customItem6__c =  '城市')  ";
            List<CustomEntity52__c> optionList = XobjectHelper.v2Query(opSql);
            logger.info("客户字段翻译：" + optionList.toString());
            if (optionList != null && optionList.size() > 0) {
                for (CustomEntity52__c option : optionList) {
                    if ("省份".equals(option.getCustomItem6__c())) {
                        lead.put("fState", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("城市".equals(option.getCustomItem6__c())) {
                        lead.put("fCity", Integer.valueOf(option.getCustomItem4__c()));
                    }
                    if ("性别".equals(option.getCustomItem6__c())) {
                        lead.put("gender", Integer.valueOf(option.getCustomItem4__c()));
                    }
                }
            }

            if (StringUtils.isNotBlank(lead.getString("ownerId")) || StringUtils.isNotBlank(lead.getString("customItem200__c")) || StringUtils.isNotBlank(lead.getString("customItem194__c"))) {
                String userSql = "SELECT id, phone FROM user WHERE phone = '" + lead.getString("ownerId") + "' OR phone= '" + lead.getString("customItem200__c") + "' OR phone= '" + lead.getString("customItem194__c") + "'";
                JSONArray userList = CommonHttpHelper.v2Query(client, userSql);
                logger.info("客户用户字段翻译：" + userList.toString());
                if (userList != null && userList.size() > 0) {
                    for (Object o : userList) {
                        JSONObject user = (JSONObject) o;
                        if (StringUtils.isNotBlank(lead.getString("ownerId")) && lead.getString("ownerId").equals(user.getString("phone"))) {
                            lead.put("ownerId", user.getLong("id"));
                        }
                        if (StringUtils.isNotBlank(lead.getString("customItem200__c")) && lead.getString("customItem200__c").equals(user.getString("phone"))) {
                            lead.put("customItem200__c", user.getLong("id"));
                        }
                        if (StringUtils.isNotBlank(lead.getString("customItem194__c")) && lead.getString("customItem194__c").equals(user.getString("phone"))) {
                            lead.put("customItem194__c", user.getLong("id"));
                        }
                    }
                }
            }

            //省
            String fState = lead.getString("fState");
            //市
            String fCity = lead.getString("fCity");
            if (StringUtils.isBlank(fState)) {
                fState = null;
                lead.put("fState", null);
            }
            if (StringUtils.isBlank(fCity)) {
                fCity = null;
                lead.put("fCity", null);
            }

            String countryNum = lead.getString("countryNum");
            String ownerIdStr = lead.getString("ownerId");
            logger.info("转换后参数;" + lead.toString());
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
                    JSONArray allocationList = CommonHttpHelper.v2Query(client, "select id, customItem23__c FROM customEntity51__c WHERE  customItem27__c = 2 AND  customItem2__c = " + dimDepart);
                    logger.info("跟进人部门对应公海池：" + userList.toString());
                    if (allocationList != null && allocationList.size() > 0) {
                        String highSeaStr = ((JSONObject) allocationList.get(0)).getString("customItem23__c");
                        highSeaId = Long.valueOf(highSeaStr);
                    }
                } else {
                    //新增的销售线索所有人电话对应得用户不存在,走自动分配。
                    logger.info("新增的销售线索所有人电话：" + ownerId + ",对应得用户不存在,重新分配。");
                    ownerIdStr = null;
                    lead.put("ownerId", null);
                    //throw new Exception("新增的销售线索所有人电话：" + ownerId + ",对应得用户不存在。");
                }
            }

            //负责人为空，自动分配。
            if (StringUtils.isBlank(ownerIdStr)) {
                allocationSet = checkAllocationSet(client, fState, fCity, countryNum);
                ownerId = Long.parseLong(allocationSet.getCustomItem13__c());
                logger.info("修改公海池分配对照表数据：" + allocationSet.toString());
                logger.info("本次资源分配用户ID" + ownerId);
                if (StringUtils.isNotBlank(allocationSet.getCustomItem23__c())) {
                    highSeaId = Long.valueOf(allocationSet.getCustomItem23__c());
                }
            }

            if (highSeaId == 0L) {
                throw new Exception("所有人：" + ownerId + "，没有对应得公海池");
            }
            //所有人
            lead.put("ownerId", ownerId);
            lead.put("highSeaId", highSeaId);
            lead.put("customItem197__c", lead.getString("countryNum"));
            lead.remove("countryNum");
            result = CommonHttpHelper.creatEntityObject(client, "lead", lead);
            result.put("code", result.getString("code"));
            if (RESULT_CODE.equals(result.getString("code")) && allocationSet != null) {
                try {
                    JSONObject object = JSONObject.parseObject(allocationSet.toString());
                    object.remove("name");
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
            sql = "SELECT id, name, customItem1__c, customItem2__c, customItem3__c , customItem4__c, customItem5__c, customItem6__c, customItem7__c, customItem8__c , customItem9__c, customItem13__c, customItem23__c, customItem24__c, customItem25__c FROM customEntity51__c WHERE customItem27__c = 2 AND customItem24__c = '0086' AND customItem1__c = " + fState + " AND customItem3__c = " + fCity + " ";
        } else {
            //查询国家区号
            sql = "SELECT id, name, customItem1__c, customItem2__c, customItem3__c , customItem4__c, customItem5__c, customItem6__c, customItem7__c, customItem8__c , customItem9__c, customItem13__c, customItem23__c, customItem24__c, customItem25__c FROM customEntity51__c WHERE customItem27__c = 2 AND customItem24__c = " + internationalNum + " ";
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

