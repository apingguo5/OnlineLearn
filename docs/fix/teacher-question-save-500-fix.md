# 教师端录入题目保存失败（500 Internal Server Error）修复

## 问题描述

教师端录入题目时，填写完表单点击"保存"按钮，请求返回 HTTP 500 Internal Server Error，导致题目无法保存。

## 根因分析

### 1. 数据库 `question` 表 `options` 列类型与数据类型不匹配

**SQL 定义（修复前）：**
```sql
`options` json DEFAULT NULL COMMENT '选项（JSON数组）',
```

`options` 列定义为 MySQL `json` 类型。MySQL 的 JSON 类型对写入的数据有严格的校验，只接受合法的 JSON 文档（必须是 `[ ]` 或 `{ }` 包裹的 JSON 值）。

**前端传值：**
```javascript
options: JSON.stringify(this.form.options)
// 例如: '[{"label":"A","text":"答案A"},{"label":"B","text":"答案B"}]'
```

前端对 `options` 进行了 `JSON.stringify` 后再传给后端。`JSON.stringify` 将 JavaScript 对象转为 **字符串**。

**后端 `QuestionVo.java` 接收：**
```java
private Object options;
```

**后端 `QuestionServiceImpl.save()`：**
```java
entity.setOptions(questionVo.getOptions());
```

由于 `options` 在 Vo 中定义为 `Object` 类型，Spring MVC 反序列化请求 JSON 时，会把已经 `JSON.stringify` 过的 `options` 字段（字符串）直接映射为 `String` 对象。

**`QuestionDao.xml` 中的 insert 语句：**
```xml
insert into question (... , `options`, ...) values (..., #{options}, ...)
```

MyBatis 在处理 `#{options}` 时，如果 Java 类型是 `String`，会将其作为普通字符串插入。当 MySQL 列类型为 `json` 时，直接插入一个 **被引号包裹的字符串**（例如 `'"[{\"label\":\"A\",\"text\":\"答案A\"}]"'`）会因为双引号嵌套导致 JSON 解析失败，触发 500 错误。

### 2. 前端 `courseId` 数据类型不匹配

`TeacherQuestionBank.vue` 中 `el-select` 的 `:value="c.id"` 绑定的是数字类型，但 `v-model="form.courseId"` 在某些 Element UI 版本下会绑定为字符串。后端 `QuestionVo.courseId` 定义为 `Integer`，如果 Spring 类型转换失败会导致参数绑定异常。

### 3. 后端 `QuestionEntity` 未配置 JSON 类型处理器

`QuestionEntity.java` 中 `options` 字段定义为 `Object`，但 MyBatis 没有为 `options` 列配置 `typeHandler` 来处理 JSON 类型与 Java 对象的映射，导致列类型转型错误。

## 修复方案

### 修复 1：数据库 `options` 列类型改为 `text`

将 `options` 列从 `json` 改为 `text`，避免 MySQL JSON 类型对数据的严格校验：

```sql
ALTER TABLE `question` MODIFY COLUMN `options` text DEFAULT NULL COMMENT '选项（JSON数组）';
```

### 修复 2：前端 `courseId` 强制转为数字

在 `saveQuestion()` 方法中，对 `courseId` 使用 `parseInt()` 确保传递数字类型：

```javascript
courseId: parseInt(this.form.courseId, 10),
```

### 修复 3：后端 `QuestionEntity` 移除 `@TableField(typeHandler = JacksonTypeHandler.class)`

删除了 `JacksonTypeHandler` 注解，MyBatis 将 `options` 按普通 `Object` 类型处理，写入 text 列时自动调用 `toString()`，读取时返回字符串。

### 完整数据流（修复后）

1. **前端 `TeacherQuestionBank.vue`**：`options` 先通过 `JSON.stringify(this.form.options)` 转为字符串
2. **HTTP 请求体**：`options` 字段值为 JSON 字符串（例如 `"[{\"label\":\"A\",...}]"`）
3. **Spring MVC 反序列化**：`QuestionVo.options`（Object 类型）接收到该字符串
4. **`QuestionServiceImpl.save()`**：`entity.setOptions(vo.getOptions())` 传递 String 到实体
5. **MyBatis insert**：`#{options}` 将 String 写入 text 列，无类型转换问题
6. **MyBatis select**：从 text 列读取 String，赋值给 `QuestionEntity.options`（Object）
7. **`ExamPaperServiceImpl`**：读取时使用 `JSON.parse(JSON.toJSONString(q.getOptions()))` 兼容 String 类型