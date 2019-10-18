package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by lvmen on 2019/9/5
 */
public interface CategoryMapper extends Mapper<Category> , IdListMapper<Category, Long> {

    /**
     * 根据品牌id查询商品分类
     * @param bid
     * @return
     */
    @Select("select * from tb_category where id in (select category_id from tb_category_brand where brand_id = #{bid})")
    List<Category> queryByBrandId(Long bid);
}
