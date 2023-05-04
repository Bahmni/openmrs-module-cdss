package org.bahmni.module.fhircdss.api.model.alert;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CDSAlert {

	private String uuid;

	private CDSIndicator indicator;

	private String summary;

	private String detail;

	private CDSSource source;
}
