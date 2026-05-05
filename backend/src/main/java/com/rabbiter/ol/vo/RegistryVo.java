package com.rabbiter.ol.vo;

/**
 * 注册参数VO
 * @author 
 * @email ${email}
 * @date 2024-02-12 00:24:20
 */
public class RegistryVo {

    /**
     * 账号
     */
    private String account;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 电话
     */
    private String phone;
    /**
     * 性别（1：男；2：女）
     */
    private Integer sex;

    /**
     * 角色ID（2：教师，3：学生）
     */
    private Integer roleId;

    /**
     * 班级ID
     */
    private Integer classId;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }
}