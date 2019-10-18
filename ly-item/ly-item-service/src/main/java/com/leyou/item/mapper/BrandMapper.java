package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by lvmen on 2019/9/6
 */
public interface BrandMapper extends BaseMapper<Brand> {

    /**
     * 新增商品分类和品牌中间表数据
     * @param cid 分类ID
     * @param bid 品牌ID
     * @return
     */
    @Insert("insert into tb_category_brand values (#{cid}, #{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    /**
     * 根据分类id查询品牌列表
     * @param cid
     * @return
     */
    @Select("SELECT b.*\n" +
            "FROM tb_brand b\n" +
            "INNER JOIN tb_category_brand cb ON b.id = cb.brand_id\n" +
            "WHERE cb.category_id = #{cid}")
    List<Brand> queryByCategoryId(@Param("cid")Long cid);

}
