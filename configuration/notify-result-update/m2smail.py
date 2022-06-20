#!/usr/bin/env python3
"""
--- Manage2Sail XRR Result document diff mailer ---
Example usage:
$ python3 m2smail.py 123456789 path/to/mailinglist
Minimum required Python version is 3.6
"""
import datetime
import difflib
import json
import pickle
import subprocess
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


def send_mail(mailinglist_path, subject, body):
    mailinglist = []
    with open(mailinglist_path, "r") as f:
        for recipient in nonblank_lines(f):
            mailinglist.append(quote(recipient))
    if mailinglist:
        cmd = f"echo \"{body}\" | mail -s \"m2smail: {subject}\" {' '.join(mailinglist)}"
        result = subprocess.run(cmd, shell=True)
        if result.returncode != 0:
            print(f"[{get_time()}] mail exited with non 0 return code: {result.returncode}\n{result.stderr}", file=stderr)
    else:
        print(f"[{get_time()}] Mailinglist is empty: {mailinglist_path}", file=stderr)


def main(event_id, mailinglist):
    print(f"[{get_time()}] Starting up")
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
                print(f"[{get_time()}] Sending mail with {len(output)} changes to {mailinglist}")
                body = "\n\n".join(output)
                send_mail(mailinglist, data["Name"], body)
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
    if len(argv) < 3:
        print("Error: Missing argument(s)" + linesep + "usage:" + linesep + "$ python3 " +
              argv[0] + " <event_id> <mailing_list>", file=stderr)
        exit(1)
    main(argv[1], argv[2])
