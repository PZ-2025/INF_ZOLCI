package com.example.backend.aspects;

import com.example.backend.exceptions.DatabaseConnectionException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import jakarta.persistence.PersistenceException;

/**
 * Aspekt do przechwytywania i obsługi wyjątków związanych z bazą danych.
 * <p>
 * Działa na poziomie metod w repozytoriach, automatycznie przechwytując
 * wyjątki związane z bazą danych i konwertując je na DatabaseConnectionException.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Aspect
@Component
public class DatabaseExceptionAspect {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseExceptionAspect.class);

    /**
     * Definicja punktu przecięcia dla wszystkich metod w repozytoriach.
     */
    @Pointcut("execution(* com.example.backend.repository.*.*(..))")
    private void repositoryMethods() {}

    /**
     * Przechwytuje wywołania metod repozytoriów i obsługuje wyjątki związane z bazą danych.
     *
     * @param joinPoint punkt przecięcia
     * @return wynik wywołania metody
     * @throws Throwable jeśli wystąpi inny wyjątek
     */
    @Around("repositoryMethods()")
    public Object handleDatabaseExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataAccessException | PersistenceException e) {
            String methodName = joinPoint.getSignature().toShortString();
            logger.error("Błąd dostępu do bazy danych w metodzie {}: {}", methodName, e.getMessage());

            throw new DatabaseConnectionException(
                    "Wystąpił problem z połączeniem do bazy danych podczas wykonywania operacji", e);
        }
    }
}