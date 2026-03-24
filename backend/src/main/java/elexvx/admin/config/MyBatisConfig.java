package elexvx.admin.config;

import java.util.Properties;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {
  @Bean
  public DatabaseIdProvider databaseIdProvider() {
    Properties properties = new Properties();
    properties.setProperty("MySQL", "mysql");
    properties.setProperty("PostgreSQL", "postgresql");
    properties.setProperty("Oracle", "oracle");
    properties.setProperty("Microsoft SQL Server", "sqlserver");
    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    provider.setProperties(properties);
    return provider;
  }
}
