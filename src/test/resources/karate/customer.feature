Feature: Customer API Tests
  Background:
    * url 'http://localhost:8080'
    * header Content-Type = 'application/json'

  # ============================================================================
  # KARATE BDD-STYLE API TESTS
  # ============================================================================
  #
  # This .feature file demonstrates KARATE testing framework for API testing.
  # Karate uses Gherkin syntax (Given-When-Then) for readable BDD-style tests.
  #
  # KEY FEATURES OF KARATE:
  # -----------------------
  # ✅ No Java code needed for simple tests
  # ✅ Built-in JSON/XML assertion support
  # ✅ Easy HTTP request/response handling
  # ✅ Data-driven testing with Examples
  # ✅ Readable by non-technical stakeholders
  #
  # ============================================================================

  # ===================== GET ALL CUSTOMERS =====================

  Scenario: Get all customers - returns list
    Given path '/api/customers'
    When method GET
    Then status 200

  # ===================== CREATE CUSTOMER =====================

  Scenario: Create new customer - success
    Given path '/api/customers'
    And request
      """
      {
        "name": "New Customer",
        "email": "new.customer@example.com"
      }
      """
    When method POST
    Then status 201
    And match response.id == '#number'
    And match response.name == 'New Customer'
    And match response.email == 'new.customer@example.com'

  Scenario: Create customer - validate response schema
    Given path '/api/customers'
    And request { "name": "Schema Test", "email": "schema@example.com" }
    When method POST
    Then status 201
    # Validate response structure
    And match response ==
      """
      {
        "id": "#number",
        "name": "#string",
        "email": "#string"
      }
      """

  # ===================== GET CUSTOMER BY ID =====================

  Scenario: Get customer by ID - success
    # First create a customer
    Given path '/api/customers'
    And request { "name": "Test Customer", "email": "test.customer@example.com" }
    When method POST
    Then status 201
    * def customerId = response.id

    # Then get by ID
    Given path '/api/customers', customerId
    When method GET
    Then status 200
    And match response.id == customerId
    And match response.name == 'Test Customer'
    And match response.email == 'test.customer@example.com'

  Scenario: Get customer by ID - not found
    Given path '/api/customers/99999'
    When method GET
    Then status 404

  # ===================== UPDATE CUSTOMER =====================

  Scenario: Update customer - success
    # First create a customer
    Given path '/api/customers'
    And request { "name": "Original Name", "email": "original@example.com" }
    When method POST
    Then status 201
    * def customerId = response.id

    # Then update
    Given path '/api/customers', customerId
    And request { "name": "Updated Name", "email": "updated@example.com" }
    When method PUT
    Then status 200
    And match response.name == 'Updated Name'
    And match response.email == 'updated@example.com'

  # ===================== DELETE CUSTOMER =====================

  Scenario: Delete customer - success
    # First create a customer
    Given path '/api/customers'
    And request { "name": "To Delete", "email": "delete@example.com" }
    When method POST
    Then status 201
    * def customerId = response.id

    # Then delete
    Given path '/api/customers', customerId
    When method DELETE
    Then status 204

    # Verify deletion
    Given path '/api/customers', customerId
    When method GET
    Then status 404

  # ===================== DATA-DRIVEN TEST =====================

  Scenario Outline: Create multiple customers - data driven
    Given path '/api/customers'
    And request { "name": "<name>", "email": "<email>" }
    When method POST
    Then status 201
    And match response.name == '<name>'
    And match response.email == '<email>'

    Examples:
      | name       | email                  |
      | Alice K    | alicek@example.com     |
      | Bob K      | bobk@example.com       |
      | Charlie K  | charliek@example.com   |

