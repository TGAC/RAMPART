package uk.ac.tgac.rampart.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

    public static ApplicationContext getAppContext() {
    	return applicationContext;
    }
        
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
    	ApplicationContextProvider.applicationContext = applicationContext;
    }
}
