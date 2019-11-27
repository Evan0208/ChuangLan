package other.apiaccount.activateaccount;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.api.ApiSupport;
import com.rkhd.platform.sdk.api.annotations.RequestMethod;
import com.rkhd.platform.sdk.api.annotations.RestMapping;
import com.rkhd.platform.sdk.data.model.CustomEntity35__c;
import com.rkhd.platform.sdk.http.Request;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.OperateResult;
import com.rkhd.platform.sdk.service.XObjectService;
import commons.utils.ChuangLanCRMHttpUtil;
import org.apache.commons.lang.StringUtils;


/**
 * 激活API主账号
 *
 * @author gongqiang
 */
public class ActivateApiAccount implements ApiSupport {

    private Logger logger = LoggerFactory.getLogger();

    private static Integer IS_CMPP = 1;

    @Override
    @RestMapping(value = "/apiAccount-activate", method = RequestMethod.POST)
    public String execute(Request request, Long userId, Long tenantId) {
        logger.info("激活API主账号开始");
        String result = "";
        try {
            String idStr = request.getParameter("id");
            logger.info("主账号ID" + idStr);

            CustomEntity35__c apiAccount = new CustomEntity35__c();
            apiAccount.setId(Long.valueOf(idStr));
            apiAccount = XObjectService.instance().get(apiAccount);
            logger.info("查询API主账号信息，" + apiAccount.toString());

            String productId = apiAccount.getCustomItem26__c();
            String accountId = "";
            if (apiAccount.getCustomItem21__c() != null) {
                accountId = apiAccount.getCustomItem21__c().toString();
            } else {
                throw new Exception("API账号对应得自助通账号ERPID不能为空。");
            }

            if (StringUtils.isBlank(accountId) || StringUtils.isBlank(productId)) {
                throw new Exception("API账号对应得自助通账号ERPID和产品ID不能为空。");
            }

            Integer isCmpp = 0;
            if (apiAccount.getCustomItem17__c() != null && IS_CMPP.equals(apiAccount.getCustomItem17__c())) {
                isCmpp = 1;
            }
            String spId = "";
            if (apiAccount.getCustomItem18__c() != null) {
                spId = apiAccount.getCustomItem18__c().toString();
            }
            String spCode = "";
            if (apiAccount.getCustomItem19__c() != null) {
                spCode = apiAccount.getCustomItem19__c().toString();
            }
            String speed = "";
            if (apiAccount.getCustomItem20__c() != null) {
                speed = apiAccount.getCustomItem20__c();
            }

            JSONObject appId = ChuangLanCRMHttpUtil.addMainApiAccount2ERP(productId, accountId, isCmpp, spId, spCode, speed);
            if (appId != null) {
                CustomEntity35__c apiAccountUpdate = new CustomEntity35__c();
                apiAccountUpdate.setId(apiAccount.getId());
                apiAccountUpdate.setAPPID__c(appId.getString("appId"));
                apiAccountUpdate.setERPID__c(appId.getInteger("id"));
                apiAccountUpdate.setCustomItem1__c(appId.getString("account"));
                //API账号已激活
                apiAccountUpdate.setCustomItem27__c(2);
                OperateResult updateOperateResult = XObjectService.instance().update(apiAccountUpdate, true);
                logger.info("API主账号修改，返回信息：" + JSONObject.toJSONString(updateOperateResult));
                if (!updateOperateResult.getSuccess()) {
                    logger.error("API主账号激活成功，回写失败，主账号ID：" + apiAccount.getId() + " ,appId:" + appId + " ");
                }
                result = "激活账号成功";
            } else {
                result = "激活账号失败，请重试。";
            }
        } catch (Exception ex) {
            logger.error("激活API主账号报错，信息：" + ex.getMessage());
            result = "激活API主账号报错，信息：" + ex.getMessage();
        }
        return result;
    }
}
