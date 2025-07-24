package com.practice.kyi.config;


import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class DbConfig {
	
    @Bean(name = "sqlSession")
	SqlSessionFactory sqlSessionFactory(DataSource dataSource, ApplicationContext applicationContext) throws Exception {
    	SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
	    sqlSessionFactoryBean.setDataSource(dataSource);
	    sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:MyBatis_setting.xml"));
	    Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath:sqlmap/**/*.xml");
	    sqlSessionFactoryBean.setMapperLocations(resources);
	    return sqlSessionFactoryBean.getObject();
	}
	
	@Bean
	DataSourceTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

}
