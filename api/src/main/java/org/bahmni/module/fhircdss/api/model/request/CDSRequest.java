package org.bahmni.module.fhircdss.api.model.request;

public class CDSRequest {

    private String hook;
    private Prefetch prefetch;

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

    public CDSRequest() {
        this.hook = "";
        this.prefetch = new Prefetch();
    }

    @Override
    public String toString() {

        return "{" +
                "\"hook\" : \"" + hook + "\"" +
                ",\"prefetch\" : " + prefetch.toString() +
                '}';
    }
}
