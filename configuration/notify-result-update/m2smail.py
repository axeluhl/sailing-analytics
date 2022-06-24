#!/usr/bin/env python3
"""
--- Manage2Sail XRR Result document diff mailer ---
Example usage:
$ python3 m2smail.py 123456789 path/to/mailing_list_path
Minimum required Python version is 3.6
"""
import datetime
import difflib
import json
import pickle
import subprocess
import argparse
from sys import argv, stderr
from os import linesep, path
from shlex import quote
from urllib.request import urlopen

CACHE_BASE_PATH = "/tmp/"


def get_cache_path(name):
    return CACHE_BASE_PATH + "m2smail" + name + ".pickle"


def save_cache(name, cache):
    with open(get_cache_path(name), "w+b") as file:
        pickle.dump(cache, file)


def load_cache(name):
    cache_path = get_cache_path(name)
    if path.exists(cache_path):
        with open(cache_path, "rb") as file:
            return pickle.load(file)
    return None


def request(url):
    return urlopen(url).read().decode('utf-8')


def get_time():
    return datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat()


def pretty_json_dump(content):
    return json.dumps(content, indent=2, sort_keys=True).splitlines(True)


def nonblank_lines(f):
    for l in f:
        line = l.rstrip()
        if line:
            yield line


def send_mail(mailing_list_path, subject, body):
    mailing_list = []
    with open(mailing_list_path, "r") as f:
        for recipient in nonblank_lines(f):
            mailing_list.append(quote(recipient))
    if mailing_list:
        cmd = f"echo \"{body}\" | mail -s \"m2smail: {subject}\" {' '.join(mailing_list)}"
        result = subprocess.run(cmd, shell=True)
        if result.returncode != 0:
            print(f"[{get_time()}] mail exited with non 0 return code: {result.returncode}\n{result.stderr}", file=stderr)
    else:
        print(f"[{get_time()}] Mailing list is empty: {mailing_list_path}", file=stderr)


def main(event_id, mailing_list_path, class_list_path):
    # Initialize class whitelist
    class_list = []
    if class_list_path:
        with open(class_list_path, "r") as f:
            for classname in nonblank_lines(f):
                class_list.append(classname)

    # Send request
    print(f"[{get_time()}] Requesting results for {len(class_list)} classes...")
    response = request(
        f"http://manage2sail.com/api/public/links/event/{event_id}?accesstoken=bDAv8CwsTM94ujZ&mediaType=json")
    now = get_time()
    if response:
        data = json.loads(response)
        cache = load_cache(event_id)
        if cache is not None:
            output = []
            for regatta in data["Regattas"]:
                name = regatta["Name"]

                # Check class whitelist
                if class_list:
                    if name not in class_list:
                        continue

                cached_content = cache[name][0] if cache.get(
                    name) is not None and cache[name][0] is not None else ""
                cached_date = cache[name][1] if cache.get(
                    name) is not None and cache[name][1] is not None else "n/a"
                formatted_content = pretty_json_dump(regatta)
                diff = ''.join(difflib.unified_diff(cached_content, formatted_content, n=0,
                                                    fromfile=name, tofile=name, fromfiledate=cached_date, tofiledate=now))
                if diff:
                    cache[name] = (formatted_content, now)
                    output.append(diff)
            if output:
                print(f"[{get_time()}] {len(output)} change(s) found. Sending mail to {mailing_list_path}")
                body = "\n\n".join(output)
                send_mail(mailing_list_path, data["Name"], body)
                save_cache(event_id, cache)
            else:
                print(f"[{get_time()}] No changes")
        else:  # Initialize cache the first time we get a response
            print(f"[{get_time()}] Initializing cache")
            cache = {}
            for regatta in data["Regattas"]:
                cache[regatta["Name"]] = (pretty_json_dump(regatta), now)
            save_cache(event_id, cache)
    else:
        print(
            f"[{get_time()}] Request failed: {response.status_code} {response.url}", file=stderr)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Manage2Sail XRR Result document diff mailer')
    parser.add_argument('event_id', help='UUID of the m2s event')
    parser.add_argument('mailing_list_path', help='path to a file containing the recipients')
    parser.add_argument('-c', '--classes', help='path to a file containing the whitelisted classes')
    args = parser.parse_args()

    main(args.event_id, args.mailing_list_path, args.classes)
