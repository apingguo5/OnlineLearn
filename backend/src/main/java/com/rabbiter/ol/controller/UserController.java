package com.rabbiter.ol.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.UserClassEntity;
import com.rabbiter.ol.entity.UserEntity;
import com.rabbiter.ol.entity.UserRoleEntity;
import com.rabbiter.ol.service.UserClassService;
import com.rabbiter.ol.service.UserRoleService;
import com.rabbiter.ol.service.UserService;
import com.rabbiter.ol.tool.MD5Util;
import com.rabbiter.ol.vo.LoginVo;
import com.rabbiter.ol.vo.RegistryVo;
import com.rabbiter.ol.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author 
 * @email ${email}
 * @date 2024-02-12 00:24:20
 */
@RestController
@RequestMapping("study/user")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserClassService userClassService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(@RequestBody UserVo userVo) {
        userVo.setPage((userVo.getPage() - 1) * userVo.getPageSize());
        Map<String, Object> page = userService.queryPage(userVo);
        return Result.success(page);
    }

    /**
     * 列表
     */
    @RequestMapping("/findList")
    public Result findList(@RequestBody UserVo userVo) {
        List<HashMap> page = userService.findList(userVo);
        return Result.success(page);
    }

    /**
     * 列表
     */
    @RequestMapping("/findNotDoWork")
    public Result findNotDoWork(@RequestBody UserVo userVo) {
        userVo.setPage((userVo.getPage() - 1) * userVo.getPageSize());
        Map<String, Object> page = userService.findNotDoWork(userVo);
        return Result.success(page);
    }

    /**
     * 列表
     */
    @RequestMapping("/findNotDoHomework")
    public Result findNotDoHomework(@RequestBody UserVo userVo) {
        userVo.setPage((userVo.getPage() - 1) * userVo.getPageSize());
        Map<String, Object> page = userService.findNotDoHomework(userVo);
        return Result.success(page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info")
    public Result info(@RequestBody UserEntity userEntity) throws ParseException {
        List<HashMap> userInfo = userService.findUserInfo(userEntity);
        if (userInfo != null && !userInfo.isEmpty()) {
            return Result.success(userInfo.get(0));
        } else {
            UserEntity user = userService.getById(userEntity.getId());
            return Result.success(user);
        }
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody UserVo user) {
        if(userService.getBaseMapper().selectCount(new QueryWrapper<UserEntity>().eq("account", user.getAccount())) != 0) {
            // 账号重复
            return Result.failureCode();
        }

        // 密码加密存储
        if (user.getUserEntity().getPassword() != null && !user.getUserEntity().getPassword().isEmpty()) {
            user.getUserEntity().setPassword(MD5Util.encrypt(user.getUserEntity().getPassword()));
        }
        user.getUserEntity().setCreateTime(new Date(System.currentTimeMillis()));
        boolean save = userService.save(user.getUserEntity());

        //新增角色
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRoleId(user.getRoleId());
        userRoleEntity.setUserId(user.getUserEntity().getId());
        userRoleService.save(userRoleEntity);

        //添加学生与班级关系  学生和班级一对一
        if (user.getClassId() != null) {
            UserClassEntity userClassEntity = new UserClassEntity();
            userClassEntity.setClassId(user.getClassId());
            userClassEntity.setUserId(user.getUserEntity().getId());
            userClassService.save(userClassEntity);
        }

        if (save) {
            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 用户注册 - 支持选择角色和班级
     */
    @RequestMapping("/registry")
    public Result registry(@RequestBody RegistryVo registryVo) {
        // 参数校验
        if (registryVo.getAccount() == null || registryVo.getAccount().trim().isEmpty()) {
            return Result.failure("账号不能为空");
        }
        if (registryVo.getPassword() == null || registryVo.getPassword().trim().isEmpty()) {
            return Result.failure("密码不能为空");
        }
        if (registryVo.getUserName() == null || registryVo.getUserName().trim().isEmpty()) {
            return Result.failure("姓名不能为空");
        }

        // 检查账号重复
        if(userService.getBaseMapper().selectCount(new QueryWrapper<UserEntity>().eq("account", registryVo.getAccount())) != 0) {
            return Result.failure("注册失败，账号已存在");
        }

        // 密码加密后存储
        UserEntity user = new UserEntity();
        user.setAccount(registryVo.getAccount());
        user.setPassword(MD5Util.encrypt(registryVo.getPassword()));
        user.setUserName(registryVo.getUserName());
        user.setPhone(registryVo.getPhone());
        user.setSex(registryVo.getSex());
        user.setCreateTime(new Date(System.currentTimeMillis()));
        boolean save = userService.save(user);
        if (!save) {
            return Result.failure("注册失败");
        }

        // 写入角色关联（默认学生3，若前端传了roleId则使用自定义角色）
        Integer roleId = registryVo.getRoleId();
        if (roleId == null) {
            roleId = 3; // 默认学生
        }
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUserId(user.getId());
        userRoleEntity.setRoleId(roleId);
        userRoleService.save(userRoleEntity);

        // 写入班级关联（学生选班或教师归属班级）
        if (registryVo.getClassId() != null) {
            UserClassEntity userClassEntity = new UserClassEntity();
            userClassEntity.setClassId(registryVo.getClassId());
            userClassEntity.setUserId(user.getId());
            userClassService.save(userClassEntity);
        }

        return Result.successCode();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody UserVo userVo) {
        boolean b;
        if (userVo.getUserEntity() == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(userVo.getId());
            userEntity.setUserName(userVo.getUserName());
            userEntity.setSex(userVo.getSex());
            userEntity.setPhone(userVo.getPhone());
            b = userService.updateById(userEntity);
        } else {
            b = userService.updateById(userVo.getUserEntity());
        }

        if (b) {
            if (userVo.getClassId() != null) {
                //根据老师ID删除老师所对应的班级并重新新增
                QueryWrapper<UserClassEntity> userClassQueryWrapper = new QueryWrapper<>();
                if (userVo.getUserEntity() != null) {
                    userClassQueryWrapper.eq("user_id", userVo.getUserEntity().getId());
                } else {
                    userClassQueryWrapper.eq("user_id", userVo.getId());
                }
                userClassService.remove(userClassQueryWrapper);

                UserClassEntity userClassEntity = new UserClassEntity();
                userClassEntity.setClassId(userVo.getClassId());
                if (userVo.getUserEntity() != null) {
                    userClassEntity.setUserId(userVo.getUserEntity().getId());
                } else {
                    userClassEntity.setUserId(userVo.getId());
                }
                userClassService.save(userClassEntity);
            }


            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody UserEntity user) {
        //删除用户信息
        userService.removeById(user.getId());

        //删除对应角色
        QueryWrapper<UserRoleEntity> userRoleQueryWrapper = new QueryWrapper<>();
        userRoleQueryWrapper.eq("user_id", user.getId());
        userRoleService.remove(userRoleQueryWrapper);

        //删除对应班级
        QueryWrapper<UserClassEntity> userClassQueryWrapper = new QueryWrapper<>();
        userClassQueryWrapper.eq("user_id", user.getId());
        userClassService.remove(userClassQueryWrapper);

        return Result.successCode();
    }


    /**
     * 登录
     */
    @RequestMapping("/login")
    public Result login(@RequestBody LoginVo loginVo) {
        // 参数校验
        if (loginVo.getAccount() == null || loginVo.getAccount().trim().isEmpty()) {
            return Result.failure("账号不能为空");
        }
        if (loginVo.getPassword() == null || loginVo.getPassword().trim().isEmpty()) {
            return Result.failure("密码不能为空");
        }

        List<HashMap> users = userService.login(loginVo);
        if (users.size() < 1) {
            return Result.failure("账号或密码错误");
        }

        return Result.success(users.get(0));
    }

    /**
     * 修改密码
     */
    @RequestMapping("/updatePassword")
    public Result updatePassword(@RequestBody UserVo userVo) {
        UserEntity byId = userService.getById(userVo.getId());
        if (byId == null) {
            return Result.failure("用户不存在");
        }
        // 验证旧密码（明文转MD5后与数据库中加密密码比较）
        if (MD5Util.verify(userVo.getPassword(), byId.getPassword())){
            UserEntity userEntity = new UserEntity();
            userEntity.setId(userVo.getId());
            userEntity.setPassword(MD5Util.encrypt(userVo.getNewPassword()));
            boolean updateById = userService.updateById(userEntity);
            if (updateById){
                return Result.successCode();
            }
            return Result.failure("密码修改失败");
        }
        return Result.failure("原密码错误");
    }

}
