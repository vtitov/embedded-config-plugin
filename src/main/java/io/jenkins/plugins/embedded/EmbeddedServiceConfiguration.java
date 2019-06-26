package io.jenkins.plugins.embedded;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Failure;
import jenkins.model.GlobalConfiguration;
import lombok.extern.java.Log;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log
@Extension
public class EmbeddedServiceConfiguration extends GlobalConfiguration {
    private List<EmbeddedProvider> services = createEmptyServicesCollection();

    private static List<EmbeddedProvider> createEmptyServicesCollection() {
        return new ArrayList<>();
    }

    public EmbeddedServiceConfiguration(java.util.List<EmbeddedProvider> services) {
        setServices(services);
        start();
    }

    @DataBoundConstructor
    public EmbeddedServiceConfiguration() {
        load();
        start();
    }

    @DataBoundSetter
    public void setServices(java.util.List<EmbeddedProvider> services) {
        stop();
        this.services = services;
    }
    public java.util.List<EmbeddedProvider> getServices() {
        return Collections.unmodifiableList(this.services);
    }


    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
        log.info("StaplerRequest uri: " + req.getRequestURLWithQueryString() + ", " + req.getRequestURL());
        log.info("JSONObject: " + json.toString());
        stop();
        req.bindJSON(this,json);
        save();
        start();
        return true;
    }

    public boolean addMessageProvider(EmbeddedProvider provider) {
        if (services == null) services = createEmptyServicesCollection();
        if (services.contains(provider)) {
            throw new Failure("Attempt to add a duplicate message provider");
        }
        services.add(provider);
        return true;
    }


    @Override
    public String getDisplayName() {
        return Messages.PluginName();
    }

    protected void stop() {
        getServices().forEach(EmbeddedProvider::stop);
    }

    protected void start() {
        getServices().forEach(EmbeddedProvider::start);
    }
}
