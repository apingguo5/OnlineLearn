# 题库管理功能整合与作业管理模块重构

## 概述

本次重构将独立的题库管理功能整合到作业与考试管理模块中，作为二级标签页显示，优化了教师端界面结构，提升了功能模块的内聚性。

## 功能修改明细

### 1. 前端页面整合

**文件**：`frontend/src/views/teacher/TeacherAssessment.vue`

**主要变更**：

#### 1.1 新增题库管理标签页

在"发布任务"和"组卷管理"标签页基础上，新增第三个标签页"题库管理"：

- **筛选栏**：
  - 课程下拉选择器
  - 题型下拉选择器（单选、多选、判断、填空、主观题）
  - 查询与重置按钮

- **题目列表表格**：
  - 题目内容列（支持超长文本溢出显示）
  - 题型标签列（带颜色区分）
  - 所属课程列
  - 分值列
  - 创建时间列
  - 操作列（编辑、删除）

- **分页组件**：支持自定义每页条数、跳转指定页

#### 1.2 录入/编辑题目对话框

新增完整的题目录入与编辑功能：

- **所属课程选择**：必填，下拉选择
- **题型选择**：单选按钮组（5种题型）
- **题目内容**：多行文本输入
- **选项编辑**（单选/多选题显示）：
  - 动态添加/删除选项
  - 选项文本输入
  - 选项标签自动生成（A/B/C/D/E/F）
- **答案输入**：
  - 判断题：对错单选
  - 单选题：选项字母单选
  - 多选题：选项字母多选
  - 填空/主观题：文本输入
- **分值设置**：数字输入框
- **答案解析**：可选，多行文本

#### 1.3 相关数据与方法

新增数据字段：
```javascript
qbLoading: false,        // 加载状态
questionList: [],        // 题目列表
qbTotal: 0,              // 总数
qbPage: 1, qbLimit: 10,  // 分页参数
qbFilter: { courseId: '', questionType: '' },  // 筛选条件
showQbDialog: false,     // 对话框显示
isQbEdit: false,         // 是否编辑模式
editQbId: null,          // 编辑的题目ID
qbSaving: false,         // 保存中状态
qbForm: {                // 表单数据
    questionType: '', stem: '', courseId: '', 
    score: 5, analysis: '', options: []
},
qbAnswerList: []         // 多选题答案数组
```

新增方法：
- `loadQuestions()` - 加载题库列表（支持筛选和分页）
- `editQuestion(row)` - 编辑题目（支持题型转换和选项JSON解析）
- `deleteQuestion(row)` - 删除题目（带确认框）
- `saveQuestion()` - 保存题目（含多选答案处理）
- `addQbOption()` - 添加选项（最多6个）
- `resetQbForm()` - 重置表单

#### 1.4 新增样式

```css
/* 题库管理对话框样式 */
.options-editor { 
    border: 1px solid #e4e7ed; 
    border-radius: 4px; 
    padding: 12px; 
    background: #fafafa; 
}
.option-row { 
    display: flex; 
    align-items: center; 
    gap: 8px; 
    margin-bottom: 8px; 
}
.opt-label { 
    font-weight: bold; 
    width: 20px; 
}
.opt-hint { 
    margin-left: 12px; 
    font-size: 12px; 
    color: #909399; 
}

/* 其他样式 */
.filter-card { 
    margin-bottom: 16px; 
    border-radius: 8px; 
}
.main-card { 
    border-radius: 8px; 
    margin-bottom: 16px; 
}
```

#### 1.5 Tab切换监听

新增Vuex watch监听标签页切换：
```javascript
watch: {
    activeTab(val) {
        if (val === 'questionbank' && this.questionList.length === 0) {
            this.loadQuestions()
        }
    }
}
```
切换到题库管理标签时自动加载题目列表。

### 2. 后端支持

本次修改依赖已有的后端接口，无需额外修改：

**文件**：`backend/src/main/java/com/rabbiter/ol/controller/QuestionController.java`

支持的接口：
- `POST /study/exam/question/list` - 查询题目列表
- `POST /study/exam/question/save` - 新增题目
- `POST /study/exam/question/update` - 更新题目
- `POST /study/exam/question/delete` - 删除题目

### 3. 路由配置更新

**文件**：`frontend/src/router/index.js`

- **删除**：独立的`/teacherquestionbank`路由配置
- **移除**：`TeacherQuestionBank.vue`组件导入

### 4. 题型转换机制

后端使用整数存储题型（1-5），前端使用字符串标识：

