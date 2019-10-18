package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.Transient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lvmen on 2019/9/6
 */
@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    @Autowired
    private UploadProperties prop;

    //private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");//允许的图片类型

    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private ThumbImageConfig thumbImageConfig;


    /**
     * 图片上传到本地
     * @param file
     * @return
     */
    public String uploadImage2(MultipartFile file) {
        try {
            // 校验文件类型
            String contentType = file.getContentType();
            if (!prop.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 准备目标路径
            // this.getClass().getClassLoader().getResource("").getFile();
            File dest = new File("F:\\暑期就业\\_99黑马基础\\31-36主流框架\\乐优商城课件\\upload", file.getOriginalFilename());
            // 保存文件到本地
            file.transferTo(dest);
            //返回路径
            String url = "http://image.leyou.com/upload" + file.getOriginalFilename();
            return url;

        } catch (IOException e) {
            // 上传失败
            log.error("[文件上传] 上传文件失败!", e);//记录日志
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
    /**
     * 图片上传到服务器
     * @param file
     * @return 图片路径
     */
//    @Transactional
    public String uploadImage(MultipartFile file) {
        try {
            // 1. 校验文件类型
            String contentType = file.getContentType();
            if (!prop.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2. 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
           // 3. 上传到FastDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
//            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            StorePath storePath = storageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(), extension, null);

            System.out.println("缩略图" + thumbImageConfig.getThumbImagePath(storePath.getPath()));
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            // 上传失败
            log.error("[文件上传] 上传文件失败!", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
}
