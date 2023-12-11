package com.pius.im.service.exception;

import com.pius.im.common.BaseErrorCode;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.exception.ApplicationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseVO unknownException(Exception e) {
        log.error(e.getMessage());
        ResponseVO resultBean = new ResponseVO();
        resultBean.setCode(BaseErrorCode.SYSTEM_ERROR.getCode());
        resultBean.setMsg(BaseErrorCode.SYSTEM_ERROR.getError());
        return resultBean;
    }

    /**
     * Validator 参数校验异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleMethodArgumentNotValidException(ConstraintViolationException e) {

        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        ResponseVO resultBean = new ResponseVO();
        resultBean.setCode(BaseErrorCode.PARAMETER_ERROR.getCode());
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            PathImpl pathImpl = (PathImpl) constraintViolation.getPropertyPath();
            // 读取参数字段，constraintViolation.getMessage() 读取验证注解中的message值
            String paramName = pathImpl.getLeafNode().getName();
            String message = "参数{".concat(paramName).concat("}").concat(constraintViolation.getMessage());
            resultBean.setMsg(message);

            return resultBean;
        }
        resultBean.setMsg(BaseErrorCode.PARAMETER_ERROR.getError() + e.getMessage());
        return resultBean;
    }

    @ExceptionHandler(ApplicationException.class)
    public Object applicationExceptionHandler(ApplicationException e) {
        ResponseVO resultBean = new ResponseVO();
        resultBean.setCode(e.getCode());
        resultBean.setMsg(e.getError());
        return resultBean;
    }

    /**
     * Validator 参数校验异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Object handleException2(BindException e) {
        FieldError err = e.getFieldError();
        String message = "参数{".concat(err.getField()).concat("}").concat(err.getDefaultMessage());
        ResponseVO resultBean = new ResponseVO();
        resultBean.setCode(BaseErrorCode.PARAMETER_ERROR.getCode());
        resultBean.setMsg(message);
        return resultBean;
    }

    //json格式
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object handleException1(MethodArgumentNotValidException e) {
        StringBuilder errorMsg = new StringBuilder();
        BindingResult re = e.getBindingResult();
        for (ObjectError error : re.getAllErrors()) {
            errorMsg.append(error.getDefaultMessage()).append(",");
        }
        errorMsg.delete(errorMsg.length() - 1, errorMsg.length());

        ResponseVO resultBean = new ResponseVO();
        resultBean.setCode(BaseErrorCode.PARAMETER_ERROR.getCode());
        resultBean.setMsg(BaseErrorCode.PARAMETER_ERROR.getError() + " : " + errorMsg);
        return resultBean;
    }

}
