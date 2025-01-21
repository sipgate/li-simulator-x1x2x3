# State Machines for Scenarios

Wiremock responds differently, based on matches to the request and depending on the current State Machine state. Having
called a specific request can move the State Machine to another state (i.e., after creating a destination, you can get
the destination).

See more information [in the Wiremock docs](https://wiremock.org/docs/stateful-behaviour/).

## Check Destination Details and Task

The State Machine simulates the different states that a X1 Network elements can have.

To restart a scenario, send an empty POST to `/__admin/scenarios/reset`. You are back to Scenario `Started` after that.

```mermaid
flowchart LR
  started["Started"] --"CreateDestinationRequest"--> destination_added["Destination Added"]
  started --"GetDestinationDetails fails" --> started
  started --"ModifyDestinationRequest fails" --> started
  started --"RemoveDestinationRequest fails" --> started
  started --"ActivateTaskRequest with unknown dID fails"--> started
  started --"ListAllDetailsRequest has no data" --> started
  destination_added --"GetDestinationDetails positive " --> destination_added
  destination_added --"ModifyDestinationRequest"--> destination_modified["Destination Modified"]
  destination_modified --"GetDestinationDetails with other data" --> destination_modified
  destination_added --"CreateDestinationRequest fails" --> destination_added
  destination_added --"RemoveDestinationRequest positive" --> started
  destination_added --"ActivateTaskRequest with existing dID positive"-->task_added["Task Added"]
  destination_added --"ActivateTaskRequest fails because of DeliveryType mismatch" --> destination_added
  task_added --"RemoveDestinationRequest fails (depending task)"--> task_added
  task_added --"DeactivateTaskRequest"--> destination_added
  destination_added --"GetTaskDetails fails" --> destination_added
  destination_added --"ModifyTaskRequest fails" --> destination_added
  destination_added --"DeactivateTaskRequest fails" --> destination_added
  destination_added --"ListAllDetailsRequest has destination" --> destination_added
  task_added --"GetTaskDetails positive" --> task_added
  task_added --"ListAllDetailsRequest has task and destination" --> task_added
  task_added --ModifyTaskRequest--> task_modified["Task Modified"]
  task_modified --"GetTaskDetails with other data" --> task_modified
  task_added --"ModifyTaskRequest fails because of DeliveryType mismatch" --> task_added
  task_added --"ActivateTaskRequest fails" --> task_added

```
