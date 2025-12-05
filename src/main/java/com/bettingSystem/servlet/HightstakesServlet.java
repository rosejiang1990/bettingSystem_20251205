package com.bettingSystem.servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 * Get the highest amount
 * @author jiangyali
 * @date 2025/12/05
 */
@WebServlet("/highstakes")
public class HightstakesServlet extends HttpServlet {
    /**
     * 获取指定投注项的最高投注金额信息
     * @param request HttpServletRequest对象，包含客户端请求信息
     * @param response HttpServletResponse对象，用于向客户端发送响应
     * @throws IOException 如果发生输入输出异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String betoferid = "";
        if(null != request.getAttribute("betofferid")){
            betoferid = request.getAttribute("betofferid").toString();
        }
        Map<String, Map<String, List<BigDecimal>>> map = new HashMap<>();
        if(null != session.getAttribute("map")){
            map = (Map<String, Map<String, List<BigDecimal>>>)session.getAttribute("map");
        }
        //get current betoferid amount Map
        Map<String, BigDecimal> betoferMap = getBetoferMap(betoferid,map);
        //Sort the betting amounts corresponding to each customer, take the top 20, and return them
        String result= getAmountSort(betoferMap);
        PrintWriter out = response.getWriter();
        out.println(result);
    }
    /**
     * 对给定的BigDecimal列表进行排序并返回最大值
     * @param oldValue 之前存储的最大值，可能为null
     * @param twoList 需要合并和排序的BigDecimal列表，可能为null或空
     * @return 排序后的最大值，如果输入都为空则返回0
     */
    private BigDecimal sortList(BigDecimal oldValue, List<BigDecimal> twoList){
        BigDecimal result = new BigDecimal(0);
        List<BigDecimal> dataList = new ArrayList<>();
        if(null != oldValue ){
            dataList.add(oldValue);
        }
        if(null != twoList && twoList.size() > 0){
            dataList.addAll(twoList);
        }
        Collections.sort(dataList, (o1, o2) -> o2.compareTo(o1));
        if(null != dataList && dataList.size() > 0){
            if(oldValue == null || dataList.get(0).compareTo(oldValue) > 0){
                result = dataList.get(0);
            }else{
                result = oldValue;
            }
        }else{
            result = oldValue;
        }
        return result;
    }
    /**
     * 根据投注项ID获取对应的客户投注金额映射
     * @param betoferid 投注项ID，用于筛选特定投注项的数据
     * @param map 包含所有客户投注数据的原始映射，结构为<客户ID, <投注项ID, 投注金额列表>>
     * @return 返回一个并发安全的映射，包含每个客户在指定投注项上的最高投注金额，结构为<客户ID, 最高投注金额>
     */
    private Map<String, BigDecimal> getBetoferMap(String betoferid,Map<String, Map<String, List<BigDecimal>>> map){
        Map<String, BigDecimal> betoferMap = new ConcurrentHashMap<>();
        for (Map.Entry<String, Map<String, List<BigDecimal>>> entry : map.entrySet()) {
            List<BigDecimal> bigDecimalList = entry.getValue().get(betoferid);
            BigDecimal sortValue = null;
            if(betoferMap.get(entry.getKey()) != null){
                sortValue = sortList(betoferMap.get(entry.getKey()),bigDecimalList);
            }else{
                sortValue = sortList(null,bigDecimalList);
            }
            if(null != sortValue){
                betoferMap.put(entry.getKey(),sortValue);
            }
        }
        return betoferMap;
    }
    /**
     * 对投注金额映射进行排序并返回前N条记录（默认前2条）的字符串
     *
     * @param betoferMap 包含客户ID和对应投注金额的映射
     * @return 格式化后的字符串，包含排序后的前N条记录，若为空则返回空字符串
     */
    private String getAmountSort(Map<String, BigDecimal> betoferMap){
        String result = "";
        Map<String, BigDecimal> sortedByValueDesc = betoferMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new));
        int hashSize = 2;
        if(null == sortedByValueDesc || sortedByValueDesc.size() == 0){
            hashSize = 0;
        }
        if(null != sortedByValueDesc && sortedByValueDesc.size() < 2){
            hashSize = sortedByValueDesc.size();
        }
        int count = 0;
        Iterator<Map.Entry<String, BigDecimal>> iterator = sortedByValueDesc.entrySet().iterator();
        StringBuffer resultStr = new StringBuffer();
        while (iterator.hasNext() && count < hashSize) {
            Map.Entry<String, BigDecimal> entry = iterator.next();
            resultStr.append(","+entry.getKey()+"="+entry.getValue());
            count++;
        }
        if(resultStr.length() > 0){
            result = resultStr.substring(1);
        }
        return result;
    }
}
