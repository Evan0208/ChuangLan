package main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.data.model.CustomEntity56__c;
import com.rkhd.platform.sdk.data.model.Order;
import com.rkhd.platform.sdk.data.model.Quote;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.service.XObjectService;
import commons.utils.ChuangLanCRMHttpUtil;
import commons.utils.CommonHttpHelper;
import commons.utils.DateTimeHelper;
import other.apiaccount.activateaccount.ActivateApiAccount;
import other.order.prepaidorderfirst.PrepaidOrderFirst;
import other.order.prepaidorderfirst.PrepaidOrderFirst2;
import other.quote.writebackapiaccount.WriteBackApiAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author gongqiang
 */
public class Main {


    public static void main(String[] args) {
        try {
            System.out.println("==========创蓝文化项目==========");

            //Long balance = ChuangLanCRMHttpUtil.checkAccountBalance("555555", 2);
            //String password = ChuangLanCRMHttpUtil.modifyPassword2ERP(13700);
            //JSONObject appId = ChuangLanCRMHttpUtil.addMainApiAccount2ERP("50","94998",1,"941019","941019","50");

            // ActivateApiAccount activateApiAccount=new ActivateApiAccount();
            // activateApiAccount.execute(null,0L,0L);

//            Quote entity = new Quote();
//           entity.setId(971620218061440L);
//           entity = XObjectService.instance().get(entity, true);
//           Map<String, Object> dataMap = JSONObject.parseObject(entity.toString());
//           dataMap.put("belongId", 409L);
//           DataModel model = new DataModel(dataMap);
//           List<DataModel> dataModelList = new ArrayList<>();
//           dataModelList.add(model);
//           ScriptTriggerParam scriptTriggerParam = new ScriptTriggerParam();
//           scriptTriggerParam.setDataModelList(dataModelList);
//
//            WriteBackApiAccount prepaidOrderFirst2=new WriteBackApiAccount();
//            prepaidOrderFirst2.execute(scriptTriggerParam);

            System.out.println("main 结束");

        } catch (Exception ex) {
            System.out.println("main 错误Exception：" + ex);
        }
    }

    public void test() {


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 7, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));
        ExecutorCompletionService<String> executorCompletionService = new ExecutorCompletionService(threadPoolExecutor);

        //两个线程的线程池

        ExecutorService executor = Executors.newFixedThreadPool(2);
        //小红买酒任务，这里的future2代表的是小红未来发生的操作，返回小红买东西这个操作的结果
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("爸：小红你去买瓶酒！");
            try {
                System.out.println("小红出去买酒了，女孩子跑的比较慢，估计5s后才会回来...");
                Thread.sleep(5000);
                return "我买回来了！";
            } catch (InterruptedException e) {
                System.err.println("小红路上遭遇了不测");
                return "来世再见！";
            }
        }, executor);

        //小明买烟任务，这里的future1代表的是小明未来买东西会发生的事，返回值是小明买东西的结果

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("爸：小明你去买包烟！");
            try {
                System.out.println("小明出去买烟了，可能要3s后回来...");
                Thread.sleep(3000);
                return "我买回来了!";
            } catch (InterruptedException e) {
                System.out.println("小明路上遭遇了不测！");
                return "这是我托人带来的口信，我已经不在了。";
            }
        }, executor);

        //获取小红买酒结果，从小红的操作中获取结果，把结果打印
        future2.thenAccept((e) -> {
            System.out.println("小红说：" + e);
        });
        //获取小明买烟的结果
        future1.thenAccept((e) -> {
            System.out.println("小明说：" + e);
        });

        System.out.println("爸：loading......");
        System.out.println("爸:我觉得无聊甚至去了趟厕所。");
        System.out.println("爸：loading......");
    }

}



