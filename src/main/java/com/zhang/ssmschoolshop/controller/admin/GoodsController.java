package com.zhang.ssmschoolshop.controller.admin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import com.zhang.ssmschoolshop.entity.*;
import com.zhang.ssmschoolshop.entity.vo.GoodsSalesVO;
import com.zhang.ssmschoolshop.service.CateService;
import com.zhang.ssmschoolshop.service.GoodsService;
import com.zhang.ssmschoolshop.service.OrderService;
import com.zhang.ssmschoolshop.util.ImageUtil;
import com.zhang.ssmschoolshop.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;


@Controller
@RequestMapping("/admin/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @RequestMapping("/showjson")
    @ResponseBody
    public Msg getAllGoods(@RequestParam(value = "page", defaultValue = "1") Integer pn, HttpServletResponse response, Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return Msg.fail("请先登录");
        }
        //一页显示几个数据
        PageHelper.startPage(pn, 9);
        List<Goods> employees = goodsService.selectByExample(new GoodsExample());
        for(Goods good:employees){
            System.out.println(good);
        }
        //显示几个页号
        PageInfo page = new PageInfo(employees, 5);

        model.addAttribute("pageInfo", page);

        return Msg.success("查询成功!").add("pageInfo", page);
    }

    @RequestMapping("/show")
    public String goodsManage(@RequestParam(value = "page", defaultValue = "1") Integer pn, HttpServletResponse response, Model model, HttpSession session) throws IOException {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        List<Category> categoryList = cateService.selectByExample(new CategoryExample());
        model.addAttribute("categoryList", categoryList);
        return "adminAllGoods";
    }

    @RequestMapping("/salesJson")
    @ResponseBody
    public Msg getAllSales(@RequestParam(value = "page",defaultValue = "1") Integer pn,String order, int categoryid, HttpSession session){
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return Msg.fail("请先登录");
        }

        GoodsExample goodsExample = new GoodsExample();
        //判断是否进行的分类查询
        if (categoryid!=0){
            goodsExample.or().andCategoryEqualTo(categoryid);
        }

        List<Goods> goodsList;
        //一页显示几个数据
        PageHelper.startPage(pn, 10);
        PageHelper.orderBy("num desc");
        goodsList = goodsService.selectByExample(goodsExample);

        List<GoodsSalesVO> list = new ArrayList<>();
        int sum;
        for (Goods good:goodsList) {
            Category category ;
            GoodsSalesVO goodsSalesVO = new GoodsSalesVO();
            if (!"".equals(orderService.getSumByGoodsId(good.getGoodsid())) && orderService.getSumByGoodsId(good.getGoodsid())!=null){
                sum = orderService.getSumByGoodsId(good.getGoodsid());
                goodsSalesVO.setNum(sum);
            }else {
                goodsSalesVO.setNum(0);
            }
            goodsSalesVO.setGoodsId(good.getGoodsid());
            goodsSalesVO.setGoodsName(good.getGoodsname());
            category = cateService.selectById(good.getCategory());
            goodsSalesVO.setCategoryName(category.getCatename());
            list.add(goodsSalesVO);
        }

