package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lvmen on 2019/9/7
 */
@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;

    /**
     * 根据商品分类id查询商品规格组
     * @param cid 商品分类id
     * @return
     */
    public List<SpecGroup> queryGroupByCid(Long cid) {
        // 查询条件
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        // 查询
        List<SpecGroup> list = groupMapper.select(group);
        if (CollectionUtils.isEmpty(list)){
            // 没查到, 抛出一个异常
            throw new LyException(ExceptionEnum.GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = paramMapper.select(param);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.PARAM_NOT_FOUND);
        }
        return list;
    }

    /**
     * 新增规格组
     * @param specGroup
     */
    @Transactional
    public void saveGroup(SpecGroup specGroup) {
        int count = groupMapper.insert(specGroup);
        if (count != 1){
            throw new LyException(ExceptionEnum.GROUP_CREATE_ERROR);
        }
    }

    /**
     * 新增规格参数
     * @param specParam
     */
    @Transactional
    public void saveParam(SpecParam specParam) {
        int count = paramMapper.insert(specParam);
        if (count != 1){
            throw new LyException(ExceptionEnum.PARAM_SAVE_ERROR);
        }

    }

    @Transactional
    public void updateParam(SpecParam specParam) {
        int count = paramMapper.updateByPrimaryKey(specParam);
        if (count != 1){
            throw new LyException(ExceptionEnum.PARAM_UPDATE_ERROR);
        }
    }

    @Transactional
    public void deleteParam(Long id) {
        int count = paramMapper.deleteByPrimaryKey(id);
        if (count != 1){
            throw new LyException(ExceptionEnum.PARAM_DELETE_ERROR);
        }
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        // 查询当前分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        // 先把规格参数变成map,map的key是规格组id,map的值时组下的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam param : specParams) {
            if (!map.containsKey(param.getGroupId())){
                // 这个组id在map中不存在,新增一个list   [算法优化]
                map.put(param.getGroupId(), new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        // 填充param到group
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
