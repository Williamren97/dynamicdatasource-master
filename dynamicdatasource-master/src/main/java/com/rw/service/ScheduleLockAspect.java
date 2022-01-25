package com.rw.service;

import com.rw.mapper.ScheduledLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
public class ScheduleLockAspect implements ApplicationContextAware {

    private  static final  Logger logger = LoggerFactory.getLogger(ScheduleLockAspect.class);

    private  static  ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public  static Object getBean(String name) {
        return getContext().getBean(name);
    }

    @Around(value = "@annotation(scheduledLock)")
    public Object around(ProceedingJoinPoint point, ScheduledLock scheduledLock) {
        //return proceed;
        Class clazz = point.getTarget().getClass();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        logger.info("\"定时任务锁 拦截了类:\" + clazz + \" 方法:\" + method");
        Object proceed = null;
        RedisTemplate<String, String> redisTemplate = (RedisTemplate) getBean("redisTemplate");
        if (redisTemplate.opsForValue().setIfAbsent("rw-SchdulesLock-" + method.getName(), "lock",10,TimeUnit.SECONDS )) {
            logger.info("其他服务未执行，通过执行");
            //获取锁，如果为false说明有其他服务正在执行，跳过执行
            try {
                proceed = point.proceed();//执行定时任务
                redisTemplate.delete("rw-SchdulesLock-" + method.getName());
                return  proceed;
            } catch (Throwable throwable) {
                redisTemplate.delete("rw-SchdulesLock-" + method.getName());
                throwable.printStackTrace();
                return  null;
            }
        }
        logger.info("其他服务已执行，未通过执行");
        return  proceed;
    }
}
