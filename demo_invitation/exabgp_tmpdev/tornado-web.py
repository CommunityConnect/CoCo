#!/usr/bin/env python

#http://unix.stackexchange.com/questions/57938/generic-http-server-that-just-dumps-post-requests

import tornado.ioloop
import tornado.web
import pprint

class MyDumpHandler(tornado.web.RequestHandler):
    def post(self):
        print('=======REQUEST=======')
        pprint.pprint(self.request)
        print('=======REQUEST BODY=======')
        pprint.pprint(self.request.body)

if __name__ == "__main__":
    tornado.web.Application([(r"/.*", MyDumpHandler),]).listen(5003)
    tornado.ioloop.IOLoop.instance().start()

