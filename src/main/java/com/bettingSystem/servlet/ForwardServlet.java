package com.bettingSystem.servlet;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
/**
 * Request Forward
 * @author jiangyali
 * @date 2025/12/05
 */
@WebServlet("/*")
public class ForwardServlet extends HttpServlet {
    /**
     * 根据路径信息转发到不同的Servlet
     * @param request HttpServletRequest对象，包含客户端请求信息
     * @param response HttpServletResponse对象，用于向客户端发送响应
     * @throws ServletException 如果发生与Servlet相关的错误
     * @throws IOException 如果发生输入输出错误
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        String pathInfo = request.getPathInfo();
        String result = "/helloServlet";
        PrintWriter out = response.getWriter();
        // getPathInfo forward
        if (pathInfo != null && pathInfo.contains("/session")) {
            if(pathInfo.lastIndexOf("/") > 0){
                String customerId = pathInfo.substring(1,pathInfo.lastIndexOf("/"));
                if(customerId == null || "".equals(customerId)){
                    out.println("customerid is not null");
                }
                request.setAttribute("customerId", customerId);
                result = pathInfo.substring(pathInfo.lastIndexOf("/"));
            }
        } else if (pathInfo != null && pathInfo.contains("/highstakes")) {
            if(pathInfo.lastIndexOf("/") > 0){
                String betofferid = pathInfo.substring(1,pathInfo.lastIndexOf("/"));
                if(betofferid == null || "".equals(betofferid)){
                    out.println("betofferid is not null");
                }
                request.setAttribute("betofferid", betofferid);
                result = pathInfo.substring(pathInfo.lastIndexOf("/"));
            }
        }
        //转发请求
        request.getRequestDispatcher(result).forward(request, response);
    }
    /**
     * 处理POST请求，根据路径信息转发到不同的Servlet
     * @param request HttpServletRequest对象，包含客户端请求信息
     * @param response HttpServletResponse对象，用于向客户端发送响应
     * @throws ServletException 如果发生与Servlet相关的错误
     * @throws IOException 如果发生输入输出错误
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String result = "";
        PrintWriter out = response.getWriter();
        if (pathInfo != null && pathInfo.contains("/stake")) {
            if(pathInfo.lastIndexOf("/") > 0){
                String betofferid = pathInfo.substring(1,pathInfo.lastIndexOf("/"));
                if(betofferid == null || "".equals(betofferid)){
                    out.println("betofferid is not null");
                }
                request.setAttribute("betofferid", betofferid);
                String sessionKey = request.getParameter("sessionKey");
                if(sessionKey == null || "".equals(sessionKey)){
                    out.println("sessionKey is not null");
                }
                request.setAttribute("sessionKey", sessionKey);
                //get body data
                String data = request.getReader().readLine();
                if(data == null || "".equals(data)){
                    out.println("data is not null");
                }
                request.setAttribute("amount", data);
                result = pathInfo.substring(pathInfo.lastIndexOf("/"));
            }
        }
        //转发请求
        request.getRequestDispatcher(result).forward(request, response);
    }
}