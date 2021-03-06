#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
FROM openjdk:8-jre-alpine

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.name="Apache Pulsar Manager" \
      org.label-schema.description="An Apache Pulsar Manager for management Pulsar clusters" \
      org.label-schema.url="https://github.com/streamnative/pulsar-manager" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/streamnative/pulsar-manager" \
      org.label-schema.vendor="StreamNative Limited" \
      org.label-schema.version=$VERSION \
      org.label-schema.schema-version="1.0"

RUN apk update

RUN apk add nginx \
  && apk add supervisor \
  && apk add mysql \
  && apk add mysql-client \
  && rm  -rf /tmp/* \
  && rm /var/cache/apk/*

RUN mkdir -p /run/nginx

WORKDIR /pulsar-manager

COPY docker/my.cnf /etc/

COPY build/libs/*.jar pulsar-manager.jar

COPY docker/supervisord.conf /etc/

COPY docker/default.conf /etc/nginx/conf.d/

COPY docker/startup.sh /pulsar-manager/

COPY docker/init_db.sql /pulsar-manager/

COPY docker/entrypoint.sh /pulsar-manager/

COPY front-end/dist /usr/share/nginx/html/

ENTRYPOINT [ "/pulsar-manager/entrypoint.sh" ]
