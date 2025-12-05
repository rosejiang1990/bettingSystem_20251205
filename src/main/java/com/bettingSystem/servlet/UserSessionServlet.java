package com.bettingSystem.servlet;
import com.bettingSystem.util.RandomUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Session请求
 * @author jiangyali
 * @date 2025/12/05
 */
@WebServlet("/session")
public class UserSessionServlet extends HttpServlet {

    private final Lock lock = new ReentrantLock();
    
    /**
     * 获取或生成用户会话token
     * @param request HttpServletRequest对象，包含客户端请求信息
     * @param response HttpServletResponse对象，用于向客户端发送响应
     * @throws IOException 当输入输出异常时抛出
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String customerid = request.getAttribute("customerId").toString();
        HttpSession session = request.getSession();
        String token = (String)session.getAttribute(customerid);
        if(token == null || "".equals(token)){
            //If the token does not exist, regenerate it and set the expiration time to 10 minutes
            if (lock.tryLock()) {
                try {
                    token = RandomUtil.randomToken();
                    session.setAttribute(customerid, token);
                    session.setMaxInactiveInterval(10 * 60);
                } finally {
                    lock.unlock();
                }
            }else{
                // 如果无法立即获取锁，返回错误响应
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().println("Server busy, please try again later.");
            }
        }
        PrintWriter out = response.getWriter();
        out.println(token);
    }
}
