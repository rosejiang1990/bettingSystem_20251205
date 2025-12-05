package com.bettingSystem.servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
/**
 * Bet request
 * @author jiangyali
 * @date 2025/12/05
 */
@WebServlet("/stake")
public class StakeServlet extends HttpServlet {

    private final Lock lock = new ReentrantLock();

    /**
     * 处理用户投注操作
     * @param request HttpServletRequest对象，包含客户端请求信息
     * @param response HttpServletResponse对象，用于向客户端发送响应
     * @throws IOException 如果发生输入输出异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionKey = "";
        if(null != request.getAttribute("sessionKey")){
            sessionKey = request.getAttribute("sessionKey").toString();
        }
        String betoferid = "";
        if(null != request.getAttribute("betofferid")){
            betoferid = request.getAttribute("betofferid").toString();
        }
        BigDecimal amount = new BigDecimal(0);
        if(null != request.getAttribute("amount")){
            amount = new BigDecimal(request.getAttribute("amount").toString());
        }
        //Check if the sessionKey exists in the session; if not, display an error message
        String customerId = getCustomerId(session, sessionKey);
        PrintWriter out = response.getWriter();
        if(customerId == null || "".equals(customerId)){
            out.println("session is timeOut or not exit");
        }
        //Retrieve betting promotions by customer ID; if the current bet exists, directly add the bet amount;
        //otherwise, create a new bet item and bet amount
        Map<String, Map<String, List<BigDecimal>>> map = getAmountStakeMap(session, betoferid, amount, customerId, response);
        out.println(map.toString());
    }

    /**
     * 根据会话密钥(sessionKey)从会话中获取客户ID
     * @param session HttpSession对象，包含当前会话的所有属性
     * @param sessionKey 要匹配的会话密钥值
     * @return 匹配到的客户ID，如果未找到则返回空字符串
     */
    private String getCustomerId(HttpSession session, String sessionKey){
        String customerId = "";
        Enumeration<String> attrs = session.getAttributeNames();
        while(attrs.hasMoreElements()){
            String name = attrs.nextElement().toString();
            if(name!=null && !"map".equals(name)){
                String value = (String)session.getAttribute(name);
                if(value.equals(sessionKey)){
                    customerId = name;
                }
            }
        }
        return customerId;
    }

    /**
     * 获取或更新客户投注金额映射表
     * @param session HttpSession对象，用于存储和获取投注数据
     * @param betoferid 投注项ID，作为二级映射的key
     * @param amount 当前投注金额，将被添加到对应投注项列表中
     * @param customerId 客户ID，作为一级映射的key
     * @return 更新后的完整投注金额映射表
     */
    private Map<String, Map<String, List<BigDecimal>>> getAmountStakeMap(HttpSession session,String betoferid,
                                                                         BigDecimal amount, String customerId,
                                                                        HttpServletResponse response) throws IOException {
        Map<String, Map<String, List<BigDecimal>>> map = new HashMap<>();
        if(null != session.getAttribute("map")){
            map = (Map<String, Map<String, List<BigDecimal>>>)session.getAttribute("map");
        }
        if (lock.tryLock()) {
            try{
                Map<String, List<BigDecimal>> custmpa = map.get(customerId);
                if(null != custmpa){
                    List<BigDecimal> decimalList = custmpa.get(betoferid);
                    if(null != decimalList && decimalList.size() > 0){
                        decimalList.add(amount);
                    }else{
                        List<BigDecimal> bigDecimals = new ArrayList<>();
                        bigDecimals.add(amount);
                        custmpa.put(betoferid,bigDecimals);
                    }
                }else{
                    List<BigDecimal> bigDecimals = new ArrayList<>();
                    bigDecimals.add(amount);
                    Map<String, List<BigDecimal>> amoutMap = new HashMap<>();
                    amoutMap.put(betoferid,bigDecimals);
                    map.put(customerId,amoutMap);
                }
                session.setAttribute("map",map);
            }finally {
                lock.unlock();
            }
        }else{
            response.getWriter().println("Server busy, please try again later.");
        }
        return map;
    }
}
