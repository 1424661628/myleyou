package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lvmen on 2019/9/13
 */
@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String,Object> model = new HashMap<>();
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 查询skus
        List<Sku> skus = spu.getSkus();
        // 查询详情
        SpuDetail detail = spu.getSpuDetail();
        // 查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 查询商品的分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid2()));
        // 查询规格参数
        List<SpecGroup> specs = specClient.queryGroupByCid(spu.getCid3());

        model.put("spu",spu);
//        model.put("title",spu.getTitle());
//        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("detail",detail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        return model;
    }

    /**
     * 生成页面
     * @param spuId
     */
    public void createHtml(Long spuId){
        // 上下文 : 保存着Model
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        // 输出流 : 将文件保存在硬盘中
        File dest = new File("F:\\暑期就业\\_99黑马基础\\31-36主流框架\\乐优商城课件\\upload", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }

        try(PrintWriter writer = new PrintWriter(dest, "UTF-8")) { //流会自动关闭
            // 生成HTML
            templateEngine.process("item", context, writer);
        } catch (Exception e){
            log.error("[静态页服务] 生成静态页异常!", e);
        }
    }


    /**
     * 删除页面
     * @param spuId
     */
    public void deleteHtml(Long spuId) {
        // 输出流 : 将文件保存在硬盘中
        File dest = new File("F:\\暑期就业\\_99黑马基础\\31-36主流框架\\乐优商城课件\\upload", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
