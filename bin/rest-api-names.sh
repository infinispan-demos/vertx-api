#!/usr/bin/env bash
echo curl -X POST -H "Content-Type: application/json" -d '{"id":"42", "name":"Oihana"}' "http://localhost:8081/api/cutenames";
curl -X POST -H "Content-Type: application/json" -d '{"id":"42", "name":"Oihana"}' "http://localhost:8081/api/cutenames";
echo call curl -X GET -H "Content-Type: application/json" "http://localhost:8081/api/cutenames/42";
curl -X GET -H "Content-Type: application/json" "http://localhost:8081/api/cutenames/42";