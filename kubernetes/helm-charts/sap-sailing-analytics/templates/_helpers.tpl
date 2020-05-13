{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the release.
*/}}
{{- define "sapsailing-app-name" -}}
{{- default .Release.Name .Values.app | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Define error message for already existing "app" value.
*/}}
{{- define "already-existing-app-tag" -}}
{{ $appname := include "sapsailing-app-name" . }}
{{- printf "Invalid value set for \"app\"-tag, a release/pod already exists using \"app: %s\". Specify either release name or explicitly the app tag." $appname -}}
{{- end -}}

{{/*
Create the mongo connection string.
*/}}
{{- define "mongodb-connection-string-master" -}}
mongodb://{{ .Values.mongodb.mongodbService }}/{{- template "sapsailing-app-name" . -}}-master?replicaSet={{ .Values.mongodb.replicaSetName }} 
{{- end -}}
{{- define "mongodb-connection-string-replica" -}}
mongodb://{{ .Values.mongodb.mongodbService }}/{{- template "sapsailing-app-name" . -}}-replica?replicaSet={{ .Values.mongodb.replicaSetName }} 
{{- end -}}

{{/*
Define master service name
*/}}
{{- define "sailing-master-service-name" }}
{{- template "sapsailing-app-name" . }}-{{ .Values.master.role }}-service
{{- end }}