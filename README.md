# openmrs-module-cdss
This module provides integration with FHIR compatible CDSS Service.

This module used to identify drug diagnosis contradictions and uses FHIR based communication. More details can be found [here](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3132686337/SNOMED+FHIR+Terminology+Server+Integration+with+Bahmni)


API Documentation [here](https://editor.swagger.io/?url=https://raw.githubusercontent.com/Bahmni/openmrs-module-cdss/main/omod/src/main/resources/openapi.yaml).

### Prerequisite
1. JDK 1.8
2. FHIR compatible CDSS Server (Reference implementation found [here](https://github.com/Bahmni/snomed-fhir-cds-service))

## Packaging
```mvn clean package```

The output is the OMOD file:
```openmrs-module-cdss/omod/target/fhir-cdss-int-[VERSION].omod```

## Deploy

Copy ```openmrs-module-cdss/omod/target/fhir-cdss-int-[VERSION].omod``` into OpenMRS modules directory and restart OpenMRS