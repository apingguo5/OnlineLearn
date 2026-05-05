package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.ol.dao.UserDao;
import com.rabbiter.ol.entity.UserEntity;
import com.rabbiter.ol.entity.UserRoleEntity;
import com.rabbiter.ol.service.UserRoleService;
import com.rabbiter.ol.service.UserService;
import com.rabbiter.ol.tool.MD5Util;
import com.rabbiter.ol.vo.LoginVo;
import com.rabbiter.ol.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleService userRoleService;

    @Override
    public Map<String, Object> queryPage(UserVo userVo) {
        Integer total = userDao.queryCount(userVo);
        List<HashMap> data = userDao.queryData(userVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total",total);
        result.put("data",data);
        return result;
    }

    @Override
    public List<HashMap> login(LoginVo loginVo) {
        List<HashMap> users = userDao.login(loginVo);
        if (users != null && users.size() > 0) {
            HashMap user = users.get(0);
            String storedPassword = (String) user.get("password");

            // 兼容两种密码存储方式：
            // 1. MD5加密存储（新注册用户）
            // 2. 明文存储（旧用户）
            boolean passwordMatched = false;

            // 判断是否为MD5哈希（32位十六进制字符串）
            if (storedPassword != null && storedPassword.length() == 32 && storedPassword.matches("[0-9a-fA-F]+")) {
                // MD5方式验证
                passwordMatched = MD5Util.verify(loginVo.getPassword(), storedPassword);
            } else {
                // 明文兼容方式验证
                passwordMatched = loginVo.getPassword().equals(storedPassword);
            }

            if (passwordMatched) {
                // 密码正确，移除密码字段再返回
                user.remove("password");

                // 根据用户ID查询 user_role 表获取角色
                // MyBatis对INT UNSIGNED可能返回Long，使用Number安全转换
                Number userIdNum = (Number) user.get("userId");
                Integer userId = userIdNum.intValue();
                List<UserRoleEntity> roleList = userRoleService.list(
                    new QueryWrapper<UserRoleEntity>().eq("user_id", userId)
                );
                if (roleList != null && !roleList.isEmpty()) {
                    // 只取第一个角色
                    user.put("roleId", roleList.get(0).getRoleId());
                }
                return users;
            }
        }
        // 密码错误或用户不存在
        if (users == null) {
            users = new ArrayList<>();
        } else {
            users.clear();
        }
        return users;
    }

    @Override
    public Map<String, Object> findNotDoWork(UserVo userVo) {
        Integer total = userDao.findNotDoWorkCount(userVo);
        List<HashMap> data = userDao.findNotDoWorkData(userVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total",total);
        result.put("data",data);
        return result;
    }

    @Override
    public Map<String, Object> findNotDoHomework(UserVo userVo) {
        Integer total = userDao.findNotDoHomeworkCount(userVo);
        List<HashMap> data = userDao.findNotDoHomeworkData(userVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total",total);
        result.put("data",data);
        return result;
    }

    @Override
    public List<HashMap> findList(UserVo userVo) {
        List<HashMap> data = userDao.findList(userVo);
        return data;
    }

    @Override
    public List<HashMap> findUserInfo(UserEntity userEntity) {
        List<HashMap> data = userDao.findUserInfo(userEntity);
        return data;
    }
}