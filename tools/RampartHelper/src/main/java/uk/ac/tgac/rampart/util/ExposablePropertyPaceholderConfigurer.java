package uk.ac.tgac.rampart.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class ExposablePropertyPaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private Map<String, String> resolvedProps;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
            throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        resolvedProps = new HashMap<String, String>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            resolvedProps.put(keyStr, resolvePlaceholder(props.getProperty(keyStr), props));
        }
    }

    public Map<String, String> getResolvedProps() {
        return Collections.unmodifiableMap(resolvedProps);
    }

}