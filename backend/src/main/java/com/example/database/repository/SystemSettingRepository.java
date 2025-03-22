package com.example.database.repository;

import com.example.database.models.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer>
{
    Optional<SystemSetting> findByKey(String key);
}