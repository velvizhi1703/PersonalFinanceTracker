Feature: Authentication API Tests

Background:
  * url 'http://localhost:9091'
  * configure headers = { 'Content-Type': 'application/json' }


Scenario: Successful login with admin credentials
  Given path '/api/auth/login'
  And request { email: 'vel@example.com', password: 'vel123' }  # Use valid admin credentials
  When method post
  Then status 200
  And match response.token == '#notnull'
  And match response.role == 'ROLE_ADMIN'
  And match response.userId == '#number'
  
  * def authToken = response.token
  * def adminUserId = response.userId

Scenario: Successful login with user credentials
  Given path '/api/auth/login'
  And request { email: 'keerthi@example.com', password: 'keerthi123' }  # Use valid user credentials
  When method post
  Then status 200
  And match response.token == '#notnull'
  And match response.role == 'ROLE_USER'
  And match response.userId == '#number'
  
  * def userToken = response.token
  * def regularUserId = response.userId

Scenario: Login with invalid credentials should fail
  Given path '/api/auth/login'
  And request { email: 'wrong@example.com', password: 'wrong' }
  When method post
  Then status 401

Scenario: Login with disabled account should be forbidden
  Given path '/api/auth/login'
  And request { email: 'kala@example.com', password: 'kala123' }
 When method post
Then status 403
# Or if 200 is correct:
Then status 200
And match response contains { token: '#string' }
* def userToken = response.token

Scenario: Get user transactions with valid token
  Given path '/api/transactions/user'
  And header Authorization = 'Bearer ' + userToken
  When method get
  Then status 200
  And match response._embedded.transactions == '#array'
  
Scenario: Admin access to all transactions
  Given path '/api/transactions/admin'
  And header Authorization = 'Bearer ' + authToken
  When method get
  Then status 200
  And match response._embedded.transactions == '#array'

Scenario: Regular user trying to access admin endpoint should be forbidden
  Given path '/api/transactions/admin'
  And header Authorization = 'Bearer ' + userToken
  When method get
  Then status 403