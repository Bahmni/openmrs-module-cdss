package org.bahmni.module.fhircdss.api.model.alert;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
public class CDSSource {

	private String label;
	private String url;
	private String icon;
	private CDSCoding topic;

}
