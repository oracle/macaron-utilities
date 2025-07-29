# Artifact Verifier Library

Library containing functionality to verify Maven artifacts against published provenances, according to a specified configuration.
Also provides an experimental feature to report alerts for particular artifacts and suggest alternative artifacts/versions.

# Verifier Configuration

Verification is configured by providing a list of Maven repository URLs against which artifacts will be verified (by obtaining provenance attestations corresponding to those artifacts from those Maven repositories), as well as a verification mode, which determines the behaviour of what constitutes acceptable verification or a verficiation failure.

Available verification modes:
- `NO_REQUIRE_VERIFY`: Verification is not required, so a failure is never returned. If verification succeeds for the artifact against any of the listed Maven repositories, return verified, otherwise return skipped
- `REQUIRE_VERIFY_IF_ATTESTED_ANY`: Verification is required if an attestation is present. If verification succeeds for the artifact against any of the listed Maven repositories, return verified, otherwise, if an non-matching attestation was found on any of the listed Maven repositories, return failure, otherwise return skipped (no attestation present on any repository).
- `REQUIRE_VERIFY_IF_ATTESTED_FIRST`: Verification is required if an attestation is present, and must be verified against the first attestation found (according to the Maven repository list order). If verification succeeds for the artifact against the first attestation found, return verified, otherwise return failure, unless no attestations are present on any of the Maven repositories, in which case return skipped.
- `REQUIRE_VERIFY_ANY`: Verification is required. If verification succeeds for the artifact against any of the listed Maven repositories, return verified, otherwise return failure.
- `REQUIRE_VERIFY_FIRST`: Verification is required, and must be verified against the first attestation found (according to the Maven repository list order). If verification succeeds for the artifact against the first attestation found, return verified, otherwise return failure, including if there are no attestations present on any of the Maven repositories.


# Artifact Usage Alert Configuration

(note: this feature is experimental and likely to change substantially in future)

Artifact usage alert configuration is provided as Json, in order to specify for which artifacts an alert should be reported, as well as optionally a set of alternative artifacts that should be suggested instead. It has the following essential format:
```
{
    "type": <artifact-usage-reporter-type>,
    <type-specific-definition-fields>
}
```

The precise format of the configuration depends on the type of artifact usage reporter selected. The available artifact usage reporter types are:

## report-gav-list

The Report GAV List artifact usage reporter type specifies a reporter that reports usage of any GAV included in an explicit list of GAVs. For example:
```
{
    "type": "report-gav-list",
    "gav-list": [
        "io.micronaut:micronaut-inject:3.8.5-oracle-00001",
        "io.micronaut:micronaut-core:3.8.5-oracle-00001",
        "io.micronaut:micronaut-inject:4.6.8-oracle-00001",
        "io.micronaut:micronaut-core:4.6.8-oracle-00001"
    ]
}
```
For reports produced by this reporter, only an alert will be reported, no alternative artifacts will be suggested.

## report-gav-alternatives-map

The Report GAV Alternatives Map artifact usage reporter type specifies a reporter that reports usage of any GAV included in an explicit mapping of GAVs to a list of alternative artifact suggestions. For example:
```
{
    "type": "report-gav-alternatives-map",
    "gav-alternatives": {
        "io.micronaut:micronaut-inject:3.8.5": ["io.micronaut:micronaut-inject:3.8.5-oracle-00001"],
        "io.micronaut:micronaut-core:3.8.5": ["io.micronaut:micronaut-core:3.8.5-oracle-00001"],
        "io.micronaut:micronaut-inject:4.6.8": ["io.micronaut:micronaut-inject:4.6.8-oracle-00001"],
        "io.micronaut:micronaut-core:4.6.8": ["io.micronaut:micronaut-core:4.6.8-oracle-00001"]
    }
}
```

## report-gav-regex

The Report GAV Regex artifact usage reporter type specifies a reporter that reports usage of any GAV that matches a specified set of regular expressions for each component of the GAV. For example:

```
{
    "type": "report-gav-regex",
    "gav-regex": {
        "~version": ".*-oracle-[0-9]+"
    }
}
```

The `gav-regex` specification follows the same format as in `verify-gav-regex`.

For reports produced by this reporter, only an alert will be reported, no alternative artifacts will be suggested.

## reporter-list

The Reporter List artifact usage reporter type specifies a reporter composed of a set of nested reporter configurations, artifacts are checked against all reporters and the union of those alerts is reported. For example:

```
{
    "type": "reporter-list",
    "reporters": [
        {
            "type": "report-gav-alternatives-map",
            "gav-alternatives": {
                "io.micronaut:micronaut-inject:3.8.5": ["io.micronaut:micronaut-inject:3.8.5-oracle-00001"],
                "io.micronaut:micronaut-core:3.8.5": ["io.micronaut:micronaut-core:3.8.5-oracle-00001"],
                "io.micronaut:micronaut-inject:4.6.8": ["io.micronaut:micronaut-inject:4.6.8-oracle-00001"],
                "io.micronaut:micronaut-core:4.6.8": ["io.micronaut:micronaut-core:4.6.8-oracle-00001"]
            }
        },
        {
            "type": "report-gav-regex",
            "gav-regex": {
                "~version": ".*-oracle-[0-9]+"
            }
        }
    ]
}
```
