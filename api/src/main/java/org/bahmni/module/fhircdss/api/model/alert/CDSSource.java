package org.bahmni.module.fhircdss.api.model.alert;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CDSSource {

	private String label;
	private String url;
	private String icon;
	private CDSCoding topic;

}
