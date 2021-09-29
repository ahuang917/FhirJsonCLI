# FhirJsonCLI
Command line interface that parses FHIR resource ndjson files. Takes patient first name and last name or ID to find the number times the ID is referenced in each resource type.

## Getting Started

### Prerequisites
json_simple1.1.jar https://code.google.com/archive/p/json-simple/downloads

Latest version of Java https://www.oracle.com/java/technologies/downloads/

## Installation and Usage
1. Clone repository.
```
git clone https://github.com/ahuang917/FhirJsonCLI.git
```
2. In a command prompt, change directory to the respoistory containing .java and .ndjson files.
3. Run Java command using the abosolute path of json_simple1.1.jar as class path and with Patient identifiers.

Passing Patient First and Last name as arguments
```
java -cp "C:\path\to\json-simple-1.1.jar" CountResources.java Rosamond509 Lynch190
```
Passing patient identifier
```
java -cp "C:\path\to\json-simple-1.1.jar" CountResources.java 34ff2141-1565-4564-a801-18f019fa10ab
```

