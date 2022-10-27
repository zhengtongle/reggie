package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.common.R;
import org.example.entity.Employee;
import org.example.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        // 1.将页面提交的密码使用Md5混淆加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3.判断是否查询到结果
        if (emp == null){
            return R.error("登陆失败");
        }

        // 4.密码比对,不一致则返回失败
        if (!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }

        // 5.查看用户状态
        if (emp.getStatus() == 0){
            return R.error("登陆失败,账号已禁用");
        }
        // 6.返回登录成功的结果
        HttpSession session = request.getSession();
        session.setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.removeAttribute("employee");

        return R.success("退出成功");
    }

    /**
     * 新增用户
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        // 设置登陆密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置创建时间和登录时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户的Id
        Long userId = (Long) request.getSession().getAttribute("employee");

        // 设置创建用户
        employee.setCreateUser(userId);
        employee.setUpdateUser(userId);

        employeeService.save(employee);


        return R.success("新增员工成功");
    }
}
