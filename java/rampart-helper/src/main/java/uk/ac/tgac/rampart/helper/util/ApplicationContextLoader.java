/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.helper.util;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Bootstraps Spring-managed beans into an application. How to use:
 * <ul>
 * <li>Create application exectx XML configuration files and put them where
 * they can be loaded as class path resources. The configuration must include
 * the {@code <exectx:annotation-config/>} element to enable annotation-based
 * configuration, or the {@code <exectx:component-scan base-package="..."/>}
 * element to also detect bean definitions from annotated classes.
 * <li>Create a "main" class that will receive references to Spring-managed
 * beans. Add the {@code @Autowired} annotation to any properties you want to be
 * injected with beans from the application exectx.
 * <li>In your application {@code main} method, create an
 * {@link ApplicationContextLoader} instance, and call the {@link #load} method
 * with the "main" object and the configuration file locations as parameters.
 * </ul>
 */
public class ApplicationContextLoader {

	protected ConfigurableApplicationContext applicationContext;

	public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Loads application exectx. Override this method to change how the
     * application exectx is loaded.
     * 
     * @param configLocations
     *            configuration file locations
     */
    protected void loadApplicationContext(String... configLocations) {
        applicationContext = new ClassPathXmlApplicationContext(
                configLocations);
        applicationContext.registerShutdownHook();
    }

    /**
     * Injects dependencies into the object. Override this method if you need
     * full control over how dependencies are injected.
     * 
     * @param main
     *            object to inject dependencies into
     */
    protected void injectDependencies(Object main) {
        getApplicationContext().getBeanFactory().autowireBeanProperties(
                main, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
    }

    /**
     * Loads application exectx, then injects dependencies into the object.
     * 
     * @param main
     *            object to inject dependencies into
     * @param configLocations
     *            configuration file locations
     */
    public void load(Object main, String... configLocations) {
        loadApplicationContext(configLocations);
        injectDependencies(main);
    }
    
    
}
