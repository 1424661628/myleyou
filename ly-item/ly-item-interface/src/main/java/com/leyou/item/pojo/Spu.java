package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * 标准产品单位
 * Created by lvmen on 2019/9/7
 */
@Data
@Table(name = "tb_spu")
public class Spu {  // 可以写一个SpuVO,但是会变得很复杂

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long brandId; // 分类id
    private Long cid1; // 1级类目
    private Long cid2; // 2级类目
    private Long cid3; // 3级类目
    private String title; // 标题
    private String subTitle; // 子标题
    private Boolean saleable; // 是否上架
    @JsonIgnore
    private Boolean valid; // 是否有效，逻辑删除用
    private Date createTime; // 创建时间
    @JsonIgnore // 返回到页面时 忽略
    private Date lastUpdateTime;// 最后修改时间

    @Transient
    private String cname; // 所属分类名字
    @Transient
    private String bname; // 所属品牌名字

    @Transient
    private List<Sku> skus; // SKU

    @Transient
    private SpuDetail spuDetail; // 商品详情
}
