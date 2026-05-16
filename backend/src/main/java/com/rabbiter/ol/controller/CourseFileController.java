package com.rabbiter.ol.controller;

import com.rabbiter.ol.tool.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 课程文件服务控制器
 * 
 * 功能：将 courses/ 目录下的本地文件通过 HTTP 流式提供给前端。
 * 
 * 为什么不用 Spring ResourceHandler？
 * Spring 的 ResourceHandler 内部使用 java.net.URL 构造 file: URI 来定位文件，
 * 而 java.net.URL 不允许路径中包含 [ ] ( ) ？ 等字符（即使文件在磁盘上真实存在），
 * 这会导致 MalformedURLException → URL编码正确但Spring解码后文件路径含[] → file: URI 构造失败 → 404。
 * 
 * 本 Controller 直接使用 java.io.File / java.nio.file.Path 操作文件系统，
 * 绕过了 java.net.URL 对特殊字符的限制，确保文件名含方括号等字符时也能正常读取。
 * 
 * 同时支持 HTTP Range 请求（即视频快进/拖动进度条），浏览器请求视频时通常会先发一个
 * Range 头请求部分数据，本控制器正确响应 206 Partial Content。
 * 
 * 根路径定位逻辑：
 * - 使用 PathUtils.getClassLoadRootPath() 获取 classpath 根路径
 * - 如果是 backend 目录则取父目录（项目根目录）
 * - 最终 courses/ 目录 = 项目根目录/courses
 * - 与 CourseScannerService 一致
 */
@Controller
public class CourseFileController {

    private static final Logger log = LoggerFactory.getLogger(CourseFileController.class);

    /**
     * 课程文件存储的本地根目录
     * 与 CourseScannerService 使用相同的路径定位逻辑
     */
    private static final String COURSES_ROOT;

    static {
        String rootPath = PathUtils.getClassLoadRootPath();
        File rootDir = new File(rootPath);
        // 如果 rootDir 是 backend 目录，取父目录（项目根目录）
        if ("backend".equals(rootDir.getName())) {
            rootDir = rootDir.getParentFile();
        }
        COURSES_ROOT = new File(rootDir, "courses").getAbsolutePath();
        log.info("[CourseFileController] courses root path: {}", COURSES_ROOT);
    }

    /**
     * 处理所有 /courses/** 的文件请求。
     * 例如：/courses/Android/Unit%201.../...%5BlNKk-RSL7wg%5D.webm
     */
    @GetMapping("/courses/**")
    public void serveCourseFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 获取完整请求 URI 并提取 /courses/ 之后的路径
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 从请求路径中移除 contextPath 和 /courses/ 前缀
        String relativePath = requestUri.substring(
            (contextPath + "/courses/").length()
        );

        // 2. URL 解码路径（因为浏览器发送的是编码后的路径）
        String decodedPath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);

        // 3. 构建本地文件路径并规范化（防止路径穿越）
        Path filePath = Paths.get(COURSES_ROOT, decodedPath).normalize();
        String normalizedRoot = Paths.get(COURSES_ROOT).normalize().toString();
        if (!filePath.toAbsolutePath().toString().startsWith(normalizedRoot)) {
            log.warn("路径穿越尝试: {}", filePath);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 4. 检查文件是否存在
        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            log.warn("课程文件不存在: {} (decoded: {}, resolved: {})",
                requestUri, decodedPath, filePath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = file.length();
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // 5. 处理 Range 请求（视频快进/拖动进度条）
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader == null) {
            // 无 Range 头 → 返回完整文件
            response.setContentType(contentType);
            response.setContentLengthLong(fileLength);
            try (InputStream is = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            log.debug("完整文件: {} ({} bytes, {})", filePath.getFileName(), fileLength, contentType);
        } else {
            // 有 Range 头 → 解析并返回 206 Partial Content
            String[] ranges = rangeHeader.replace("bytes=", "").split(",");
            // 只处理第一个 range
            String range = ranges[0].trim();

            long start, end;
            if (range.startsWith("-")) {
                // 后缀范围: -500 → 最后500字节
                long suffix = Long.parseLong(range.substring(1));
                start = Math.max(0, fileLength - suffix);
                end = fileLength - 1;
            } else if (range.contains("-")) {
                // 标准范围: 100-200 或 100-
                String[] parts = range.split("-");
                start = Long.parseLong(parts[0]);
                end = parts.length > 1 && !parts[1].isEmpty()
                    ? Long.parseLong(parts[1])
                    : fileLength - 1;
                if (start >= fileLength) {
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.setHeader("Content-Range", "bytes */" + fileLength);
                    return;
                }
                if (end >= fileLength) {
                    end = fileLength - 1;
                }
            } else {
                // 无效 range
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            long contentLength = end - start + 1;

            // 设置 206 响应头
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setContentType(contentType);
            response.setContentLengthLong(contentLength);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);

            // 写出指定范围的数据
            try (RandomAccessFile raf = new RandomAccessFile(file, "r");
                 OutputStream os = response.getOutputStream()) {
                raf.seek(start);
                byte[] buffer = new byte[8192];
                long remaining = contentLength;
                int bytesRead;
                while (remaining > 0
                    && (bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    os.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                os.flush();
            }
            log.debug("部分内容: {} (bytes {}-{}/{}, {})",
                filePath.getFileName(), start, end, fileLength, contentType);
        }
    }
}