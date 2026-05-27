package com.rabbiter.ol.vo;

public class ClassNoticeVo {

    private Integer id;
    private Integer classId;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer senderId;
    private Integer isPinned;
    private Integer userId;
    private Integer page;
    private Integer pageSize;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getNoticeType() { return noticeType; }
    public void setNoticeType(Integer noticeType) { this.noticeType = noticeType; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getIsPinned() { return isPinned; }
    public void setIsPinned(Integer isPinned) { this.isPinned = isPinned; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
