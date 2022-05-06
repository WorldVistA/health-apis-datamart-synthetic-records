package gov.va.api.health.minimartmanager;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.jpa.HibernatePersistenceProvider;

@Value
@Builder
@Accessors(fluent = false)
public class PersistenceUnit implements PersistenceUnitInfo {
  String persistenceUnitName;

  @Builder.Default
  String persistenceProviderClassName = HibernatePersistenceProvider.class.getName();

  @Builder.Default
  PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

  DataSource jtaDataSource;

  @Builder.Default List<String> mappingFileNames = emptyList();

  @Builder.Default List<URL> jarFileUrls = emptyList();

  URL persistenceUnitRootUrl;

  List<Class<?>> managedClasses;

  @Builder.Default boolean excludeUnlistedClasses = false;

  @Builder.Default SharedCacheMode sharedCacheMode = SharedCacheMode.NONE;

  @Builder.Default ValidationMode validationMode = ValidationMode.AUTO;

  @Builder.Default Properties properties = new Properties();

  @Builder.Default String persistenceXMLSchemaVersion = "2.1";

  @Builder.Default ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

  @Override
  public void addTransformer(ClassTransformer transformer) {}

  @Override
  public boolean excludeUnlistedClasses() {
    return excludeUnlistedClasses;
  }

  @Override
  public List<String> getManagedClassNames() {
    return managedClasses.stream().map(Class::getName).collect(toList());
  }

  @Override
  public ClassLoader getNewTempClassLoader() {
    return null;
  }

  @Override
  public DataSource getNonJtaDataSource() {
    return getJtaDataSource();
  }
}
