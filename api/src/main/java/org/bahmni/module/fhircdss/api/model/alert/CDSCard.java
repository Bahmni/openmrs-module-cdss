package org.bahmni.module.fhircdss.api.model.alert;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CDSCard {

	private String uuid;

	private CDSIndicator indicator;

	private String summary;

	private String detail;

	private CDSSource source;
}
