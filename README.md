# Nexus Cleaner

## Introduction
The automated builds in a continuous integration environment might generate lots of artifacts daily
thus many of the early builds becoming garbage needed to be collected.
Nexus Cleaner is a tool for maintaining a clean nexus repository by deleting the artifacts which are no longer being used.

## How does it work

The Nexus Cleaner tool deletes all versions of a given artifact except the last 2 and the versions specified in the artifact metadata which is a json file.

For example, given the following artifact metadata:

```json
{
  "groupId": "com.example",
  "artifactId": "my-artifact",
  "versionsToKeep": [ "1.2", "1.3" ],
  "environment": "acceptance"
}
```

and the following versions deployed on nexus as read from the artifact's maven-metadata.xml found on nexus:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>com.example</groupId>
  <artifactId>my-artifact</artifactId>
  <versioning>
    <release>1.8</release>
    <versions>
      <version>1.1</version>
      <version>1.2</version>
      <version>1.3</version>
      <version>1.4</version>
      <version>1.5</version>
      <version>1.6</version>
      <version>1.7</version>
      <version>1.8</version>
    </versions>
    <lastUpdated>20170428122549</lastUpdated>
  </versioning>
</metadata>
```

after triggering the cleanup process, the versions **1.1, 1.4, 1.5** and **1.6** will be deleted.

The Nexus Cleaner tool exposes 2 rest endpoints:

```
/start-cleanup (GET, POST)
/update-artifact-metadata (PUT)
```

### /start-cleanup (GET)

When a GET request is sent to this endpoint the application reads a json array of artifacts from a file on the classpath for which to look for metadata
and start the cleanup process

### /start-cleanup (POST)

When a POST request is sent to this endpoint the application expects a payload with a json array of artifacts for which to look for metadata
and start the cleanup process

### /update-artifact-metadata (PUT)

This endpoints is used to add or update the artifact metadata json which will be read by the Nexus Cleaner when the cleanup process is started.
The metadata json will be deployed in the artifacts folder on nexus with the name **{groupId}-{artifactId}-{environment}.nc.json** regardless of the name of the file (if it is not just a simple json payload)