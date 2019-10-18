package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 品牌表 实体类
 * Created by lvmen on 2019/9/6
 */
@Table(name = "tb_brand")
@Data
public class Brand {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private String name; // 品牌名称
    private String image; // 品牌图片
    private Character letter; // 首字母
}