package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by lvmen on 2019/9/7
 */
@Data
@Table(name="tb_spu_detail")
public class SpuDetail {
    @Id
    private Long spuId; // 对应的SPU的id
    private String description; // 商品描述
    private String genericSpec; // 通用规格
    private String specialSpec; // 特殊规格参数
    private String packingList; // 包装清单
    private String afterService; // 售后服务
}