//        if ("ASC".equals(order)){
//            Collections.sort(list, new Comparator<GoodsSalesVO>() {
//                @Override
//                public int compare(GoodsSalesVO o1, GoodsSalesVO o2) {
//                    return o1.getNum() - o2.getNum();
//                }
//            });//倒序
//        }
//        if ("DESC".equals(order)){
//            Collections.sort(list, new Comparator<GoodsSalesVO>() {
//                @Override
//                public int compare(GoodsSalesVO o1, GoodsSalesVO o2) {
//                    return o2.getNum() - o1.getNum();
//                }
//            });//正序
//        }

        //顶部header商品分类
        List<Category> categoryList = cateService.findAll();
        //显示几个页号
        PageInfo page = new PageInfo(goodsList, 5);
        return Msg.success("成功").add("pageInfo",page).add("list",list).add("categoryList",categoryList);
    }

    @RequestMapping("/sales/{categoryid}")
    public String userManage(@PathVariable int categoryid, Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("categoryid",categoryid);
        return "adminGoodsSales";
    }

    @RequestMapping("/add")
    public String showAdd(@ModelAttribute("succeseMsg") String msg, Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        if (!msg.equals("")) {
            model.addAttribute("msg", msg);
        }
        List<Category> categoryList = cateService.selectByExample(new CategoryExample());
        model.addAttribute("categoryList", categoryList);
        //还需要查询分类传给addGoods页面
        return "addGoods";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Msg updateGoods(Goods goods, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return Msg.fail("请先登录");
        }
        /* goods.setGoodsid(goodsid);*/
        goodsService.updateGoodsById(goods);
        return Msg.success("更新成功!");
    }

    @RequestMapping(value = "/delete/{goodsid}", method = RequestMethod.DELETE)
    @ResponseBody
    public Msg deleteGoods(@PathVariable("goodsid") Integer goodsid) {
        goodsService.deleteGoodsById(goodsid);
        return Msg.success("删除成功!");
    }

    @RequestMapping("/addGoodsSuccess")
    public String addGoods(Goods goods,
                           @RequestParam MultipartFile[] fileToUpload,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) throws IOException {
        /*goods.setCategory(1);*/
        goods.setUptime(new Date());
        goods.setActivityid(1);
        goodsService.addGoods(goods);
        for (MultipartFile multipartFile : fileToUpload) {
            String fileName = goods.getGoodsname()+ multipartFile.getOriginalFilename();
            if (multipartFile != null) {
               String ImagePath= ImageUtil.imagePath(multipartFile,fileName);
               System.out.println("最后存入数据的图片名字为:"+ImagePath);
                //把图片路径存入数据库中
              goodsService.addImagePath(new ImagePath(null, goods.getGoodsid(), ImagePath));

            }
        }

        redirectAttributes.addFlashAttribute("succeseMsg", "商品添加成功!");

        return "redirect:/admin/goods/add";
    }

    @RequestMapping("/addCategory")
    public String addcategory(@ModelAttribute("succeseMsg") String msg, Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.or();
        List<Category> categoryList;
        categoryList = cateService.selectByExample(categoryExample);
        model.addAttribute("categoryList", categoryList);
        if (!msg.equals("")) {
            model.addAttribute("msg", msg);
        }
        return "addCategory";
    }

    @Autowired
    private CateService cateService;

    @RequestMapping("/addCategoryResult")
    public String addCategoryResult(Category category, Model addCategoryResult, RedirectAttributes redirectAttributes) {
        List<Category> categoryList = new ArrayList<>();
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.or().andCatenameEqualTo(category.getCatename());
        categoryList = cateService.selectByExample(categoryExample);
        if (!categoryList.isEmpty()) {
            redirectAttributes.addAttribute("succeseMsg", "分类已存在");
            return "redirect:/admin/goods/addCategory";
        } else {
            cateService.insertSelective(category);
            redirectAttributes.addFlashAttribute("succeseMsg", "分类添加成功!");
            return "redirect:/admin/goods/addCategory";
        }
    }

    @RequestMapping("/saveCate")
    @ResponseBody
    public Msg saveCate(Category category) {
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.or().andCatenameEqualTo(category.getCatename());
        List<Category> categoryList = cateService.selectByExample(categoryExample);
        if (categoryList.isEmpty()) {
            cateService.updateByPrimaryKeySelective(category);
            return Msg.success("更新成功");
        } else return Msg.success("名字已经存在");
    }

    @RequestMapping("/deleteCate")
    @ResponseBody
    public Msg deleteCate(Category category) {
        cateService.deleteByPrimaryKey(category.getCateid());
        return Msg.success("删除成功");
    }

    @RequestMapping("/search")
    @ResponseBody
    public Msg searchGoods(@RequestParam(value = "page",defaultValue = "1") Integer pn,int categoryid, HttpSession session, String goodsName ){
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return Msg.fail("请先登录");
        }

        GoodsExample goodsExample = new GoodsExample();
        //判断是否进行的分类查询
        if (categoryid!=0){
            goodsExample.or().andCategoryEqualTo(categoryid);
        }
        goodsExample.or().andGoodsnameLike("%"+goodsName+"%");
        //一页显示几个数据
        PageHelper.startPage(pn, 10);
        List<Goods> goodsList = goodsService.selectByExample(goodsExample);
        List<GoodsSalesVO> list = new ArrayList<>();
        int sum;
        for (Goods good:goodsList) {
            Category category =new Category();
            GoodsSalesVO goodsSalesVO = new GoodsSalesVO();
            if (!"".equals(orderService.getSumByGoodsId(good.getGoodsid())) && orderService.getSumByGoodsId(good.getGoodsid())!=null){
                sum = orderService.getSumByGoodsId(good.getGoodsid());
                goodsSalesVO.setNum(sum);
            }else {
                goodsSalesVO.setNum(0);
            }
            goodsSalesVO.setGoodsId(good.getGoodsid());
            goodsSalesVO.setGoodsName(good.getGoodsname());
            category = cateService.selectById(good.getCategory());
            goodsSalesVO.setCategoryName(category.getCatename());
            list.add(goodsSalesVO);
        }

        //顶部header商品分类
        List<Category> categoryList = cateService.findAll();
        //显示几个页号
        PageInfo page = new PageInfo(goodsList, 5);
        return Msg.success("成功").add("pageInfo",page).add("list",list).add("categoryList",categoryList);
    }

}
