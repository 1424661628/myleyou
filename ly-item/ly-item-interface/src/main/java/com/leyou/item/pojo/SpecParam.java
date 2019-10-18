package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 商品规格参数
 * Created by lvmen on 2019/9/7
 */
@Data
@Table(name = "tb_spec_param")
public class SpecParam {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id; // 主键id
    private Long cid;  // 商品分类id
    private Long groupId; // 规格组id
    private String name;
    @Column(name = "`numeric`") // 避免产生歧义,转义字符 `xxx`
    private Boolean numeric;
    private String unit; // 单位 - 只有numeric为true时有值
    private Boolean generic; // 是否通用
    private Boolean searching; // 是否是搜索字段
    private String segments; // 搜索段
}
