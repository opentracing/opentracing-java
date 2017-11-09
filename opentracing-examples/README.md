# OpenTracing-Java examples

Goal of these examples is to
- test API changes
- use for regression testing
- show common instrumentation patterns

List of patterns:

- **activate_deactivate** - actions are executed by scheduler.   
It shows continuation as a solution to finish span when last action is completed.
- **active_span_replacement** - start an isolated task and query for its result in another task/thread
- **actor_propagation** - tracing for blocking and non-blocking actor based tracing
- **client_server** - typical client-server example
- **common_request_handler** - one request handler for all requests
- **late_span_finish** - late parent span finish
- **listener_per_request** - one listener per request
- **multiple_callbacks** - many callbacks spawned at the same time
- **nested_callbacks** - one callback at the time, defined in a pipeline fashion
- **promise_propagation** - tracing patterns for promises with callbacks
- **suspend_resume_propagation** - tracing pattern for interleaving of spans
