package com.palgona.palgona.common.aop;

import com.palgona.palgona.common.annotation.Retry;
import jakarta.persistence.OptimisticLockException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OptimisticLockAop {

    @Around("@annotation(com.palgona.palgona.common.annotation.Retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Retry retry = method.getAnnotation(Retry.class);
        Exception exceptionHolder = null;
        int maxRetryCount = retry.maxRetryCount();
        long retryInterval = retry.retryInterval();
        while (maxRetryCount-- > 0) {
            try {
                return joinPoint.proceed();

            } catch (OptimisticLockException e) {
                exceptionHolder = e;
                Thread.sleep(retryInterval);
            }
        }

        throw exceptionHolder;
    }
}
