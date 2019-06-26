package io.jenkins.plugins.embedded;

import hudson.ExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jenkins.model.Jenkins;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.Serializable;
import java.util.logging.Level;


@Log
public abstract class EmbeddedProvider implements Describable<EmbeddedProvider> {

    private static final int MAX_UNSIGNED_16_BIT_INT = 0xFFFF; // port max

    abstract public void start();
    abstract public void stop();

    protected FormValidation xDoCheckPort(@QueryParameter String value) {
        return parsePort(value)
                .filter(iPort -> iPort < 0 || iPort > MAX_UNSIGNED_16_BIT_INT)
                .fold(
                        ()->FormValidation.warning("Please specify an integer in range 0..65536"),
                        iPort -> FormValidation.ok()
                );
    }

    protected static Option<String> parseDir(String directory) {
        return StringUtils.isEmpty(directory)
                ? Option.none()
                : Option.of(directory);
    }

    protected static Option<Integer> parsePort(String value) {
        return Try.of(()-> Integer.parseInt(value))
                .onFailure(e -> log.log(Level.FINE, "no port specified", e))
                .toOption();
    }


    public abstract static class EmbeddedProviderDescriptor extends Descriptor<EmbeddedProvider> {

        public FormValidation doCheckPath(@QueryParameter String value) {
            return xDoCheckPath(value,null);
        }
        protected FormValidation xDoCheckPath(String value, String subdir) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            StringBuilder sb = new StringBuilder();
            if (value.length()>0) { // no value entered yet means ok
                if (new File(value).isFile()) {
                    return FormValidation.error("%s is a file; must be a directory.", value);
                }
                if (!StringUtils.isEmpty(subdir)) {
                    if (new File(value, subdir).exists())
                        sb.append(String.format("%s already exists.", subdir));
                    else
                        sb.append(String.format("%s doesn't exist yet. It will be created.", subdir));
                }
            }
            return FormValidation.ok(sb.toString());
        }

        public static ExtensionList<EmbeddedProviderDescriptor> all() {
            return Jenkins.getInstance().getExtensionList(EmbeddedProviderDescriptor.class);
        }
    }
}
