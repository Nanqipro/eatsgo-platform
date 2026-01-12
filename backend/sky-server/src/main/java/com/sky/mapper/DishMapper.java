package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 根据分类id查询套餐的数量
 */
@Mapper
public interface DishMapper {
    @Select("select count(id) from dish where category_id = #{id}")
    Integer countByCatagoryId(Long id);
}
