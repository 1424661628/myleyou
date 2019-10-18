package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by lvmen on 2019/9/6
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询品牌
     * @param page 当前页
     * @param rows 每页大小
     * @param sortBy 排序字段
     * @param desc 是否为降序
     * @param key 搜索关键词
     * @return PageResult<Brand> 分页结果
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        /**
         * 通用mapper里面有一个PageHelper,
         * 利用Mybatis的拦截器,对接下来要执行的Sql进行拦截,自动在后面加上limit语句
         */
        // 分页
        PageHelper.startPage(page, rows);
        /**
         * where name like "%x%" or letter == "x" order by id desc
         */
        // 过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            // 过滤条件
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        // 排序
        if (StringUtils.isNotBlank(sortBy)){
                String orderByClause = sortBy + (desc ? " DESC" : " ASC");
                example.setOrderByClause(orderByClause);
        }
        // 查询
        List<Brand> list = brandMapper.selectByExample(example);//Page对象已经把所有的查询出来的都记录下来了,强转不够优雅,使用PageInfo解析翻页结果

        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌
        int count = brandMapper.insert(brand);
        if (count != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        for (Long cid : cids) { // 新增品牌和分类中间表
            brandMapper.insertCategoryBrand(cid,brand.getId());
            if (count != 1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    /**
     * 根据品牌id查询品牌
     * @param id 品牌id
     * @return Brand
     */
    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    /**
     * 根据商品分类ID查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBranByCid(Long cid) {
        /**
         * category和brand做的中间表,brand中没有cid
         * 要想查,就需要多表关联
         */
        List<Brand> list = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据ids集合批量查询品牌
     * @param ids
     * @return
     */
    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    /**
     * 根据品牌id删除品牌
     * @param id
     */
    @Transactional
    public void deleteBrandById(Long id) {
        int count = brandMapper.deleteByPrimaryKey(id);
        if (count != 1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
    }
}
