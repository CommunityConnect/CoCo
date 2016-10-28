#!/usr/bin/env python

import os
import sys
import time
from httplib2 import Http
import json


# portalURL = 'http://localhost:9090/CoCo-agent/rest/bgp'
#portalURL = 'http://localhost:5003'
portalURL = 'http://10.10.10.2:5003'

#fire up ~/demo_invitation/exabgp_tmpdev/tornado-web.py  for tests



def _prefixed(level, message):
    now = time.strftime('%a, %d %b %Y %H:%M:%S', time.localtime())
    return "%s %-8s %-6d %s" % (now, level, os.getpid(), message)


# When the parent dies we are seeing continual newlines, so we only access so many before stopping
counter = 0

while True:
    try:
        line = sys.stdin.readline().strip()
        sys.stdout.flush()
        if line == "":
            counter += 1
            if counter > 100:
                break
            continue

        counter = 0
        http_obj = Http()
        resp, content = http_obj.request(
            uri=portalURL,
            method='POST',
            headers={'Content-Type': 'application/json; charset=UTF-8'},
            body=line
        )

        print >> sys.stderr, _prefixed(sys.argv[1] if len(sys.argv) >= 2 else 'EXABGP PROCESS', line)


    except KeyboardInterrupt:
        pass
    except IOError:
        # most likely a signal during readline
        pass
