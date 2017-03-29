package com.lee.demo.upload.controller;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * 图片上传控制器，实现图片上传、回显、单独获取
 *
 * Created by fernando on 3/28/17.
 */
@Controller
public class ImageUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploadController.class);

    // 图片存放目录
    private static final String IMAGES_PATH = "/images";

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * 上传图片页面
     */
    @GetMapping(value = "/image")
    public String image() {
        return "image";
    }

    /**
     * 上传图片并回显（返回图片url，并通过thymeleaf渲染）
     */
    @PostMapping(value = "/upload")
    @ResponseBody
    public ModelAndView uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("image");  // 上传图片页面
        if (!file.isEmpty()) {
            try {

                String fileName = file.getOriginalFilename();
                LOGGER.info("fileName: {}", fileName);

                // 物理存放地址
                String imagePath = request.getSession().getServletContext().getRealPath(IMAGES_PATH);
                LOGGER.info("imagePath = {}", imagePath);

                File dest = new File(imagePath, fileName);
                // 检测是否存在目录，不存在则创建
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                file.transferTo(dest);

                String imageUrl = request.getContextPath() + IMAGES_PATH + File.separatorChar + fileName;
                LOGGER.info("imageUrl = {}", imageUrl);
                modelAndView
                    .addObject("imageUrl", imageUrl);  // 返回图片url，thymeleaf中通过th:src接收
            } catch (Exception e) {
                LOGGER.error("image upload fail", e);
            }
        } else {
            LOGGER.warn("image empty");
        }
        return modelAndView;
    }

    /**
     * 单独显示图片（直接返回图片内容）
     * 注意@GetMapping的value写法，因为图片有后缀名，需要特殊处理
     */
    @GetMapping(value = "/image/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<?> getImage(@PathVariable String fileName, HttpServletRequest request) {
        LOGGER.info("show image, fileName = " + fileName);
        try {
            String imagePath = request.getSession().getServletContext().getRealPath(IMAGES_PATH);
            return ResponseEntity.ok(resourceLoader.getResource("file:" + imagePath + File.separatorChar + fileName));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
