# OpenTracing-Java examples

Goal of these examples is to
- test API changes
- use for regression testing
- show common instrumentation patterns

List of patterns:

- **activate_deactivate** - actions are executed by scheduler.   
It shows continuation as a solution to finish span when last action is completed.
- **active_span_replacement** - start an isolated task and query for its result in another task/thread
- **client_server** - typical client-server example
- **common_request_handler** - one request handler for all requests
- **late_span_finish** - late parent span finish
- **listener_per_request** - one listener per request
- **multiple_callbacks** - many callbacks spawned at the same time
- **nested_callbacks** - one callback at the time, defined in a pipeline fashion
- **slf4j_mdc_debug_id** - keep debug_id of the active Span inside MDC

