package other.selfserviceaccounts.resetpassword;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.api.ApiSupport;
import com.rkhd.platform.sdk.api.annotations.RequestMethod;
import com.rkhd.platform.sdk.api.annotations.RestMapping;
import com.rkhd.platform.sdk.data.model.CustomEntity11__c;
import com.rkhd.platform.sdk.data.model.CustomEntity18__c;
import com.rkhd.platform.sdk.http.Request;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.service.XObjectService;
import commons.utils.ChuangLanCRMHttpUtil;
import org.apache.commons.lang.StringUtils;

/**
 * 自助通账号重置密码
 *
 * @author gongqiang
 */
public class ResetPassword implements ApiSupport {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    @RestMapping(value = "/password-reset", method = RequestMethod.POST)
    public String execute(Request request, Long userId, Long tenantId) {
        logger.info("自助通账号重置密码开始");
        String result = "";
        try {
            String idStr = request.getParameter("id");
            logger.info("自助通账号配置ID" + idStr);

            CustomEntity11__c selfServiceSet = new CustomEntity11__c();
            selfServiceSet.setId(Long.valueOf(idStr));
            selfServiceSet = XObjectService.instance().get(selfServiceSet);
            logger.info("查询自助通账号配置信息，" + selfServiceSet.toString());
            //自助通账号
            long selfServiceAccountId = selfServiceSet.getCustomItem33__c();

            CustomEntity18__c selfServiceAccount = new CustomEntity18__c();
            selfServiceAccount.setId(selfServiceAccountId);
            selfServiceAccount = XObjectService.instance().get(selfServiceAccount);
            logger.info("查询自助通账号信息，" + selfServiceAccount.toString());

            Integer erpId = selfServiceAccount.getCustomItem44__c();
            if (erpId == null) {
                throw new Exception("自助通账号没有ERPID");
            }
            String password = ChuangLanCRMHttpUtil.modifyPassword2ERP(erpId);
            if (StringUtils.isNotBlank(password)) {
                result = "重置密码成功，已将密码发送至客户手机";
                //"重置密码成功， <br> 新密码：" + password;
            } else {
                throw new Exception("自助通账号重置密码失败");
            }
        } catch (Exception ex) {
            logger.error("自助通账号重置密码报错，信息：" + ex.getMessage());
            result = "自助通账号重置密码报错，信息：" + ex.getMessage();
        }
        return result;
    }
}
