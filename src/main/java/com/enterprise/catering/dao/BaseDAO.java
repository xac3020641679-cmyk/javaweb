package com.enterprise.catering.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;


public class BaseDAO {
    @Autowired
    protected JdbcTemplate jdbcTemplate;
}







