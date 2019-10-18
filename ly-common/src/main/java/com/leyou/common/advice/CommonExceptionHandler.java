package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by lvmen on 2019/9/3
 */
@ControllerAdvice//默认情况下拦截加了Controller的类下的方法
public class CommonExceptionHandler {

    @ExceptionHandler(LyException.class)//抛出这个异常时,执行这个方法
    public ResponseEntity<ExceptionResult> handleException(LyException e){
        System.out.println("********"+e);
        return ResponseEntity.status(e.getExceptionEnum().getCode())
                .body(new ExceptionResult(e.getExceptionEnum()));
    }
}
