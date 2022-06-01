package com.zhang.ssmschoolshop.controller.front;


import com.zhang.ssmschoolshop.entity.*;
import com.zhang.ssmschoolshop.service.ActivityService;
import com.zhang.ssmschoolshop.service.CateService;
import com.zhang.ssmschoolshop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private CateService cateService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ActivityService activityService;


    @RequestMapping("/")
    public String showAdmin(Model model, HttpSession session) {
        Integer userid;
        User user = (User) session.getAttribute("user");
        if (user == null) {
            userid = null;
        } else {
            userid = user.getUserid();
        }

        //商品分类
        List<Category> categoryList = cateService.findAll();

        ActivityExample example = new ActivityExample();
        List<Activity> activityList = activityService.getAllActivity(example);
        //活动商品
        List<Goods> activityGoods = new ArrayList<>();
        List<List<Goods>> goodsList = new ArrayList<>();
        List<Activity> activityList_1 = new ArrayList<>();

        //活动对应商品
        for (Activity activity:activityList
             ) {
            activityGoods = goodsService.findGoodsByActiveId(activity.getActivityid());
            goodsList.add(activityGoods);
        }

        List<Goods> goodsAndImage = new ArrayList<>();

        for (List<Goods> goodsList1:goodsList) {
            //获取每个商品的图片
            for (Goods goods:goodsList1) {
                //判断是否为登录状态
                if (userid == null) {
                    goods.setFav(false);
                } else {
                    Favorite favorite = goodsService.selectFavByKey(new FavoriteKey(userid, goods.getGoodsid()));
                    if (favorite == null) {
                        goods.setFav(false);
                    } else {
                        goods.setFav(true);
                    }
                }
                List<ImagePath> imagePathList = goodsService.findImagePath(goods.getGoodsid());
                goods.setImagePaths(imagePathList);
                goodsAndImage.add(goods);
            }

        }
        
        model.addAttribute("goodsList",goodsList);
        model.addAttribute("activityList",activityList);
        model.addAttribute("categoryList",categoryList);

        return "main";
    }




    @RequestMapping("/main")
    public String showAllGoods(Model model, HttpSession session) {
        Integer userid;
        User user = (User) session.getAttribute("user");
        if (user == null) {
            userid = null;
        } else {
            userid = user.getUserid();
        }

        //商品分类
        List<Category> categoryList = cateService.findAll();

        ActivityExample example = new ActivityExample();
        List<Activity> activityList = activityService.getAllActivity(example);
        //活动商品
        List<Goods> activityGoods = new ArrayList<>();
        List<List<Goods>> goodsList = new ArrayList<>();
        //活动对应商品
        for (Activity activity:activityList
        ) {
            activityGoods = goodsService.findGoodsByActiveId(activity.getActivityid());
            goodsList.add(activityGoods);
        }
        List<Goods> goodsAndImage = new ArrayList<>();

        for (List<Goods> goodsList1:goodsList) {
        //获取每个商品的图片
        for (Goods goods:goodsList1) {
            //判断是否为登录状态
            if (userid == null) {
                goods.setFav(false);
            } else {
                Favorite favorite = goodsService.selectFavByKey(new FavoriteKey(userid, goods.getGoodsid()));
                if (favorite == null) {
                    goods.setFav(false);
                } else {
                    goods.setFav(true);
                }
            }
            List<ImagePath> imagePathList = goodsService.findImagePath(goods.getGoodsid());
            goods.setImagePaths(imagePathList);
            goodsAndImage.add(goods);
        }

        }
//        System.out.println(goodsAndImage);
//        System.out.println(goodsList);
        model.addAttribute("goodsList",goodsList);
        model.addAttribute("activityList",activityList);
        model.addAttribute("categoryList",categoryList);

        return "main";
    }

    public List<Goods> getCateGoods(String cate, Integer userid) {
        //查询分类
        CategoryExample digCategoryExample = new CategoryExample();
        digCategoryExample.or().andCatenameLike(cate);
        List<Category> digCategoryList = cateService.selectByExample(digCategoryExample);

        if (digCategoryList.size() == 0) {
            return null;
        }

        //查询属于刚查到的分类的商品
        GoodsExample digGoodsExample = new GoodsExample();
        List<Integer> digCateId = new ArrayList<Integer>();
        for (Category tmp:digCategoryList) {
            digCateId.add(tmp.getCateid());
        }
        digGoodsExample.or().andCategoryIn(digCateId);

        List<Goods> goodsList = goodsService.selectByExampleLimit(digGoodsExample);

        List<Goods> goodsAndImage = new ArrayList<>();
        //获取每个商品的图片
        for (Goods goods:goodsList) {
            //判断是否为登录状态
            if (userid == null) {
                goods.setFav(false);
            } else {
                Favorite favorite = goodsService.selectFavByKey(new FavoriteKey(userid, goods.getGoodsid()));
                if (favorite == null) {
                    goods.setFav(false);
                } else {
                    goods.setFav(true);
                }
            }

            List<ImagePath> imagePathList = goodsService.findImagePath(goods.getGoodsid());
            goods.setImagePaths(imagePathList);
            goodsAndImage.add(goods);
        }
        return goodsAndImage;
    }
}