| 前端值 | 后端值 | 题型说明 |
|--------|--------|----------|
| single | 1 | 单选题 |
| multiple | 2 | 多选题 |
| judge | 3 | 判断题 |
| fill | 4 | 填空题 |
| essay | 5 | 主观题 |

**转换代码**：
```javascript
const TYPE_REVERSE = { single: 1, multiple: 2, judge: 3, fill: 4, essay: 5 }

// 保存时转换
const questionTypeNum = TYPE_REVERSE[this.qbForm.questionType]

// 编辑时转换
const typeStr = typeof row.questionType === 'number' 
    ? this.numToType(row.questionType) 
    : row.questionType
```

### 5. 选项JSON存储机制

题目选项以JSON格式存储在数据库`options`字段：

**存储格式**：
```json
[{"text":"选项A内容"},{"text":"选项B内容"},...]
```

**前端处理**：
- 保存时：将包含label和text的选项数组转换为只含text的JSON字符串
- 加载时：解析JSON并重新添加label标签（A/B/C...）

### 6. 作业再次提交功能

题库管理整合的同时，也完善了作业再次提交功能：

**后端修改**：`UserDoHomeworkController.save()`
- 检查学生是否已提交过该作业
- 已提交则更新记录（重置状态、清空前次分数）
- 未提交则创建新记录

**前端修改**：`TeacherAssessment.vue`
- 发布任务表格新增"允许重交"开关列
- `toggleResubmit()`方法处理开关切换

## 数据模型变更

### 数据库字段新增

**表**：`homework`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| allow_resubmit | TINYINT(1) | 0 | 是否允许再次提交（0=不允许，1=允许） |

**需手动执行SQL**：
```sql
ALTER TABLE homework ADD COLUMN allow_resubmit TINYINT(1) DEFAULT 0 COMMENT '是否允许再次提交：0-不允许，1-允许';
```

### MyBatis映射更新

**文件**：`backend/src/main/resources/mapper/HomeworkDao.xml`
- resultMap新增`allowResubmit`属性映射
- baseQuery查询字段中新增该列

## 界面结构优化

### 整合前

```
教师端功能菜单
├── 发布任务  →  TeacherAssessment.vue (tab1)
├── 组卷管理  →  TeacherExamPaper.vue
└── 题库管理  →  TeacherQuestionBank.vue (独立页面)
```

### 整合后

```
教师端功能菜单
└── 作业与考试  →  TeacherAssessment.vue（三标签合一）
    ├── 发布任务
    ├── 组卷管理
    └── 题库管理 ← 新整合
```

**优势**：
1. 功能内聚性增强，相关功能集中管理
2. 减少页面跳转，提升操作流畅度
3. 统一样式和交互规范
4. 便于后续功能扩展

## 功能验证清单

- [x] 题库标签页切换正常，数据自动加载
- [x] 课程筛选功能正常
- [x] 题型筛选功能正常（5种题型）
- [x] 题目列表分页正常
- [x] 新增单选题正常
- [x] 新增多选题正常
- [x] 新增判断题正常
- [x] 新增填空题正常
- [x] 新增主观题正常
- [x] 编辑题目功能正常（含题型、选项、答案转换）
- [x] 删除题目功能正常（含二次确认）
- [x] 选项动态增删正常
- [x] 答案根据题型动态显示
- [x] 分页查询正常
- [x] 允许重交开关正常
- [x] 学生端再次提交按钮显示正常

## 遗留文件说明

原独立题库管理页面文件`frontend/src/views/teacher/TeacherQuestionBank.vue`仍保留在代码库中，作为功能参考备份，可在后续版本中安全删除。

## 涉及文件清单

| 序号 | 文件路径 | 修改类型 | 说明 |
|------|----------|----------|------|
| 1 | `frontend/src/views/teacher/TeacherAssessment.vue` | 修改 | 新增题库管理标签页及相关功能 |
| 2 | `frontend/src/router/index.js` | 修改 | 删除独立题库路由 |
| 3 | `backend/src/main/java/com/rabbiter/ol/controller/UserDoHomeworkController.java` | 修改 | 支持再次提交逻辑 |
| 4 | `backend/src/main/java/com/rabbiter/ol/entity/HomeworkEntity.java` | 修改 | 新增allowResubmit字段 |
| 5 | `backend/src/main/resources/mapper/HomeworkDao.xml` | 修改 | 新增字段映射 |
| 6 | `backend/src/main/resources/mapper/UserDoHomeworkDao.xml` | 修改 | 新增字段查询 |

---

**文档创建时间**：2024-05-20
**修改模块**：教师端-作业与考试管理
**版本**：v1.0
