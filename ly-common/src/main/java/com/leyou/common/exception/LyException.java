package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by lvmen on 2019/9/3
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LyException extends RuntimeException{//抛异常时抛出自定义的异常类

    private ExceptionEnum exceptionEnum;

}
