#!/usr/bin/env bash

function curl_wrapper()
{
    local out=$("$@")
    local return_code=$?
    local status_code=$(get_status_code "$out")
    local response=$(get_response "$out")
    local message=$(get_http_code_message $status_code)

    if is_http_ok $status_code; then
        success "Successfully ran [ $@ ]. Response: $response"
        echo $response
    else
        error "Error: Command [ $@ ] returned: [$status_code] $message"
    fi
}
