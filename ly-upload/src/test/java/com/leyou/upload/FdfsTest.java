package com.leyou.upload;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.LyUploadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by lvmen on 2019/9/7
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LyUploadService.class)
public class FdfsTest {

    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private ThumbImageConfig thumbImageConfig;

    // 图片上传
    @Test
    public void testUpload() throws FileNotFoundException {
        File file = new File("D:\\image\\2.jpg");
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadFile(
                new FileInputStream(file), file.length(), "jpg", null);
        // 带分组的路径
        System.out.println(storePath.getFullPath());
        // 不带分组的路径
        System.out.println(storePath.getPath());
    }
    //group1/M00/00/00/wKijg11_nUCAKRyoAAGfdb76emI944.jpg
    //M00/00/00/wKijg11_nUCAKRyoAAGfdb76emI944.jpg

    // 图片上传 带有缩略图
    @Test
    public void testUploadAndCreateThumb() throws FileNotFoundException {
        File file = new File("D:\\image\\2.jpg");
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                new FileInputStream(file), file.length(), "jpg", null);
        // 带分组的路径
        System.out.println(storePath.getFullPath());
        // 不带分组的路径
        System.out.println(storePath.getPath());
        // 获取缩略图路径
        String path = thumbImageConfig.getThumbImagePath(storePath.getPath());
        System.out.println(path);
        //group1/M00/00/00/wKijg11_ncKAfQmEAAGfdb76emI542.jpg
        //M00/00/00/wKijg11_ncKAfQmEAAGfdb76emI542.jpg
        //M00/00/00/wKijg11_ncKAfQmEAAGfdb76emI542_60x60.jpg
    }
}

