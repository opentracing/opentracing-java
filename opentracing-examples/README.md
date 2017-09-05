# OpenTracing-Java examples

Examples of common instrumentation patterns:

- **activate_deactivate** - callbacks finish at some time.   
It shows continuation as a solution to finish span when last callback is completed.
- **active_span_replacement** - start an isolated task and query for its result in another task/thread
- **client_server** - typical client-server example
- **common_request_handler** - one request handler for all requests
- **late_span_finish** - late parent span finish
- **listener_per_request** - one listener per request
- **multiple_callbacks** - multiple callbacks
- **nested_callbacks** - nested callbacks

