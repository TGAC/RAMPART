/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
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
 */

package uk.ac.tgac.rampart.util;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class SpiFactory<T extends Service> {

    private ServiceLoader<T> loader;
    private Class<T> clazz;

    @SuppressWarnings("unchecked")
    public SpiFactory(Class<T> clazz) {
        this.clazz = clazz;
        this.loader = ServiceLoader.load(clazz);
    }

    /**
     * This default method accepts an identifier if it equals the service's name, or if it matches the class names.
     * @param identifier The identifier of the service
     * @param service The service to be checked
     * @return Whether the service accepts the given identifier
     */
    protected boolean acceptsIdentifier(String identifier, T service) {
       return   identifier.equalsIgnoreCase(service.getName()) ||
                identifier.equalsIgnoreCase(this.clazz.getCanonicalName()) ||
                identifier.equalsIgnoreCase(this.clazz.getName());
    }

    /**
     * Creates a new instance of the service with the specified name if it can be found, otherwise return null
     * @param name The identifier name for the service to create
     * @return A new service
     */
    public T create(String name) {

        // Create the appropriate service, if the name is recognized
        for (T service : loader) {
            if (this.acceptsIdentifier(name, service)) {
                return service;
            }
        }

        return null;
    }

    /**
     * Creates a new instance of the service with the specified name if it can be found, otherwise return null
     * @param name The identifier name for the service to create
     * @param ces The conan executor service which should be injected into the created service
     * @return A new service, with conan executor service injected inside it
     */
    public T create(String name, ConanExecutorService ces) {

        // Create the appropriate service, if the name is recognized
        for (T service : loader) {
            if (this.acceptsIdentifier(name, service)) {

                // Horrible hack!!!  Cleanup at some point.
                ((AbstractConanProcess)service).setConanProcessService(ces.getConanProcessService());
                return service;
            }
        }

        return null;
    }

    /**
     * Checks if the specified service exists.
     * @param name The name of the service to check
     * @return True if the serivce was found, false otherwise
     */
    public boolean serviceAvailable(String name) {

        for(T service : this.getServices()) {
            if (this.acceptsIdentifier(name, service)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Goes through all requested services found on the classpath and returns a list as a string, surrounded with square
     * brackets
     *
     * @return A list of services represented in a string.
     */
    public String listServicesAsString() {

        List<String> typeStrings = listServices();

        return "[" + StringUtils.join(typeStrings, ", ") + "]";
    }


    /**
     * Returns a list of a strings describing all the classes that are available
     * @return A list of services, each service is represented by a string.
     */
    public List<String> listServices() {

        List<T> services = getServices();

        List<String> typeStrings = new ArrayList<>();

        for (T service : services) {
            typeStrings.add(service.getName());
        }

        return typeStrings;
    }

    /**
     * Returns a list of the requested services
     * @return A list of services
     */
    public List<T> getServices() {

        Iterator<T> it = loader.iterator();

        List<T> serviceList = new ArrayList<>();

        while (it.hasNext()) {
            serviceList.add(it.next());
        }

        return serviceList;
    }

}