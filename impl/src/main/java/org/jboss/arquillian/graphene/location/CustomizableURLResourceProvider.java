/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.graphene.location;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.spi.configuration.GrapheneConfiguration;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The CustomizableURLResourceProvider is used in the context of Graphene, if you use
 * the standalone framework integration option (see https://docs.jboss.org/author/display/ARQGRA2/Framework+Integration+Options)
 * and thus the Arquillian {@link org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider} is not on
 * on the classpath.
 *
 * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider
 */
public class CustomizableURLResourceProvider implements ResourceProvider {

    private static final Logger log = Logger.getLogger(CustomizableURLResourceProvider.class.getName());

    @Inject
    private Instance<GrapheneConfiguration> grapheneConfiguration;

    @Inject
    private Instance<TestClass> testClass;

    @Override
    public boolean canProvide(Class<?> type) {
        //can only provide an URL resource
        if (!URL.class.isAssignableFrom(type)) {
            return false;
        }
        //no custom URL can be provided if not configured
        String configuredURL = grapheneConfiguration.get().getUrl();
        if (configuredURL == null) {
            return false;
        }
        //check if the configured URL is a valid one
        try {
            new URL(configuredURL);
        } catch (MalformedURLException ex) {
            return false;
        }
        //the custom URL provider does not operate on deployment
        //WARNING: this would not work with ARQ Suite!
        if (hasDeployment(testClass.get())) {
            return false;
        }
        return true;
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        String configuredURL = grapheneConfiguration.get().getUrl();
        try {
            return new URL(configuredURL);
        } catch (MalformedURLException ex) {
            log.log(Level.SEVERE, "should never happen since we already went through two checks!", ex);
        }
        return null;
    }

    private boolean hasDeployment(TestClass testClass) {
        return testClass.getMethods(Deployment.class).length > 0;
    }
}
