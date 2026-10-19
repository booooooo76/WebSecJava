#!/usr/bin/env bash
# Same 10 requests as task3-basic-auth.http, runnable without the IntelliJ HTTP Client.
# Start the app first: ./mvnw spring-boot:run   (port 8081)

BASE=http://localhost:8081/api/notes
AUTH='Authorization: Basic YWRtaW46YWRtaW4xMjM='
BAD='Authorization: Basic YWRtaW46d3JvbmdwYXNz'
JSON='Content-Type: application/json'

req() {
  local label="$1" expect="$2"; shift 2
  local code
  code=$(curl -s -o /tmp/task3_body -w '%{http_code}' "$@")
  local mark="FAIL"; [ "$code" = "$expect" ] && mark="OK"
  printf '%-46s expect %s got %s  [%s]\n' "$label" "$expect" "$code" "$mark"
  LAST_BODY=$(cat /tmp/task3_body)
}

req "1. CREATE with credentials" 201 -X POST "$BASE" -H "$AUTH" -H "$JSON" \
  -d '{"title":"Secured note","content":"Created with Basic auth"}'
ID=$(echo "$LAST_BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

req "2. READ ALL with credentials"          200 "$BASE" -H "$AUTH"
req "3. READ ONE with credentials"          200 "$BASE/$ID" -H "$AUTH"
req "4. UPDATE with credentials"            200 -X PUT "$BASE/$ID" -H "$AUTH" -H "$JSON" \
  -d '{"title":"Secured note (edited)","content":"Updated with Basic auth"}'
req "5. DELETE with credentials"            204 -X DELETE "$BASE/$ID" -H "$AUTH"
req "6. READ deleted note with credentials" 404 "$BASE/$ID" -H "$AUTH"
req "7. READ ALL without credentials"       401 "$BASE"
req "8. CREATE without credentials"         401 -X POST "$BASE" -H "$JSON" \
  -d '{"title":"Should be rejected","content":"No Authorization header"}'
req "9. READ ALL with wrong password"       401 "$BASE" -H "$BAD"
req "10. DELETE without credentials"        401 -X DELETE "$BASE/1"
