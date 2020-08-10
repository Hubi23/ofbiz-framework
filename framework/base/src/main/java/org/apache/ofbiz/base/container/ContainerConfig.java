/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.base.container;

import org.apache.ofbiz.base.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

/**
 * ContainerConfig - Container configuration for ofbiz.xml
 *
 */
public class ContainerConfig {

    private ContainerConfig() {}

    public static final String module = ContainerConfig.class.getName();

    private static Map<String, Configuration> configurations = new LinkedHashMap<>();

    public static Configuration getConfiguration(String containerName, String configFile) throws ContainerException {
        Configuration configuration = configurations.get(containerName);
        if (configuration == null) {
            getConfigurations(configFile);
            configuration = configurations.get(containerName);
        }
        if (configuration == null) {
            throw new ContainerException("No container found with the name : " + containerName);
        }
        return configuration;
    }

    public static Collection<Configuration> getConfigurations(String configFile) throws ContainerException {
        if (UtilValidate.isEmpty(configFile)) {
            throw new ContainerException("configFile argument cannot be null or empty");
        }
        URL xmlUrl = UtilURL.fromResource(configFile);
        if (xmlUrl == null) {
            throw new ContainerException("Could not find container config file " + configFile);
        }
        return getConfigurations(xmlUrl);
    }

    public static Collection<Configuration> getConfigurations(URL xmlUrl) throws ContainerException {
        if (xmlUrl == null) {
            throw new ContainerException("xmlUrl argument cannot be null");
        }
        Collection<Configuration> result = getConfigurationPropsFromXml(xmlUrl);
        synchronized (ContainerConfig.class) {
            for (Configuration container : result) {
                configurations.put(container.name, container);
            }
        }
        return result;
    }

    public static String getPropertyValue(ContainerConfig.Configuration parentProp, String name, String defaultValue) {
        ContainerConfig.Configuration.Property prop = parentProp.getProperty(name);
        if (prop == null || UtilValidate.isEmpty(prop.value)) {
            return defaultValue;
        }
        return prop.value;
    }

    public static int getPropertyValue(ContainerConfig.Configuration parentProp, String name, int defaultValue) {
        ContainerConfig.Configuration.Property prop = parentProp.getProperty(name);
        if (prop == null || UtilValidate.isEmpty(prop.value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(prop.value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getPropertyValue(ContainerConfig.Configuration parentProp, String name, boolean defaultValue) {
        ContainerConfig.Configuration.Property prop = parentProp.getProperty(name);
        if (prop == null || UtilValidate.isEmpty(prop.value)) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(prop.value);
    }

    public static String getPropertyValue(ContainerConfig.Configuration.Property parentProp, String name, String defaultValue) {
        ContainerConfig.Configuration.Property prop = parentProp.getProperty(name);
        if (prop == null || UtilValidate.isEmpty(prop.value)) {
            return defaultValue;
        }
        return prop.value;
    }

    public static int getPropertyValue(ContainerConfig.Configuration.Property parentProp, String name, int defaultValue) {
        ContainerConfig.Configuration.Property prop = parentProp.getProperty(name);
        if (prop == null || UtilValidate.isEmpty(prop.value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(prop.value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getPropertyValue(ContainerConfig.Configuration.Property parentProp, String name, boolean defaultValue) {
        ContainerConfig.Configuration.Property prop = parentProp.getProperty(name);
        if (prop == null || UtilValidate.isEmpty(prop.value)) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(prop.value);
    }

    private static Collection<Configuration> getConfigurationPropsFromXml(URL xmlUrl) throws ContainerException {
        Document containerDocument = null;
        try {
            containerDocument = UtilXml.readXmlDocument(xmlUrl, true);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ContainerException("Error reading the container config file: " + xmlUrl, e);
        }
        Element root = containerDocument.getDocumentElement();
        // REFACTO to use stream(), map(), collect()
        return UtilXml.childElementList(root, "container").stream()
            .map(Configuration::new)
            .collect(Collectors.toList());
    }

    public static class Configuration {
        public final String name;
        public final String className;
        public final List<String> loaders;
        public final Map<String, Property> properties;

        public Configuration(Element element) {
            this.name = element.getAttribute("name");
            this.className = element.getAttribute("class");
            this.loaders = StringUtil.split(element.getAttribute("loaders"), ",");

            properties = new LinkedHashMap<>();
            for (Element curElement: UtilXml.childElementList(element, "property")) {
                Property property = new Property(curElement);
                properties.put(property.name, property);
            }
        }

        public Property getProperty(String name) {
            return properties.get(name);
        }

        public List<Property> getPropertiesWithValue(String value) {
            // REFACTO to use stream(), filter() x 2, collect(), Collectors.toCollection()
            if (UtilValidate.isEmpty(properties))
                return new LinkedList<>();
            return properties.values().stream()
                .filter(Objects::nonNull)
                .filter(p -> value.equals(p.value))
                .collect(Collectors.toCollection(LinkedList::new));
        }

        public static class Property {
            public String name;
            public String value;
            public Map<String, Property> properties;

            public Property(Element element) {
                this.name = element.getAttribute("name");
                this.value = element.getAttribute("value");
                if (UtilValidate.isEmpty(this.value)) {
                    this.value = UtilXml.childElementValue(element, "property-value");
                }

                properties = new LinkedHashMap<>();
                for (Element curElement: UtilXml.childElementList(element, "property")) {
                    Property property = new Property(curElement);
                    properties.put(property.name, property);
                }
            }

            public Property getProperty(String name) {
                return properties.get(name);
            }

            public List<Property> getPropertiesWithValue(String value) {
                List<Property> props = new LinkedList<>();
                if (UtilValidate.isNotEmpty(properties)) {
                    for (Property p: properties.values()) {
                        if (p != null && value.equals(p.value)) {
                            props.add(p);
                        }
                    }
                }
                return props;
            }
        }
    }
}
