package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by lvmen on 2019/9/7
 */
@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specService;

    /**
     * 根据商品分类id查询商品规格组
     * @param cid 商品分类id
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> list = specService.queryGroupByCid(cid);
        return ResponseEntity.ok(list);
    }

    /**
     * 根据规格组id或商品分类id查询其全部商品规格参数信息
     * @param gid 商品规格分组id
     * @param cid 商品分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamList(
            @RequestParam(value = "gid", required=false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching){
        List<SpecParam> list = specService.queryParamList(gid, cid, searching);
        return ResponseEntity.ok(list);
    }


    /**
     * 新增规格组
     * @param specGroup
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveGroup(@RequestBody SpecGroup specGroup){
        System.out.println("======传进来的参数=========" + specGroup);
        specService.saveGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 新增规格参数
     * @param specParam
     * @return
     */
    @PostMapping("params")
    public ResponseEntity<Void> saveParam(@RequestBody SpecParam specParam){
        specService.saveParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改商品规格参数
     * @param specParam
     * @return
     */
    @PutMapping("params")
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam specParam){
        specService.updateParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据商品规格参数id删除
     * @param id
     * @return
     */
    @DeleteMapping("params")
    public ResponseEntity<Void> deleteParam(@RequestParam("id") Long id){
        specService.deleteParam(id);
        return ResponseEntity.ok(null);
    }

    /**
     * 根据分类id查询规格组,组内参数
     * @param cid
     * @return
     */
    //方法名和api中的不一致没有关系,只要路径和参数一致就对了.[靠http的请求]
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specService.queryListByCid(cid));
    }

}
