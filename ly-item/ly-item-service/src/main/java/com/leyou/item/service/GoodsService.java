package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.CartDTO;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lvmen on 2019/9/7
 */
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;


    /**
     * 分页查询SPU
     * @param page 页码
     * @param rows 行数
     * @param saleable 是否上架
     * @param key 搜索关键词
     * @return
     */
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 搜索字段过滤
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%"+ key +"%");
        }
        // 上下架过滤
        if (saleable != null){
            criteria.andEqualTo("saleable", saleable);
        }
        // 默认排序
        example.setOrderByClause("last_update_time DESC");
        // 查询
        List<Spu> spus = spuMapper.selectByExample(example);
        // 判断
        if (CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 解析分类和品牌的名称
        loadCategoryAndBrandName(spus);
        // 解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);
    }


    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail detail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (detail == null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return detail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        // 查询库存
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(ids,skuList);
        return skuList;
    }


    @Transactional
    public void deleteGoodsBySpuId(Long spuId){
        if (spuId == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM);
        }
        //删除spu,把spu中的valid字段设置成false
        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setValid(false);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count == 0) {
            throw new LyException(ExceptionEnum.DELETE_GOODS_ERROR);
        }
        // 发送消息 @TODO

    }

    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 查询sku
        spu.setSkus(querySkuBySpuId(id));
        // 查询detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @Transactional
    public void saveGoods(Spu spu) {
            // 新增spu
            spu.setCreateTime(new Date());
            spu.setLastUpdateTime(spu.getCreateTime());
            spu.setSaleable(true);
            spu.setValid(false);

            int count = spuMapper.insert(spu);
            if (count != 1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

        // 新增sku和stock
        saveSkuAndStock(spu);
        System.out.println("新增sku和stock");

        // 发送mq消息, 商品保存了
        amqpTemplate.convertAndSend("item.insert", spu.getId());

    }

    private void updateGoods(Spu spu){
        if (spu.getId() == 0) {
            throw new LyException(ExceptionEnum.INVALID_PARAM);
        }
        //首先查询sku
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            //删除所有sku
            skuMapper.delete(sku);
            //删除库存
            List<Long> ids = skuList.stream()
                    .map(Sku::getId)
                    .collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //更新数据库  spu  spuDetail
        spu.setLastUpdateTime(new Date());
        //更新spu spuDetail
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count == 0) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count == 0) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //更新sku和stock
        saveSkuAndStock(spu);

        // 发送mq消息 ,商品更新了
        amqpTemplate.convertAndSend("item.update", spu.getId());
    }

    /**
     * 传入spu,新增sku和stock
     * @param spu
     */
    private void saveSkuAndStock(Spu spu) {
        int count;

        // 新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(detail);
        if (count != 1){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_SAVE_ERROR);
        }

        // 定义库存集合
        List<Stock> stockList = new ArrayList<>();
            // 新增sku
            List<Sku> skus = spu.getSkus();
            for (Sku sku : skus) {
                sku.setCreateTime(new Date());
                sku.setLastUpdateTime(sku.getCreateTime());
                sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count != 1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            // 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }

        // 批量新增库存, 实体类主键必须为id
        count = stockMapper.insertList(stockList);
        if (count != 1){
            throw new LyException(ExceptionEnum.STOCK_SAVE_ERROR);
        }
    }


    /**
     * 根据id集合查询sku
     * @param ids
     * @return
     */
    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        loadStockInSku(ids, skus);
        return skus;
    }

    /**
     * 查询库存
     * @param ids
     * @param skus
     */
    private void loadStockInSku(List<Long> ids, List<Sku> skus) {
        // 查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)){
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        // 我们把stock变成一个map,其key是sku的id,值是库存值
        Map<Long, Long> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

    /**
     * 根据购物车集合减库存
     * @param cartDTOs 购物车集合
     */
    @Transactional
    public void decreaseStock(List<CartDTO> cartDTOs) {
        for (CartDTO cartDto : cartDTOs) {
            int count = stockMapper.decreaseStock(cartDto.getSkuId(), cartDto.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
// todo 如果先查询库存, 判断是否充足, 在进行减库存. 使用synchroizer 由于是分布式,会出现分布式事务. 多线程问题
            // todo ZooKeeper 大数据中会用到   我们使用SQL语句级的
        }
    }

    /**
     * 查询商品分类名和品牌名
     * @param spus 商品id集合
     */
    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }

    }
}

/**
 * stream流是未来的重点.  => 响应式编程
 * flux
 * webflux
 */