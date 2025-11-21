# language: en
@config-api @redirect @tls @negative
Feature: Accessing Configuration API over HTTP/HTTPS
  As a client of the Configuration API
  I want correct behavior for HTTP and HTTPS access
  So that redirects are enforced and invalid TLS chains are surfaced

  Background:
    Given the endpoint path "/configuration/api/v1/live/destinations"

  @redirect
  Scenario Outline: HTTP requests are permanently redirected to HTTPS
    Given the base host "<host>"
    And I construct the URL "http://<host><path>"
    When I send a GET request to that URL
    Then the response status code should be 308
    And the "Location" header should start with "https://<host><path>"

    Examples:
      | host                                             | path                                    |
      | configuration.dev.mercury.sitacloud.aero         | /configuration/api/v1/live/destinations |
      | configuration.uat.mercury.sitacloud.aero         | /configuration/api/v1/live/destinations |

  @tls @negative
  Scenario Outline: HTTPS request fails due to untrusted certificate chain
    Given the base host "<host>"
    And I construct the URL "https://<host><path>"
    When I send a GET request to that URL with default certificate validation
    Then the request should fail during TLS handshake
    And the error message should contain one of:
      | SEC_E_UNTRUSTED_ROOT |
      | self signed          |
      | certificate verify failed |
      | unable to get local issuer certificate |

    Examples:
      | host                                             | path                                    |
      | configuration.dev.mercury.sitacloud.aero         | /configuration/api/v1/live/destinations |
      | configuration.uat.mercury.sitacloud.aero         | /configuration/api/v1/live/destinations |
