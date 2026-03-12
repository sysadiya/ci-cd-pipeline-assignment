Feature: Order API Tests
  Background:
    * url 'http://localhost:8080'
    * header Content-Type = 'application/json'

  # ============================================================================
  # ORDER API - KARATE BDD TESTS
  # ============================================================================

  # ===================== GET ALL ORDERS =====================

  Scenario: Get all orders (list)
    Given path '/api/orders/all'
    When method GET
    Then status 200

  # ===================== GET ORDERS WITH PAGINATION =====================

  Scenario: Get orders with pagination
    Given path '/api/orders'
    And param page = 0
    And param size = 5
    When method GET
    Then status 200
    And match response.content == '#array'

  # ===================== GET ORDER BY ID =====================

  Scenario: Get order by ID - not found
    Given path '/api/orders/99999'
    When method GET
    Then status 404

  # ===================== DATE RANGE FILTER TEST =====================

  Scenario: Get orders between dates
    Given path '/api/orders/date-range'
    And param start = '2026-01-01'
    And param end = '2026-12-31'
    And param page = 0
    And param size = 10
    When method GET
    Then status 200
    And match response.content == '#array'

