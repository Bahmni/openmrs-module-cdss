package org.bahmni.module.fhircdss.api.model.alert;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CDSCoding {

	private String system;
	private String code;
	private String display;

}
