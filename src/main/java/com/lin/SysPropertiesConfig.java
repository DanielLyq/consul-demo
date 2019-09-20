package com.lin;

import com.lin.utils.SecurityUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.cloud.consul.config.ConsulPropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class SysPropertiesConfig extends PropertyResourceConfigurer
{

    private static final List<String> sysConfigList = new ArrayList<>();

    SysPropertiesConfig() {
        sysConfigList.add("spring.datasource.username");
        sysConfigList.add("spring.datasource.password");
        sysConfigList.add("config.mongodb.user");
        sysConfigList.add("config.mongodb.password");
    }

    @Override
    protected void processProperties( ConfigurableListableBeanFactory beanFactory,
                                      Properties props ) {

        StandardServletEnvironment standardServletEnvironment = (StandardServletEnvironment) beanFactory
                .getBean(Environment.class);

        MutablePropertySources mutablePropertySources = standardServletEnvironment
                .getPropertySources();
        Iterator<PropertySource<?>> iterator = mutablePropertySources.iterator();
        while (iterator.hasNext())
        {
            PropertySource propertySource = iterator.next();
            if (propertySource instanceof CompositePropertySource)
            {
                CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
                try {
                    decrypt(compositePropertySource);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {

                }
            }
        }
    }

    private void decrypt(CompositePropertySource compositePropertySource) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (String s : sysConfigList) {
            PropertySource encrypProperty = getEncrypProperty(s, compositePropertySource);
            if (encrypProperty != null) {
                if (encrypProperty instanceof CompositePropertySource) {
                    encrypProperty = getEncrypProperty(s, (CompositePropertySource) encrypProperty);
                }
                if (encrypProperty instanceof ConsulPropertySource) {
                    final Method method = ((ConsulPropertySource) encrypProperty).getClass().getDeclaredMethod("getProperties");
                    method.setAccessible(true);
                    final Object invoke = method.invoke(encrypProperty);
                    ((Map<String, Object>)invoke).put(s, SecurityUtils.decode(encrypProperty.getProperty(s).toString()));
                }
            }
        }
    }

    private PropertySource getEncrypProperty(String configKey, CompositePropertySource compositePropertySource) {
        for (PropertySource<?> propertySource : compositePropertySource.getPropertySources()) {
            final Object property = propertySource.getProperty(configKey);
            if (property != null) {
                return propertySource;
            }
        }
        return null;
    }
}