package commons.utils;

import java.util.ArrayList;
import java.util.List;


import com.rkhd.platform.sdk.exception.ApiEntityServiceException;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.BatchOperateResult;
import com.rkhd.platform.sdk.model.OperateResult;
import com.rkhd.platform.sdk.model.QueryResult;
import com.rkhd.platform.sdk.model.XObject;
import com.rkhd.platform.sdk.service.XObjectService;

/**
 * @author Administrator
 */
public class XobjectHelper {
    private static final Logger logger = LoggerFactory.getLogger();

    /**
     * 默认操作数量
     */
    private static final int OP_NUM = 500;
    /**
     * query最大sql长度
     */
    private final static int QUERY_LENGTH = 1000;
    /**
     * query预留长度
     */
    private final static int QUERY_OBL_LENGTH = 50;

    /**
     * V2最大查询长度
     */
    final static int V2_LENGTH = 100;

    /**
     * V2SQL查询
     *
     * @param baseSql 查询SQL
     */
    public static <T extends XObject> List<T> v2Query(String baseSql) throws ApiEntityServiceException, ScriptBusinessException {
        long start = 0;
        List<T> list = new ArrayList<T>();
        boolean isTrue = false;
        do {
            isTrue = false;
            String sql = baseSql + " limit " + start + " , " + (start + V2_LENGTH);
            logger.debug("sql-->" + sql);
            QueryResult<T> result = XObjectService.instance().query(sql);
            start = start + V2_LENGTH;
            if (result.getSuccess()) {
                List<T> tempList = result.getRecords();
                if (tempList != null && tempList.size() > 0) {
                    list.addAll(tempList);
                    if (start < result.getTotalCount()) {
                        isTrue = true;
                        continue;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                throw new ScriptBusinessException("获取查询数据失败:" + list.getClass());
            }

        } while (isTrue);
        logger.debug("list-size->" + list.size());
        return list;

    }

    /**
     * V2SQL查询 WHERE IN 条件
     *
     * @param baseSql   查询SQL
     * @param whereList 查询条件
     */
    public static <T extends XObject> List<T> v2QuerySplitIn(String baseSql, List<String> whereList)
            throws ApiEntityServiceException, ScriptBusinessException {
        List<T> list = new ArrayList<T>();
        int sqlLength = QUERY_LENGTH - QUERY_OBL_LENGTH - baseSql.length();
        String tempsql = "";
        for (String item : whereList) {
            if ((tempsql.length() + item.length() + 1) > sqlLength) {
                String sql = String.format(baseSql, "(" + tempsql + ") ");
                logger.debug("formatSql:" + sql);
                list.addAll(v2Query(sql));
                tempsql = "";
            }
            if ("".equals(tempsql)) {
                tempsql = item;
            } else {
                tempsql = tempsql + "," + item;
            }
        }
        if (!"".equals(tempsql)) {
            String sql = String.format(baseSql, "(" + tempsql + ") ");
            logger.debug("formatsql:" + sql);
            list.addAll(v2Query(sql));
        }
        return list;
    }

    /**
     * 新增实体
     *
     * @param list 新增实体列表
     */
    public static <T extends XObject> BatchOperateResult insertAll(List<T> list) throws ApiEntityServiceException {
        BatchOperateResult batchResult = new BatchOperateResult();
        if (list == null || list.size() == 0) {
            logger.debug("需要添加的数据不存在");
            batchResult.setSuccess(false);
            batchResult.setErrorMessage("需要添加的数据不存在");
            return batchResult;
        }
        //获取实际数量
        int allCount = list.size();
        boolean isTrue = false;
        int start = 0;
        logger.debug("allCount-->" + allCount);
        do {
            isTrue = false;
            List<T> tempList;
            List<OperateResult> opList = new ArrayList<OperateResult>();
            if ((start + XobjectHelper.OP_NUM) < allCount) {
                tempList = list.subList(start, start + XobjectHelper.OP_NUM);
                start = start + XobjectHelper.OP_NUM;
                isTrue = true;
            } else {
                tempList = list.subList(start, allCount);
                start = allCount;
            }

            BatchOperateResult tempBatchResult = XObjectService.instance().insert(tempList, false, true);

            batchResult.setSuccess(tempBatchResult.getSuccess());
            if (tempBatchResult.getSuccess()) {
                if (batchResult.getOperateResults() != null && batchResult.getOperateResults().size() > 0) {
                    opList.addAll(tempBatchResult.getOperateResults());
                }

                batchResult.setOperateResults(opList);
                logger.debug(start + ":保存数据成功");
            } else {
                batchResult.setErrorMessage(start + ":保存数据失败:" + tempBatchResult.getErrorMessage());
            }
        } while (isTrue);
        return batchResult;
    }

    /**
     * 修改实体列表信息
     *
     * @param list 修改信息
     */
    public static <T extends XObject> BatchOperateResult updateAll(List<T> list) throws ApiEntityServiceException {
        BatchOperateResult batchResult = new BatchOperateResult();
        if (list == null || list.size() == 0) {
            logger.debug("需要添加的数据不存在");
            batchResult.setSuccess(false);
            batchResult.setErrorMessage("需要添加的数据不存在");
            return batchResult;
        }
        //获取实际数量
        int allCount = list.size();
        boolean isTrue = false;
        int start = 0;
        logger.debug("allCount-->" + allCount);
        do {
            isTrue = false;
            List<T> tempList;
            List<OperateResult> opList = new ArrayList<OperateResult>();
            if ((start + XobjectHelper.OP_NUM) < allCount) {
                tempList = list.subList(start, start + XobjectHelper.OP_NUM);
                start = start + XobjectHelper.OP_NUM;
                isTrue = true;
            } else {
                tempList = list.subList(start, allCount);
                start = allCount;
            }
            BatchOperateResult tempbatchresult = XObjectService.instance().update(tempList, false, true);
            batchResult.setSuccess(tempbatchresult.getSuccess());
            if (tempbatchresult.getSuccess()) {
                if (batchResult.getOperateResults() != null && batchResult.getOperateResults().size() > 0) {
                    opList.addAll(tempbatchresult.getOperateResults());
                }
                batchResult.setOperateResults(opList);
                logger.debug(start + ":更新数据成功");
            } else {
                batchResult.setErrorMessage(start + ":更新数据失败:" + tempbatchresult.getErrorMessage());
            }
        } while (isTrue);
        return batchResult;
    }
}
