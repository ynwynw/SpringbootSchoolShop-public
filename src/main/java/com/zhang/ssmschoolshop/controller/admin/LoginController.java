package com.zhang.ssmschoolshop.controller.admin;


import com.github.pagehelper.util.StringUtil;
import com.zhang.ssmschoolshop.entity.Admin;
import com.zhang.ssmschoolshop.entity.AdminExample;
import com.zhang.ssmschoolshop.entity.User;
import com.zhang.ssmschoolshop.service.AdminService;
import com.zhang.ssmschoolshop.util.Md5Util;
import com.zhang.ssmschoolshop.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class LoginController {

    @Autowired
    private AdminService adminService;

    @RequestMapping("/login")
    public String adminLogin() {
        return "adminLogin";
    }

    @RequestMapping("/confirmLogin")
    public String confirmLogin(Admin admin, Model model, HttpServletRequest request) {
        admin.setPassword(Md5Util.MD5Encode(admin.getPassword(),"utf-8"));
        Admin selectAdmin = adminService.selectByName(admin);
        if (selectAdmin == null) {
            model.addAttribute("errorMsg", "用户名或密码错误");
            return "adminLogin";
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("admin", selectAdmin);
            return "redirect:/admin/user/show";
        }
    }

    @RequestMapping("/logout")
    public String adminLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("admin");
        return "redirect:/admin/login";
    }

    @RequestMapping("/show")
    public String adminShow(Model model, HttpSession session){
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("admin",admin);
        return "adminManage";
    }

    @RequestMapping("/showJson")
    @ResponseBody
    public Msg showJson(String adminName ,HttpSession session){

        Admin admin = new Admin();
        admin = (Admin) session.getAttribute("admin");
        admin.setAdminname(admin.getAdminname());
        if (StringUtil.isNotEmpty(adminName)){
            admin.setAdminname(adminName);
        }

        Admin admin1 = adminService.selectByName(admin);
        return Msg.success("查询成功").add("admin",admin1);
    }

    @PostMapping("/saveInfo")
    @ResponseBody
    public Msg saveInfo(String name, HttpServletRequest request) {
        HttpSession session = request.getSession();
        AdminExample adminExample = new AdminExample();
        Admin admin,updateAdmin = new Admin();
        Admin admins;
        Integer adminId;
        admin = (Admin) session.getAttribute("admin");
        adminId = admin.getAdminid();
        admins =adminService.selectByName(admin);
        if (!StringUtils.isEmpty(admin)){
            updateAdmin.setAdminid(admins.getAdminid());
            updateAdmin.setAdminname(name);
            adminService.updateByPrimaryKey(updateAdmin);
            return Msg.success("更新成功");
        }else {
            return Msg.fail("更新失败");
        }
    }

    @PostMapping("/savePsw")
    @ResponseBody
    public Msg savePsw(String Psw, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Admin admin = (Admin) session.getAttribute("admin");
        if (Psw.length()<6){
            return Msg.fail("密码格式错误");
        }
        admin.setPassword(Md5Util.MD5Encode(Psw, "UTF-8"));
        adminService.updateByPrimaryKey(admin);
        return Msg.success("修改密码成功");
    }

    public static void main(String[] args) {
        System.out.println(Md5Util.MD5Encode("123456","utf-8"));
    }
}
