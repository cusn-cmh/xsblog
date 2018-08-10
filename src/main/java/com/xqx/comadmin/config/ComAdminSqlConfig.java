package com.xqx.comadmin.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootConfiguration
@MapperScan(basePackages = "com.xqx.comadmin.mapper")
public class ComAdminSqlConfig {

    @Bean
    @Primary
    public SqlSessionFactory comAdminSqlSessionFactory(DataSource comAdminDataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(comAdminDataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/comadmin/*Mapper.xml"));
        bean.setTypeAliasesPackage("com.xqx.comadmin.entity");
        return bean.getObject();
    }

    /**
     * 默认事物
     * 调用时使用：@Transactional
     * @return
     */
    @Bean
    @Primary
    public PlatformTransactionManager comAdminTransactionManager(DataSource comAdminDataSource) {
        return new DataSourceTransactionManager(comAdminDataSource);
    }
}