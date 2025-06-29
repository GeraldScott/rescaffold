#!/bin/bash

# URL base
BASE_URL=http://localhost:8080/api

# Admin credentials
ADMIN_USERNAME=admin
ADMIN_PASSWORD=adminpassword

# User credentials for testing
TEST_USERNAME=testuser
TEST_PASSWORD=testpassword

# Login as admin and get token
echo "Logging in as admin..."
ADMIN_RESPONSE=$(curl -s -X POST \
  "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}")

ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

echo "Successfully logged in as admin"
echo "Admin Token: ${ADMIN_TOKEN:0:20}..."

# Create a test user
echo "Creating test user..."
curl -s -X POST \
  "${BASE_URL}/users" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${TEST_USERNAME}\"}" \
  -G -d "password=${TEST_PASSWORD}" -d "roles=ROLE_USER"

# Login as test user
echo "Logging in as test user..."
USER_RESPONSE=$(curl -s -X POST \
  "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${TEST_USERNAME}\",\"password\":\"${TEST_PASSWORD}\"}")

USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$USER_TOKEN" ]; then
  echo "Failed to get user token"
  exit 1
fi

echo "Successfully logged in as test user"
echo "User Token: ${USER_TOKEN:0:20}..."

# Test accessing protected endpoints
echo "Testing access to endpoints..."

echo "GET /api/genders (should succeed for test user):"
curl -s -X GET \
  "${BASE_URL}/genders" \
  -H "Authorization: Bearer ${USER_TOKEN}" | jq

echo "POST /api/genders (should fail for test user):"
curl -s -X POST \
  "${BASE_URL}/genders" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"code":"X","description":"Test Gender"}' | jq

echo "POST /api/genders (should succeed for admin):"
curl -s -X POST \
  "${BASE_URL}/genders" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"code":"X","description":"Test Gender"}' | jq

echo "Test complete"