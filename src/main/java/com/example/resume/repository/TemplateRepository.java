package com.example.resume.repository;

import com.example.resume.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {


    Template findByName(String name);
}
