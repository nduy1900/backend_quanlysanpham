package com.example.Quanlysanpham.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    // Ghi log trước khi thực thi phương thức service
    @Before("execution(* com.example.Quanlysanpham.service..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("===========BEFORE==========");

        // Tên class chứa phương thức
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Tên phương thức
        String methodName = joinPoint.getSignature().getName();

        // Các tham số đầu vào
        Object[] args = joinPoint.getArgs();

        log.info("Tên class: " + className);
        log.info("Tên phương thức được gọi: " + methodName);
        log.info("Tham số đầu vào: " + Arrays.deepToString(args));

    }

    // Ghi log sau khi phương thức thực thi thành công
    @AfterReturning(pointcut = "execution(* com.example.Quanlysanpham.service..*(..))",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("===========AFTER RETURNING==========");

        // Tên class chứa phương thức
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Tên phương thức
        String methodName = joinPoint.getSignature().getName();

        log.info("Tên class: " + className);
        log.info("Tên phương thức: " + methodName);
        log.info("Kết quả trả về: " + result);
        log.info("Thực thi thành công");
    }

    // Ghi log khi phương thức xảy ra lỗi
    @AfterThrowing(pointcut = "execution(* com.example.Quanlysanpham.service..*(..))",
            throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        System.out.println("===========AFTER THROWING==========");

        // Tên class chứa phương thức
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Tên phương thức
        String methodName = joinPoint.getSignature().getName();

        ex.printStackTrace();
        log.error("Phương thức xảy ra lỗi: " + methodName);
        log.error("Thông báo lỗi: " + ex.getMessage());
        log.error("Loại exception: " + ex.getClass().getSimpleName());
    }

    // Đo thời gian thực thi service
    @Around("execution(* com.example.Quanlysanpham.service..*(..))")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();

        // Tên phương thức
        String methodName = joinPoint.getSignature().getName();
        System.out.println("=============AROUND============");
        log.info("Phương thức " + methodName + " executed in " + (end - start) + " ms");
        return result;
    }
}
