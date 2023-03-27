package org.bahmni.module.fhircdss.api.model.request;

import java.io.Serializable;

public class CDSRequest {

    String hook;
    Prefetch prefetch;

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public Prefetch getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(Prefetch prefetch) {
        this.prefetch = prefetch;
    }

    @Override
    public String toString() {

        return "{" +
                "\"hook\" : \"" + hook + "\"" +
                ",\"prefetch\" : " + prefetch.toString() +
                '}';
    }
}
