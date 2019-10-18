package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * 商品规格组
 * Created by lvmen on 2019/9/7
 */
@Data
@Table(name = "tb_spec_group")
public class SpecGroup {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id; // 主键id

    private Long cid; // 商品分类id

    private String name; // 规格组名

    @Transient
    private List<SpecParam> params; // 规格参数
}
