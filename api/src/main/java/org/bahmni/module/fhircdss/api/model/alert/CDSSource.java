/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

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
