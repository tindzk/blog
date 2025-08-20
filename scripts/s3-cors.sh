#!/bin/sh
aws s3api put-bucket-cors --bucket timsblog --cors-configuration file://cors.json
