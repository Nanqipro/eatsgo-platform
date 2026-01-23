package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.netty.handler.codec.MessageAggregationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.MessageConstraintException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 套餐业务实现
 */

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐 同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        // 将套餐表插入数据
        setmealMapper.insert(setmeal);

        // 获取生成的套餐的id
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {setmealDish.setSetmealId(setmealId);});
        // 保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                // 套餐处于起售状态，不能删除
                throw new RuntimeException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        // 批量删除套餐
        ids.forEach(setmealId -> {
            // 删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            // 删除套餐和菜品的关联数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        });


    }

    /**
     * 根据id查询套餐和套餐中的菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;

    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 修改套餐表数据
        setmealMapper.update(setmeal);

        //获取套餐 id
        Long setmealId = setmealDTO.getId();

        // 删除套餐和菜品的关联数据
        setmealDishMapper.deleteBySetmealId(setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        // 重新插入新的套餐和菜品的关联数据
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 起售套餐时，判断当前套餐是否关联了停售中的菜品，如果关联了则无法起售 提示套餐关联了停售中的菜品
        if(status == StatusConstant.ENABLE){
            // select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList =  dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        // 抛出业务异常
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
}
