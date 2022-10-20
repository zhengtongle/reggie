package org.example.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.common.R;
import org.example.entity.Employee;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 检查用户是否登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        // 不需要过滤的请求路径
        String[] uris = new String[]{"/employee/login", "/employee/logout", "/backend/**", "/front/**"};

        // 匹配请求路径是否需要处理
        boolean check = check(requestURI, uris);
        if (check) {
            filterChain.doFilter(request, response);
            return;
        }
        // 判断是否已登录并放行
        HttpSession session = request.getSession();
        if (session.getAttribute("employee") != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 未登录则返回未登录结果，通过输出流向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//        log.info("拦截到请求：{}", request.getRequestURI());
//        filterChain.doFilter(request, response);
    }

    /**
     * 路径匹配，本次请求是否需要处理
     *
     * @param uri
     * @param uris
     * @return
     */
    public boolean check(String uri, String[] uris) {
        for (String url : uris) {
            boolean match = PATH_MATCHER.match(url, uri);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
