package org.bahmni.module.fhircdss.api.model.alert;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CDSCard {

	private String uuid;

	private CDSIndicator indicator;

	private String summary;

	private String detail;

	private CDSSource source;
}
