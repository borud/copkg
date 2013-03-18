package org.cloudname.copkg;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Objects;

import org.cloudname.copkg.PackageCoordinate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.SerializationFeature;


import java.util.Map;
import java.util.TreeSet;
import java.util.Collections;
import java.io.IOException;

/**
 * Job description.  A job consists of:
 *
 * <ul>
 *   <li> service coordinate
 *   <li> package coordinate
 *   <li> a key/value map of parameters
 * </ul>
 *
 * To indicate that a parameter that takes no value is present a key
 * pointing to {@code null} is added to the map.
 *
 * @author borud
 */
public final class Job {
    // These are strings because this class is JSON serializable and
    // we need to do a bit of work before we can replace these with
    // the proper types.
    private final String serviceCoordinate;
    private final String packageCoordinate;
    private final Map<String,String> params;

    /**
     * @param serviceCoordinate the service coordinate of the job
     * @param packageCoordinate the package coordinate of the job
     * @param map holding key-value pairs that represent the parameters of the job
     */
    @JsonCreator
    public Job(@JsonProperty("serviceCoordinate") String serviceCoordinate,
               @JsonProperty("packageCoordinate") String packageCoordinate,
               @JsonProperty("params") Map<String, String> params) {
        checkNotNull(serviceCoordinate);
        checkNotNull(packageCoordinate);
        checkNotNull(params);
        this.serviceCoordinate = serviceCoordinate;
        this.packageCoordinate = packageCoordinate;
        this.params = params;
    }

    public String getServiceCoordinate() {
        return serviceCoordinate;
    }

    public String getPackageCoordinate() {
        return packageCoordinate;
    }

    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Parse a JSON blob and return a Job instance.
     * @return a Job instance.
     */
    public static Job parse(String json) throws IOException {
        checkNotNull(json);
        return new ObjectMapper().readValue(json, Job.class);
    }

    /**
     * Transform this instance to a JSON blob.
     */
    public String toJson() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get the parameters formatted as a command line options array.
     * The parameters will always appear in lexical order for
     * predictability.
     *
     * <i>If you find yourself tempted to just join this array by
     * spaces beware that it isn't quite that simple.  You will have
     * to re-add the quotation marks around arguments that contain
     * whitespace.</i>
     *
     * @return the parameters formatted as a command line option
     *   array.
     */
    @JsonIgnore // derived property
    public String[] getOptionArray() {
        String[] optionArray = new String[params.size()];
        int index = 0;
        for (String key : new TreeSet<String>(params.keySet())) {
            String value = params.get(key);
            optionArray[index++] = "--" + key + ((value == null) ? "" : ("=" + value));
        }
        return optionArray;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serviceCoordinate, packageCoordinate, params);
    }

    @Override
    public boolean equals(final Object obj){
        if(! (obj instanceof Job)) {
            return false;
        }

        final Job other = (Job) obj;
        return Objects.equal(packageCoordinate, other.packageCoordinate)
            && Objects.equal(serviceCoordinate, other.serviceCoordinate)
            && Objects.equal(params, other.params);
    }

    @Override
    public String toString() {
        return Job.class.getName() + " " + toJson();
    }
}
